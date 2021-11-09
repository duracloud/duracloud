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

import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.bridge.GetSnapshotTotalsBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotsTotalsTaskParameters;
import org.duracloud.snapshot.dto.task.GetSnapshotsTotalsTaskResult;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Nicholas Woodward
 * Date: 8/5/21
 */
@RunWith(EasyMockRunner.class)
public class GetSnapshotsTotalsTaskRunnerTest extends EasyMockSupport {

    @Mock(name = "RestHttpHelper")
    private RestHttpHelper restHelper;
    private GetSnapshotsTotalsTaskRunner taskRunner;
    private StorageProvider storageProvider;
    private String dcHost = "dc-host";
    private String dcStoreId = "dc-store-id";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";
    private String status = "SNAPSHOT_COMPLETE";

    @Before
    public void setup() {

        this.storageProvider = createMock("StorageProvider", StorageProvider.class);

        taskRunner = new GetSnapshotsTotalsTaskRunner(dcHost, dcStoreId, bridgeHost, bridgePort,
                                                bridgeUser, bridgePass, storageProvider) {
            @Override
            protected RestHttpHelper createRestHelper() {
                return restHelper;
            }
        };
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testBuildSnapshotURL() {
        replayAll();

        GetSnapshotsTotalsTaskParameters taskParams = new GetSnapshotsTotalsTaskParameters();
        taskParams.setStatus(status);

        String snapshotUrl = taskRunner.buildBridgeURL(taskParams);

        String expectedUrl = "http://" + bridgeHost + ":" + bridgePort +
                             "/bridge/snapshot/total?host=" + dcHost + "&storeId=" + dcStoreId +
                             "&status=" + status;
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String bridgeURL = "bridge-url";
        String storeId = "store-id";
        String spaceId = "space-id";

        long totalCount = 1L;
        long totalSize = 100L;
        long totalFiles = 1000L;

        GetSnapshotTotalsBridgeResult bridgeResult =
            new GetSnapshotTotalsBridgeResult(totalCount, totalSize, totalFiles);
        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(200, null, resultStream);
        EasyMock.expect(restHelper.get(bridgeURL))
                .andReturn(response);

        replayAll();

        String callResult = taskRunner.callBridge(restHelper, bridgeURL);

        GetSnapshotsTotalsTaskResult taskResult =
            GetSnapshotsTotalsTaskResult.deserialize(callResult);

        assertEquals(totalCount, taskResult.getTotalCount());
        assertEquals(totalSize, taskResult.getTotalSize());
        assertEquals(totalFiles, taskResult.getTotalFiles());
    }

    @Test
    public void testCallBridgeFailure() throws Exception {
        String bridgeURL = "bridge-url";

        InputStream resultStream = IOUtil.writeStringToStream("Error");
        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(500, null, resultStream);
        EasyMock.expect(restHelper.get(bridgeURL))
                .andReturn(response);

        replayAll();
        try {
            taskRunner.callBridge(restHelper, bridgeURL);
            fail("Exception expected on 500 response");
        } catch (TaskException e) {
            // Expected exception
        }
    }
}
