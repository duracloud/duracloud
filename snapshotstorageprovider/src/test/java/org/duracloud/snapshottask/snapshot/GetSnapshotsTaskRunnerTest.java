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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.duracloud.account.db.model.Role;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.dto.bridge.GetSnapshotListBridgeResult;
import org.duracloud.snapshot.dto.task.GetSnapshotListTaskResult;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Bill Branan
 *         Date: 7/29/14
 */
@RunWith(EasyMockRunner.class)
public class GetSnapshotsTaskRunnerTest extends EasyMockSupport {

    @Mock(name="RestHttpHelper")
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
        
        this.storageProvider = createMock("StorageProvider", StorageProvider.class);
        
        taskRunner = new GetSnapshotsTaskRunner(dcHost, dcStoreId, bridgeHost, bridgePort,
                                                bridgeUser, bridgePass, storageProvider){
            @Override
            protected RestHttpHelper
                      createRestHelper() {
                return restHelper;
            }
        };
    }

    @After
    public void tearDown(){
        verifyAll();
    }

    @Test
    public void testBuildSnapshotURL() {
        replayAll();
        String snapshotUrl = taskRunner.buildBridgeURL();
        String expectedUrl = "http://"+ bridgeHost + ":" + bridgePort +
                             "/bridge/snapshot?host="+ dcHost + "&storeId=" + dcStoreId;
        assertEquals(expectedUrl, snapshotUrl);
    }

    @Test
    public void testPerformWithAdminRole() throws Exception {
        testPerformWithRole(Role.ROLE_ADMIN);
    }

    @Test
    public void testPerformWithUserRole() throws Exception {
        testPerformWithRole(Role.ROLE_USER);
    }

    private void testPerformWithRole(Role role) throws Exception {
        String storeId = "store-id";
        String spaceId = "space-id";
        String nonExistentSpace = "non-existent-space-id";
        SnapshotSummary summary1 =
            new SnapshotSummary("id-1", SnapshotStatus.SNAPSHOT_COMPLETE, "desc-1", storeId, spaceId);
        SnapshotSummary summary2 =
            new SnapshotSummary("id-2", SnapshotStatus.SNAPSHOT_COMPLETE, "desc-1", storeId, nonExistentSpace);
        
        List<SnapshotSummary> summaries = new ArrayList<>();
        summaries.add(summary1);
        summaries.add(summary2);

        SecurityContext context = createMock(SecurityContext.class);
        Authentication authentication = createMock(Authentication.class);
        @SuppressWarnings("rawtypes")
        Collection authorities = Arrays.asList(new SimpleGrantedAuthority(role.name()));
        EasyMock.expect(authentication.getAuthorities())
                .andReturn(authorities);

        EasyMock.expect(context.getAuthentication()).andReturn(authentication);
        SecurityContextHolder.setContext(context);

        GetSnapshotListBridgeResult bridgeResult =
            new GetSnapshotListBridgeResult(summaries);
        InputStream resultStream =
            IOUtil.writeStringToStream(bridgeResult.serialize());

        RestHttpHelper.HttpResponse response =
            RestHttpHelper.HttpResponse.buildMock(200, null, resultStream);
        EasyMock.expect(restHelper.get(EasyMock.isA(String.class)))
                .andReturn(response);

        if(!role.equals(Role.ROLE_ADMIN)){
           EasyMock.expect(this.storageProvider.getSpaces()).andReturn(Arrays.asList(spaceId).iterator());
        }
        
        replayAll();
        String result = taskRunner.performTask(null);

        GetSnapshotListTaskResult taskResult =
            GetSnapshotListTaskResult.deserialize(result);
        List<SnapshotSummary> summaryResults = taskResult.getSnapshots();
        if(role.equals(Role.ROLE_ADMIN)){
            assertEquals(2, summaryResults.size());
        }else{
            assertEquals(1, summaryResults.size());
        }

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
        
        replayAll();
        
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

        replayAll();
        try {
            taskRunner.callBridge(restHelper, bridgeURL);
            fail("Exception expected on 500 response");
        } catch(TaskException e) {
        }
    }

}
