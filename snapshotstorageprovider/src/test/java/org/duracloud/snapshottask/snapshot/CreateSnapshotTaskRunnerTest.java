/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeResult;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskResult;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.domain.RetrievedContent;
import org.duracloud.storage.error.ServerConflictException;
import org.duracloud.storage.error.StorageStateException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 1/29/14
 */
public class CreateSnapshotTaskRunnerTest {

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private RestHttpHelper restHelper;
    private CreateSnapshotTaskRunner taskRunner;

    private String dcHost = "instance-host";
    private String dcPort = "instance-port";
    private String dcStoreId = "store-id";
    private String dcAccountName = "account-name";
    private String dcSnapshotUser = "snapshot-user";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";
    private String bridgeMemberId = "bridge-member-id";

    @Before
    public void setup() {
        snapshotProvider = EasyMock.createMock("StorageProvider",
                                               StorageProvider.class);
        unwrappedSnapshotProvider =
            EasyMock.createMock("SnapshotStorageProvider",
                                SnapshotStorageProvider.class);
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner =
            new CreateSnapshotTaskRunner(snapshotProvider, unwrappedSnapshotProvider,
                                         dcHost, dcPort, dcStoreId, dcAccountName,
                                         dcSnapshotUser, bridgeHost,
                                         bridgePort, bridgeUser, bridgePass, bridgeMemberId);
    }

    private void replayMocks() {
        EasyMock.replay(snapshotProvider, unwrappedSnapshotProvider, restHelper);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(snapshotProvider, unwrappedSnapshotProvider, restHelper);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("create-snapshot", taskRunner.getName());
    }

