package org.duracloud.duradmin.spaces.controller;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.domain.Content;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.snapshottask.snapshot.RestoreSnapshotTaskRunner;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class SnapshotControllerTest {

    ContentStore store;
    ContentStoreManager storeManager;
    HttpServletResponse response;
    HttpServletRequest request;
    PrintWriter writer;
    Content content;
    DuracloudUserDetailsService userDetailsService;

    @Before
    public void setUp() throws Exception {
        store = EasyMock.createMock(ContentStore.class);
        storeManager = EasyMock.createMock(ContentStoreManager.class);
        response = EasyMock.createMock(HttpServletResponse.class);
        request = EasyMock.createMock(HttpServletRequest.class);
        writer = EasyMock.createMock(PrintWriter.class);
        content = EasyMock.createMock(Content.class);
        userDetailsService = EasyMock.createMock(DuracloudUserDetailsService.class);
        EasyMock.expect(storeManager.getContentStore(EasyMock.isA(String.class)))
                .andReturn(store);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(store, storeManager, response, request, writer,
                        content, userDetailsService);
    }

    private void replay() {
        EasyMock.replay(store, storeManager, response, request, writer,
                        content, userDetailsService);
    }

    @Test
    public void testCreateSnapshot() throws Exception {
        EasyMock.expect(store.contentExists(EasyMock.isA(String.class),
                                            EasyMock.isA(String.class)))
                .andReturn(false);
        EasyMock.expect(store.performTask(EasyMock.isA(String.class),
                                          EasyMock.isA(String.class)))
                .andReturn("response");

        response.setStatus(202);
        EasyMock.expectLastCall();
        response.setHeader(EasyMock.isA(String.class),
                           EasyMock.isA(String.class));
        EasyMock.expectLastCall();
        EasyMock.expect(response.getWriter()).andReturn(writer);
        writer.write(EasyMock.isA(String.class));
        EasyMock.expectLastCall();

        setupUserDetails();

        replay();
        SnapshotController controller =
            new SnapshotController(storeManager, userDetailsService);
        Assert.assertNull(controller.create(request,
                                        response,
                                        "spaceId",
                                        "storeId",
                                        "description"));
    }
    
    @Test
    public void testRestoreSnapshot() throws Exception {
        EasyMock.expect(store.performTask(EasyMock.eq(RestoreSnapshotTaskRunner.TASK_NAME),
                                          EasyMock.isA(String.class)))
                .andReturn("response");


        setupUserDetails();

        replay();
        SnapshotController controller =
            new SnapshotController(storeManager, userDetailsService);
        Assert.assertNotNull(controller.restore(request, "store-id", "snapshot-id"));
    }

    protected void setupUserDetails() {
        final String username = "mjackson";
        EasyMock.expect(request.getUserPrincipal()).andReturn(new Principal() {
            @Override
            public String getName() {
                return username;
            }
        });
        SecurityUserBean userBean =
            new SecurityUserBean(username, "password", "email@email.com", true,
                                 true, true, true, null, null);
        EasyMock.expect(userDetailsService.getUserByUsername(username))
                .andReturn(userBean);
    }

    @Test
    public void testGetSnapshot() throws Exception{
        EasyMock.expect(store.contentExists(EasyMock.isA(String.class),
                                            EasyMock.isA(String.class)))
                .andReturn(true);
        EasyMock.expect(store.getContent(EasyMock.isA(String.class),
                                         EasyMock.isA(String.class)))
                .andReturn(content);
        
        Properties props = new Properties();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        props.store(os, null);
        EasyMock.expect(content.getStream())
                .andReturn(new ByteArrayInputStream(os.toByteArray()));
        replay();
        SnapshotController controller =
            new SnapshotController(storeManager, userDetailsService);
        Assert.assertNotNull(controller.get("storeId", "spaceId"));
    }
    
    @Test
    public void testGetRestoreBySnaphshotId() throws Exception{
        EasyMock.expect(store.performTask(EasyMock.eq("get-restore"),
                                         EasyMock.isA(String.class)))
                .andReturn("results");
        
        replay();
        SnapshotController controller =
            new SnapshotController(storeManager, userDetailsService);
        Assert.assertNotNull(controller.getRestore("store-id", "snapshot-id"));
    }

    @Test
    public void testGetRestore() throws Exception{
        EasyMock.expect(store.performTask(EasyMock.eq("get-restore"),
                                         EasyMock.isA(String.class)))
                .andReturn("results");
        
        replay();
        SnapshotController controller =
            new SnapshotController(storeManager, userDetailsService);
        Assert.assertNotNull(controller.getRestore("store-id", 1000l));
    }

}
