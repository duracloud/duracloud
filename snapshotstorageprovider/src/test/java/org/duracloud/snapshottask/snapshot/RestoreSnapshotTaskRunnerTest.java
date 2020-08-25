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
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.dto.bridge.CreateRestoreBridgeResult;
import org.duracloud.snapshot.dto.task.RestoreSnapshotTaskResult;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 7/30/14
 */
public class RestoreSnapshotTaskRunnerTest {

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private RestHttpHelper restHelper;
    private RestoreSnapshotTaskRunner taskRunner;

    private String dcHost = "instance-host";
    private String dcPort = "instance-port";
    private String dcStoreId = "store-id";
    private String dcSnapshotUser = "snapshot-user";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        snapshotProvider = EasyMock.createMock("StorageProvider",
                                               StorageProvider.class);
        unwrappedSnapshotProvider =
            EasyMock.createMock("SnapshotStorageProvider",
                                SnapshotStorageProvider.class);
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner =
            new RestoreSnapshotTaskRunner(snapshotProvider, unwrappedSnapshotProvider,
                                          dcHost, dcPort, dcStoreId, dcSnapshotUser,
                                          bridgeHost, bridgePort, bridgeUser,
                                          bridgePass);
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
        assertEquals("restore-snapshot", taskRunner.getName());
    }

    @Test
    public void testBuildBridgeURL() {
        replayMocks();

        String bridgeUrl = taskRunner.buildBridgeURL();
        String expectedUrl = "http://" + bridgeHost + ":" + bridgePort +
                             "/bridge/restore";
        assertEquals(expectedUrl, bridgeUrl);
    }

    @Test
    public void testBuildBridgeBody() {
        replayMocks();

        String spaceId = "space-id";
        String snapshotId = "snapshot-id";
        String userEmail = "user-email";

        String result =
            taskRunner.buildBridgeBody(spaceId, snapshotId, userEmail);
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"host\":\"" + dcHost + "\""));
        assertThat(cleanResult, containsString("\"port\":\"" + dcPort + "\""));
        assertThat(cleanResult, containsString("\"storeId\":\"" + dcStoreId + "\""));
        assertThat(cleanResult, containsString("\"spaceId\":\"" + spaceId + "\""));
        assertThat(cleanResult, containsString("\"snapshotId\":\"" + snapshotId + "\""));
        assertThat(cleanResult, containsString("\"userEmail\":\"" + userEmail + "\""));
    }

    @Test
    public void testSetRestoreSpaceUserPermissions() {
        String spaceId = "space-id";

        Capture<Map<String, AclType>> spaceACLsCapture = Capture.newInstance(CaptureType.FIRST);
        snapshotProvider.setSpaceACLs(EasyMock.eq(spaceId),
                                      EasyMock.capture(spaceACLsCapture));

        replayMocks();

        taskRunner.setRestoreSpaceUserPermissions(spaceId);
        Map<String, AclType> capSpaceACLs = spaceACLsCapture.getValue();
        String user = StorageProvider.PROPERTIES_SPACE_ACL + dcSnapshotUser;
        assertEquals(capSpaceACLs.get(user), AclType.WRITE);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String restoreId = "restore-id";
        String snapshotURL = "snapshot-url";
        String snapshotBody = "snapshot-body";

        CreateRestoreBridgeResult bridgeResult =
            new CreateRestoreBridgeResult(restoreId,
                                          RestoreStatus.INITIALIZED);
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

        RestoreSnapshotTaskResult taskResult =
            RestoreSnapshotTaskResult.deserialize(callResult);
        assertEquals(restoreId, taskResult.getRestoreId());
        assertEquals(RestoreStatus.INITIALIZED, taskResult.getStatus());
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
        } catch (TaskException e) {
            // Expected exception
        }
    }

    @Test
    public void testAddRestoreIdToSpaceProps() throws Exception {
        String restoreSpaceId = "restore-space-id";
        String restoreId = "restore-id";

        Map<String, String> spaceProps = new HashMap<>();
        EasyMock.expect(snapshotProvider.getSpaceProperties(restoreSpaceId))
                .andReturn(spaceProps);

        Capture<Map<String, String>> propsCapture = Capture.newInstance(CaptureType.FIRST);
        unwrappedSnapshotProvider.setNewSpaceProperties(EasyMock.eq(restoreSpaceId),
                                                        EasyMock.capture(propsCapture));
        EasyMock.expectLastCall().once();

        replayMocks();

        taskRunner.addRestoreIdToSpaceProps(restoreSpaceId, restoreId);
        Map<String, String> updatedSpaceProps = propsCapture.getValue();
        assertEquals(String.valueOf(restoreId),
                     updatedSpaceProps.get(Constants.RESTORE_ID_PROP));
    }

    @Test
    public void testCheckExistingRestore() {
        replayMocks();
        // TODO: Write test
    }

    @Test
    public void testCreateSpace() {
        replayMocks();
        // TODO: Write test
    }

}
