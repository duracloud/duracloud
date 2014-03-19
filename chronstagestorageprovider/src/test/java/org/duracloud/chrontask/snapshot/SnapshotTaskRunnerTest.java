/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chrontask.snapshot;

import org.duracloud.chronstorage.ChronStageStorageProvider;
import org.duracloud.common.constant.Constants;
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
import static org.junit.Assert.assertTrue;

/**
 * @author Bill Branan
 *         Date: 1/29/14
 */
public class SnapshotTaskRunnerTest {

    private ChronStageStorageProvider chronProvider;
    private SnapshotTaskRunner taskRunner;

    private String dcHost = "instance-host";
    private String dcPort = "instance-port";
    private String dcStoreId = "store-id";
    private String dcAccountName = "account-name";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        chronProvider = EasyMock.createMock("ChronStageStorageProvider",
                                            ChronStageStorageProvider.class);
        taskRunner = new SnapshotTaskRunner(chronProvider, dcHost, dcPort,
                                            dcStoreId, dcAccountName, bridgeHost,
                                            bridgePort, bridgeUser, bridgePass);
    }

    private void replayMocks() {
        EasyMock.replay(chronProvider);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(chronProvider);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("snapshot", taskRunner.getName());
    }

    @Test
    public void testBuildSnapshotURL() {
        replayMocks();

        String spaceId = "space-id";
        String snapshotId = "snapshot-id";

        String snapshotUrl = taskRunner.buildSnapshotURL(spaceId, snapshotId);
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                             "/snapshot/" + dcHost + "/" + dcPort +"/" +
                             dcStoreId + "/" + spaceId + "/" + snapshotId;
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testParseTaskParams() {
        replayMocks();

        String taskParamsSerialized =
            "{\"spaceId\" : \"test-space\", \"snapshotProperties\" : " +
            "{\"description\" : \"test snapshot\"}}";

        SnapshotTaskParameters taskParams =
            taskRunner.parseTaskParams(taskParamsSerialized);
        assertEquals("test-space", taskParams.getSpaceId());
        assertEquals("test snapshot",
                     taskParams.getSnapshotProperties().get("description"));
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
            chronProvider.addContent(EasyMock.eq(spaceId),
                                     EasyMock.eq(Constants.SNAPSHOT_ID),
                                     EasyMock.eq("text/x-java-properties"),
                                     EasyMock.<Map<String, String>>isNull(),
                                     EasyMock.eq((long)props.length()),
                                     EasyMock.<String>anyObject(),
                                     EasyMock.<InputStream>anyObject()))
                .andReturn("success!");
        replayMocks();

        taskRunner.storeSnapshotProps(spaceId, props);
    }

    @Test
    public void testBuildTaskResult() {
        replayMocks();

        String snapshotId = "snapshot-id";
        String expectedResult = "{\"snapshotId\":\""+snapshotId+"\"}";
        String result = taskRunner.buildTaskResult(snapshotId);
        String cleanResult = result.replaceAll("\n", "")
                                   .replaceAll("\r", "")
                                   .replaceAll("\\s+", "");
        assertEquals(expectedResult, cleanResult);
    }

}
