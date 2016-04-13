/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.SnapshotHistoryItem;
import org.duracloud.snapshot.dto.bridge.GetSnapshotHistoryBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotHistoryTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gad Krumholz
 *         Date: 7/02/15
 */
public class GetSnapshotHistoryTaskRunnerTest {

    private RestHttpHelper restHelper;
    private GetSnapshotHistoryTaskRunner taskRunner;

    private String snapshotId = "snapshot-id";
    private int pageNumber = 42;
    private int pageSize = 100;

    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner = new GetSnapshotHistoryTaskRunner(bridgeHost, bridgePort,
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
        GetSnapshotHistoryTaskParameters taskParams =
            new GetSnapshotHistoryTaskParameters();
        taskParams.setSnapshotId(snapshotId);
        taskParams.setPageNumber(pageNumber);
        taskParams.setPageSize(pageSize);
        

        String url = taskRunner.buildBridgeURL(taskParams);
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                             "/bridge/snapshot/" + snapshotId +
                             "/history?page=" + pageNumber +
                             "&pageSize=" + pageSize;
        assertEquals(expectedUrl, url);

        // With some unusual parameters
        taskParams = new GetSnapshotHistoryTaskParameters();
        taskParams.setSnapshotId(snapshotId);
        taskParams.setPageNumber(-500);
        taskParams.setPageSize(100000000);

        url = taskRunner.buildBridgeURL(taskParams);
        expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                      "/bridge/snapshot/" + snapshotId +
                      "/history?page=0&pageSize=1000";
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String bridgeURL = "bridge-url";
        String history = "history";
        Date historyDate = new Date();
        
        SnapshotHistoryItem historyItem = new SnapshotHistoryItem();
        historyItem.setHistory(history);
        historyItem.setHistoryDate(historyDate);
        List<SnapshotHistoryItem> historyItems = new ArrayList<>();
        historyItems.add(historyItem);
        GetSnapshotHistoryBridgeResult bridgeResult =
            new GetSnapshotHistoryBridgeResult();
        bridgeResult.setHistoryItems(historyItems);

        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(200, null, resultStream);
        EasyMock.expect(restHelper.get(bridgeURL))
                .andReturn(response);

        replayMocks();

        String callResult = taskRunner.callBridge(restHelper, bridgeURL);

        GetSnapshotHistoryBridgeResult taskResult =
                GetSnapshotHistoryBridgeResult.deserialize(callResult);
        SnapshotHistoryItem item = taskResult.getHistoryItems().get(0);
        assertEquals(history, item.getHistory());
        assertEquals(historyDate, item.getHistoryDate());
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
