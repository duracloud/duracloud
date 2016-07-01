/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.duracloud.snapshot.dto.bridge.GetSnapshotContentBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotContentsTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 8/12/14
 */
public class GetSnapshotContentsTaskRunnerTest {

    private RestHttpHelper restHelper;
    private GetSnapshotContentsTaskRunner taskRunner;

    private String snapshotId = "snapshot-id";
    private int pageNumber = 42;
    private int pageSize = 100;
    private String prefix = "prefix";

    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner = new GetSnapshotContentsTaskRunner(bridgeHost, bridgePort,
                                                       bridgeUser, bridgePass);
    }

    private void replayMocks() {
        EasyMock.replay(restHelper);
    }

    @After
    public void tearDown(){
        EasyMock.verify(restHelper);
    }

    @Test
    public void testBuildSnapshotURL() {
        replayMocks();

        // With all valid parameters
        GetSnapshotContentsTaskParameters taskParams =
            new GetSnapshotContentsTaskParameters();
        taskParams.setSnapshotId(snapshotId);
        taskParams.setPageNumber(pageNumber);
        taskParams.setPageSize(pageSize);
        taskParams.setPrefix(prefix);

        String url = taskRunner.buildBridgeURL(taskParams);
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                             "/bridge/snapshot/" + snapshotId +
                             "/content?page=" + pageNumber +
                             "&pageSize=" + pageSize + "&prefix=" + prefix;
        assertEquals(expectedUrl, url);

        // With some unusual parameters
        taskParams = new GetSnapshotContentsTaskParameters();
        taskParams.setSnapshotId(snapshotId);
        taskParams.setPageNumber(-500);
        taskParams.setPageSize(100000000);
        taskParams.setPrefix(null);

        url = taskRunner.buildBridgeURL(taskParams);
        expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                      "/bridge/snapshot/" + snapshotId +
                      "/content?page=0&pageSize=1000&prefix=";
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String bridgeURL = "bridge-url";
        String contentId = "contentId";
        String propName = "prop-name";
        String propValue = "prop-value";

        Map<String, String> contentProps = new HashMap<>();
        contentProps.put(propName, propValue);
        SnapshotContentItem contentItem = new SnapshotContentItem();
        contentItem.setContentId(contentId);
        contentItem.setContentProperties(contentProps);
        List<SnapshotContentItem> contentItems = new ArrayList<>();
        contentItems.add(contentItem);
        GetSnapshotContentBridgeResult bridgeResult =
            new GetSnapshotContentBridgeResult();
        bridgeResult.setContentItems(contentItems);

        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(200, null, resultStream);
        EasyMock.expect(restHelper.get(bridgeURL))
                .andReturn(response);

        replayMocks();

        String callResult = taskRunner.callBridge(restHelper, bridgeURL);

        GetSnapshotContentBridgeResult taskResult =
            GetSnapshotContentBridgeResult.deserialize(callResult);
        SnapshotContentItem item = taskResult.getContentItems().get(0);
        assertEquals(contentId, item.getContentId());
        assertEquals(propValue, item.getContentProperties().get(propName));
    }

    @Test
    public void testCallBridgeFailure() throws Exception {
        String bridgeURL = "bridge-url";

        InputStream resultStream = IOUtil.writeStringToStream("Error");
        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(500, null, resultStream);
        EasyMock.expect(restHelper.get(bridgeURL))
                .andReturn(response);

        replayMocks();

        try {
            taskRunner.callBridge(restHelper, bridgeURL);
            fail("Exception expected on 500 response");
        } catch(TaskException e) {
        }
    }

}
