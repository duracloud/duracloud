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

import org.apache.http.HttpStatus;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.bridge.RestartSnapshotBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotTaskResult;
import org.duracloud.storage.error.TaskException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Bernstein
 *         Date: 08/10/15
 */
public class RestartSnapshotTaskRunnerTest {

    private RestHttpHelper restHelper;
    private RestartSnapshotTaskRunner taskRunner;

    private String snapshotId = "snapshot-id";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner = new RestartSnapshotTaskRunner(bridgeHost, bridgePort,
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
                             "/bridge/snapshot/" + snapshotId + "/restart";
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testCallBridgeSuccess() throws Exception {
        String bridgeURL = "bridge-url";

        SnapshotStatus status = SnapshotStatus.INITIALIZED;
        String description = "description";

        RestartSnapshotBridgeResult bridgeResult =
            new RestartSnapshotBridgeResult();
        bridgeResult.setDescription(description);
        bridgeResult.setStatus(status);
        
        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(HttpStatus.SC_ACCEPTED, null, resultStream);
        EasyMock.expect(restHelper.post(bridgeURL, null, null))
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
            RestHttpHelper.HttpResponse.buildMock(500, null, resultStream);
        EasyMock.expect(restHelper.post(bridgeURL, null, null))
                .andReturn(response);

        replayMocks();

        try {
            taskRunner.callBridge(restHelper, bridgeURL);
            fail("Exception expected on 500 response");
        } catch(TaskException e) {
        }
    }

}
