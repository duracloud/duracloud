package org.duracloud.duradmin.spaces.controller;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.task.SnapshotTaskClient;
import org.duracloud.client.task.SnapshotTaskClientManager;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class SnapshotControllerTest {

    private ContentStore store;
    private ContentStoreManager storeManager;
    private HttpServletResponse response;
    private HttpServletRequest request;
    private PrintWriter writer;
    private Content content;
    private DuracloudUserDetailsService userDetailsService;
    private SnapshotTaskClient taskClient;
    private SnapshotTaskClientManager taskClientFactory;
    
    @Before
    public void setUp() throws Exception {
        store = EasyMock.createMock(ContentStore.class);
        storeManager = EasyMock.createMock(ContentStoreManager.class);
        response = EasyMock.createMock(HttpServletResponse.class);
        request = EasyMock.createMock(HttpServletRequest.class);
        writer = EasyMock.createMock(PrintWriter.class);
        content = EasyMock.createMock(Content.class);
        userDetailsService = EasyMock.createMock(DuracloudUserDetailsService.class);
        taskClient = EasyMock.createMock(SnapshotTaskClient.class);
        taskClientFactory = EasyMock.createMock(SnapshotTaskClientManager.class);
    }

    protected void setupGetContentStore() throws ContentStoreException {
        EasyMock.expect(storeManager.getContentStore(EasyMock.isA(String.class)))
                .andReturn(store);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(store,
                        storeManager,
                        response,
                        request,
                        writer,
                        content,
                        userDetailsService,
                        taskClient, 
                        taskClientFactory);
    }

    private void replay() {
        EasyMock.replay(store,
                        storeManager,
                        response,
                        request,
                        writer,
                        content,
                        userDetailsService,
                        taskClient,
                        taskClientFactory);
    }

    @Test
    public void testCreateSnapshot() throws Exception {
        setupGetContentStore();

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
        SnapshotController controller = createController();
        Assert.assertNull(controller.create(request,
                                        response,
                                        "spaceId",
                                        "storeId",
                                        "description"));
    }
    
    @Test
    public void testRestoreSnapshot() throws Exception {
        setupGetContentStore();

        EasyMock.expect(store.performTask(EasyMock.eq(SnapshotConstants.RESTORE_SNAPSHOT_TASK_NAME),
                                          EasyMock.isA(String.class)))
                .andReturn("response");


        setupUserDetails();

        replay();
        SnapshotController controller = createController();
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
        setupGetContentStore();

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
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.get("storeId", "spaceId"));
    }
    
    @Test
    public void testGetRestoreBySnaphshotId() throws Exception{
        
        setupGetContentStore();

        EasyMock.expect(store.performTask(EasyMock.eq("get-restore"),
                                         EasyMock.isA(String.class)))
                .andReturn("results");
        
        replay();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.getRestore("store-id", "snapshot-id"));
    }

    @Test
    public void testGetRestore() throws Exception{
        
        setupGetContentStore();

        EasyMock.expect(store.performTask(EasyMock.eq("get-restore"),
                                         EasyMock.isA(String.class)))
                .andReturn("results");
        
        replay();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.getRestore("store-id", 1000l));
    }
    
    @Test
    public void testGetContent() throws Exception{

        String storeId = "store-id";
        String snapshotId = "snapshot-id";
        Integer page = 0;
        String prefix = "prefix";
        
        List<SnapshotContentItem> items = new ArrayList<>();
        for(int i = 0; i < 200; i++ ) {
            SnapshotContentItem item = new SnapshotContentItem();
            item.setContentId("content-id"+ i);
            item.setContentProperties(new HashMap<String,String>());
            items.add(item);
        }
        
        EasyMock.expect(this.taskClientFactory.get(storeId))
                .andReturn(taskClient);
        EasyMock.expect(taskClient.getSnapshotContents(page, 200)).andReturn(items);
        replay();
        SnapshotController controller = createController();
        ModelAndView mav = controller.getContent(storeId, snapshotId, page, prefix);
        Map<String,Object> model = mav.getModel();
        Assert.assertEquals(model.get("storeId"), storeId);
        Assert.assertEquals(model.get("snapshotId"), snapshotId);
        Assert.assertEquals(model.get("prefix"), prefix);
        Assert.assertEquals(model.get("page"),page);
        Assert.assertEquals(model.get("nextPage"),page+1);
    }
        

    protected SnapshotController createController() {
        return new SnapshotController(storeManager,
                                      userDetailsService,
                                      taskClientFactory);
    }

}
