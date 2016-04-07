/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.dto.bridge.GetSnapshotListBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotListTaskResult;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 *         Date: 7/29/14
 */
public class GetSnapshotsTaskRunnerTest {

    private RestHttpHelper restHelper;
    private GetSnapshotsTaskRunner taskRunner;
    private StorageProvider storageProvider;
    private String dcHost = "dc-host";
    private String dcStoreId = "dc-store-id";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        storageProvider = EasyMock.createMock("StorageProvider", StorageProvider.class);
        taskRunner = new GetSnapshotsTaskRunner(dcHost, dcStoreId, bridgeHost, bridgePort,
                                                bridgeUser, bridgePass, storageProvider);
    }

    private void replayMocks() {
        EasyMock.replay(restHelper, storageProvider);
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
                             "/bridge/snapshot?host="+ dcHost + "&storeId=" + dcStoreId;
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String bridgeURL = "bridge-url";
        String storeId = "store-id";
        String spaceId = "space-id";
        SnapshotSummary summary1 =
            new SnapshotSummary("id-1", SnapshotStatus.SNAPSHOT_COMPLETE, "desc-1", storeId, spaceId);
        List<SnapshotSummary> summaries = new ArrayList<>();
        summaries.add(summary1);

        GetSnapshotListBridgeResult bridgeResult =
            new GetSnapshotListBridgeResult(summaries);
        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(200, null, resultStream);
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
