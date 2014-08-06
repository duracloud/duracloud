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
import org.duracloud.snapshot.dto.bridge.GetSnapshotListBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotListTaskResult;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.storage.error.TaskException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 7/29/14
 */
public class GetSnapshotsTaskRunnerTest {

    private RestHttpHelper restHelper;
    private GetSnapshotsTaskRunner taskRunner;

    private String dcHost = "dc-host";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner = new GetSnapshotsTaskRunner(dcHost, bridgeHost, bridgePort,
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

        String snapshotUrl = taskRunner.buildBridgeURL();
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                             "/bridge/snapshot?host="+ dcHost;
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String bridgeURL = "bridge-url";

        SnapshotSummary summary1 =
            new SnapshotSummary("id-1", SnapshotStatus.SNAPSHOT_COMPLETE, "desc-1");
        List<SnapshotSummary> summaries = new ArrayList<>();
        summaries.add(summary1);

        GetSnapshotListBridgeResult bridgeResult =
            new GetSnapshotListBridgeResult(summaries);
        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            new RestHttpHelper.HttpResponse(200, null, null, resultStream);
        EasyMock.expect(restHelper.get(bridgeURL))
                .andReturn(response);

        replayMocks();

        String callResult = taskRunner.callBridge(restHelper, bridgeURL);

        GetSnapshotListTaskResult taskResult =
            GetSnapshotListTaskResult.deserialize(callResult);
        List<SnapshotSummary> summaryResults = taskResult.getSnapshots();
        assertEquals(1, summaryResults.size());
        SnapshotSummary result = summaryResults.get(0);
        assertEquals(summary1.getSnapshotId(), result.getSnapshotId());
        assertEquals(summary1.getStatus(), result.getStatus());
        assertEquals(summary1.getDescription(), result.getDescription());
    }

    @Test
    public void testCallBridgeFailure() throws Exception {
        String bridgeURL = "bridge-url";

        InputStream resultStream = IOUtil.writeStringToStream("Error");
        RestHttpHelper.HttpResponse response =
            new RestHttpHelper.HttpResponse(500, null, null, resultStream);
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
