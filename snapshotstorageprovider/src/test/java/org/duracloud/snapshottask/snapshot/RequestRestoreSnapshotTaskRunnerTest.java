/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.dto.bridge.CreateRestoreBridgeResult;
import org.duracloud.snapshot.dto.task.RestoreSnapshotTaskResult;
import org.duracloud.storage.error.TaskException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Bernstein
 *         Date: 11/04/15
 */
public class RequestRestoreSnapshotTaskRunnerTest {

    private RestHttpHelper restHelper;
    private RequestRestoreSnapshotTaskRunner taskRunner;

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
        restHelper = EasyMock.createMock("RestHttpHelper", RestHttpHelper.class);
        taskRunner =
            new RequestRestoreSnapshotTaskRunner(dcHost, dcPort, dcStoreId, dcSnapshotUser,
                                         bridgeHost, bridgePort, bridgeUser,
                                         bridgePass);
    }

    private void replayMocks() {
        EasyMock.replay( restHelper);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(restHelper);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("request-restore-snapshot", taskRunner.getName());
    }

    @Test
    public void testBuildBridgeURL() {
        replayMocks();

        String bridgeUrl = taskRunner.buildBridgeURL();
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                             "/bridge/restore/request";
        assertEquals(expectedUrl, bridgeUrl);
    }

    @Test
    public void testBuildBridgeBody() {
        replayMocks();

        String snapshotId = "snapshot-id";
        String userEmail = "user-email";

        String result =
            taskRunner.buildBridgeBody(snapshotId, userEmail);
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"host\":\""+dcHost+"\""));
        assertThat(cleanResult, containsString("\"port\":\""+dcPort+"\""));
        assertThat(cleanResult, containsString("\"storeId\":\""+dcStoreId+"\""));
        assertThat(cleanResult, containsString("\"snapshotId\":\""+snapshotId+"\""));
        assertThat(cleanResult, containsString("\"userEmail\":\""+userEmail+"\""));
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
            new RestHttpHelper.HttpResponse(201, null, null, resultStream);
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
            new RestHttpHelper.HttpResponse(500, null, null, resultStream);
                Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        EasyMock.expect(restHelper.put(snapshotURL, snapshotBody, headers))
                .andReturn(response);

        replayMocks();

        try {
            taskRunner.callBridge(restHelper, snapshotURL, snapshotBody);
            fail("Exception expected on 500 response");
        } catch(TaskException e) {
        }
    }

 }