    @Test
    public void testBuildSnapshotURL() {
        replayMocks();

        String snapshotId = "snapshot-id";

        String snapshotUrl = taskRunner.buildSnapshotURL(snapshotId);
        String expectedUrl = "http://" + bridgeHost + ":" + bridgePort +
                             "/bridge/snapshot/" + snapshotId;
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testBuildSnapshotBody() {
        replayMocks();

        String spaceId = "space-id";
        String description = "description";
        String userEmail = "user-email";
        CreateSnapshotTaskParameters taskParams =
            new CreateSnapshotTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setDescription(description);
        taskParams.setUserEmail(userEmail);

        String result = taskRunner.buildSnapshotBody(taskParams);
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"host\":\"" + dcHost + "\""));
        assertThat(cleanResult, containsString("\"port\":\"" + dcPort + "\""));
        assertThat(cleanResult, containsString("\"storeId\":\"" + dcStoreId + "\""));
        assertThat(cleanResult, containsString("\"spaceId\":\"" + spaceId + "\""));
        assertThat(cleanResult, containsString("\"description\":\"" + description + "\""));
        assertThat(cleanResult, containsString("\"userEmail\":\"" + userEmail + "\""));
        assertThat(cleanResult, containsString("\"memberId\":\"" + bridgeMemberId + "\""));

    }

    @Test
    public void testGenerateSnapshotId() {
        replayMocks();

        String spaceId = "space-id";
        long timestamp = System.currentTimeMillis();
        String snapshotId = taskRunner.generateSnapshotId(spaceId, timestamp);

        String delim = SnapshotIdentifier.DELIM;
        String expectedSnapshotId =
            dcAccountName + delim + dcStoreId + delim + spaceId + delim;
        assertTrue(snapshotId.startsWith(expectedSnapshotId));
        assertTrue(snapshotId.endsWith(DateUtil.convertToStringPlain(timestamp)));
    }

    @Test
    public void testAddSnapshotIdToSpaceProps() throws Exception {
        String spaceId = "space-id";
        String snapshotId = "snapshot-id";

        Map<String, String> spaceProps = new HashMap<>();
        EasyMock.expect(snapshotProvider.getSpaceProperties(spaceId))
                .andReturn(spaceProps);

        Capture<Map<String, String>> propsCapture = Capture.newInstance(CaptureType.FIRST);
        unwrappedSnapshotProvider.setNewSpaceProperties(EasyMock.eq(spaceId),
                                                        EasyMock.capture(propsCapture));
        EasyMock.expectLastCall().once();

        replayMocks();

        taskRunner.addSnapshotIdToSpaceProps(spaceId, snapshotId);
        Map<String, String> updatedSpaceProps = propsCapture.getValue();
        assertEquals(String.valueOf(snapshotId),
                     updatedSpaceProps.get(Constants.SNAPSHOT_ID_PROP));
    }

    @Test
    public void testSetSnapshotUserPermissions() {
        String spaceId = "space-id";
        String aclUserName = "acl-user-name";
        AclType aclValue = AclType.WRITE;

        Map<String, AclType> spaceACLs = new HashMap<>();
        spaceACLs.put(aclUserName, aclValue);
        EasyMock.expect(snapshotProvider.getSpaceACLs(spaceId))
                .andReturn(spaceACLs);

        Capture<Map<String, AclType>> spaceACLsCapture =
            Capture.newInstance(CaptureType.FIRST);
        snapshotProvider.setSpaceACLs(EasyMock.eq(spaceId),
                                      EasyMock.capture(spaceACLsCapture));

        replayMocks();

        taskRunner.setSnapshotUserPermissions(spaceId);
        Map<String, AclType> capSpaceACLs = spaceACLsCapture.getValue();
        assertEquals(capSpaceACLs.get(aclUserName), aclValue);
        String user = StorageProvider.PROPERTIES_SPACE_ACL + dcSnapshotUser;
        assertEquals(capSpaceACLs.get(user), AclType.READ);
    }

    @Test
    public void testBuildSnapshotProps() throws Exception {
        replayMocks();

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put("one", "two");
        propsMap.put("three", "four");
        String props = taskRunner.buildSnapshotProps(propsMap);
        assertTrue(props.contains("one=two"));
        assertTrue(props.contains("three=four"));

        Properties realProps = new Properties();
        realProps.load(new StringReader(props));
        assertEquals("two", realProps.getProperty("one"));
        assertEquals("four", realProps.getProperty("three"));
    }

    @Test
    public void testStoreSnapshotProps() {
        String spaceId = "space-id";
        String props = "one=two";

        EasyMock.expect(
            snapshotProvider.addContent(EasyMock.eq(spaceId),
                                        EasyMock.eq(Constants.SNAPSHOT_PROPS_FILENAME),
                                        EasyMock.eq("text/x-java-properties"),
                                        EasyMock.<Map<String, String>>isNull(),
                                        EasyMock.eq((long) props.length()),
                                        EasyMock.<String>anyObject(),
                                        EasyMock.<InputStream>anyObject()))
                .andReturn("success!");
        replayMocks();

        taskRunner.storeSnapshotProps(spaceId, props);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String snapshotId = "snapshot-id";
        String snapshotURL = "snapshot-url";
        String snapshotBody = "snapshot-body";

        CreateSnapshotBridgeResult bridgeResult =
            new CreateSnapshotBridgeResult(snapshotId,
                                           SnapshotStatus.INITIALIZED);
        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(201, null, resultStream);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        EasyMock.expect(restHelper.put(snapshotURL, snapshotBody, headers))
                .andReturn(response);

        replayMocks();

        String callResult =
            taskRunner.callBridge(restHelper, snapshotURL, snapshotBody);

        CreateSnapshotTaskResult taskResult =
            CreateSnapshotTaskResult.deserialize(callResult);
        assertEquals(snapshotId, taskResult.getSnapshotId());
        assertEquals(SnapshotStatus.INITIALIZED, taskResult.getStatus());
    }

    @Test
    public void testCallBridgeFailure() throws Exception {
        String snapshotURL = "snapshot-url";
        String snapshotBody = "snapshot-body";

        InputStream resultStream = IOUtil.writeStringToStream("Error");
        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(500, null, resultStream);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        EasyMock.expect(restHelper.put(snapshotURL, snapshotBody, headers))
                .andReturn(response);

        replayMocks();

        try {
            taskRunner.callBridge(restHelper, snapshotURL, snapshotBody);
            fail("Exception expected on 500 response");
        } catch (RuntimeException e) {
            // Expected exception
        }
    }

    @Test
    public void testCallBridgeFailureConflict() throws Exception {
        String snapshotURL = "snapshot-url";
        String snapshotBody = "snapshot-body";
        String errorMessage = "Error";
        InputStream resultStream = IOUtil.writeStringToStream(errorMessage);
        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(409, null, resultStream);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        EasyMock.expect(restHelper.put(snapshotURL, snapshotBody, headers))
                .andReturn(response);

        replayMocks();

        try {
            taskRunner.callBridge(restHelper, snapshotURL, snapshotBody);
            fail("Exception expected on 409 response");
        } catch (ServerConflictException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void testPerformSnapshotPropertiesAlreadyExists() throws Exception {
        String spaceId = "space-id";
        CreateSnapshotTaskParameters params = new CreateSnapshotTaskParameters();
        params.setDescription("desc");
        params.setSpaceId(spaceId);
        params.setUserEmail("test@duracloud.org");
        String snapshotId = "snapshot-001";
        Properties props = new Properties();
        props.put(Constants.SNAPSHOT_ID_PROP, snapshotId);

        RetrievedContent content = new RetrievedContent();
        content.setContentStream(new ByteArrayInputStream(
            (Constants.SNAPSHOT_ID_PROP + "=" + snapshotId).getBytes()));
        EasyMock.expect(this.snapshotProvider.getContent(spaceId,
                                                         Constants.SNAPSHOT_PROPS_FILENAME))
                .andReturn(content);
        replayMocks();
        try {
            taskRunner.performTask(params.serialize());
            fail("Exception expected on 500 response");
        } catch (StorageStateException e) {
            assertTrue(e.getMessage().contains(snapshotId));
        }
    }

}
