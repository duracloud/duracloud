/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.snapshot.dto.CreateSnapshotTaskParameters;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 1/29/14
 */
public class CreateSnapshotTaskRunnerTest {

    private SnapshotStorageProvider snapshotProvider;
    private CreateSnapshotTaskRunner taskRunnerCreate;

    private String dcHost = "instance-host";
    private String dcPort = "instance-port";
    private String dcStoreId = "store-id";
    private String dcAccountName = "account-name";
    private String dcSnapshotUser = "snapshot-user";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        snapshotProvider = EasyMock.createMock("SnapshotStorageProvider",
                                               SnapshotStorageProvider.class);
        taskRunnerCreate =
            new CreateSnapshotTaskRunner(snapshotProvider, dcHost, dcPort,
                                         dcStoreId, dcAccountName,
                                         dcSnapshotUser, bridgeHost,
                                         bridgePort, bridgeUser, bridgePass);
    }

    private void replayMocks() {
        EasyMock.replay(snapshotProvider);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(snapshotProvider);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("create-snapshot", taskRunnerCreate.getName());
    }

    @Test
    public void testBuildSnapshotURL() {
        replayMocks();

        String snapshotId = "snapshot-id";

        String snapshotUrl = taskRunnerCreate.buildSnapshotURL(snapshotId);
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
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

        String result = taskRunnerCreate.buildSnapshotBody(taskParams);
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"host\":\""+dcHost+"\""));
        assertThat(cleanResult, containsString("\"port\":\""+dcPort+"\""));
        assertThat(cleanResult, containsString("\"storeId\":\""+dcStoreId+"\""));
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId+"\""));
        assertThat(cleanResult, containsString("\"description\":\""+description+"\""));
        assertThat(cleanResult, containsString("\"userEmail\":\""+userEmail+"\""));
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

        Capture<Map<String, AclType>> spaceACLsCapture = new Capture<>();
        snapshotProvider.setSpaceACLs(EasyMock.eq(spaceId),
                                      EasyMock.capture(spaceACLsCapture));

        replayMocks();

        taskRunnerCreate.setSnapshotUserPermissions(spaceId);
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
        String props = taskRunnerCreate.buildSnapshotProps(propsMap);
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
                                     EasyMock.eq(Constants.SNAPSHOT_ID),
                                     EasyMock.eq("text/x-java-properties"),
                                     EasyMock.<Map<String, String>>isNull(),
                                     EasyMock.eq((long)props.length()),
                                     EasyMock.<String>anyObject(),
                                     EasyMock.<InputStream>anyObject()))
                .andReturn("success!");
        replayMocks();

        taskRunnerCreate.storeSnapshotProps(spaceId, props);
    }

}
