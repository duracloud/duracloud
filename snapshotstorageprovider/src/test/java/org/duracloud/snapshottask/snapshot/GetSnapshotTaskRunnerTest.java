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
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.bridge.GetSnapshotBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotTaskResult;
import org.duracloud.storage.error.TaskException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 7/29/14
 */
public class GetSnapshotTaskRunnerTest {

    private RestHttpHelper restHelper;
    private GetSnapshotTaskRunner taskRunner;

    private String snapshotId = "snapshot-id";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner = new GetSnapshotTaskRunner(bridgeHost, bridgePort,
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

        String snapshotUrl = taskRunner.buildBridgeURL(snapshotId);
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                             "/bridge/snapshot/" + snapshotId;
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String bridgeURL = "bridge-url";
        SnapshotStatus status = SnapshotStatus.TRANSFERRING_FROM_DURACLOUD;
        String description = "description";

        GetSnapshotBridgeResult bridgeResult =
            new GetSnapshotBridgeResult();
        bridgeResult.setDescription(description);
        bridgeResult.setStatus(status);
        
        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            new RestHttpHelper.HttpResponse(200, null, null, resultStream);
        EasyMock.expect(restHelper.get(bridgeURL))
                .andReturn(response);

        replayMocks();

        String callResult = taskRunner.callBridge(restHelper, bridgeURL);

        GetSnapshotTaskResult taskResult =
            GetSnapshotTaskResult.deserialize(callResult);
        assertEquals(status, taskResult.getStatus());
        assertEquals(description, taskResult.getDescription());
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
