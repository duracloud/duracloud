/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.spaces.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.task.SnapshotTaskClient;
import org.duracloud.client.task.SnapshotTaskClientManager;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.duracloud.snapshot.dto.SnapshotHistoryItem;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.GetRestoreTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotContentsTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotHistoryTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotListTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.RequestRestoreSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.RestoreSnapshotTaskResult;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Daniel Bernstein
 */
@RunWith(EasyMockRunner.class)
public class SnapshotControllerTest extends EasyMockSupport {

    @Mock
    private ContentStore store;

    @Mock
    private ContentStoreManager storeManager;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private PrintWriter writer;

    @Mock
    private Content content;

    @Mock
    private DuracloudUserDetailsService userDetailsService;

    @Mock
    private SnapshotTaskClient taskClient;

    @Mock
    private SnapshotTaskClientManager taskClientFactory;

    private String username = "username";
    private String userEmail = "user@example.com";
    private String storeId = "storeId";
    private String snapshotId = "snapshot-id";

    @Before
    public void setUp() throws Exception {

    }

    protected void setupGetContentStore() throws ContentStoreException {
        EasyMock.expect(storeManager.getContentStore(EasyMock.isA(String.class)))
                .andReturn(store);
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    @Test
    public void testCreateSnapshot() throws Exception {

        String spaceId = "spaceId";
        String description = "description";

        setupGetContentStore();
        setupGetTaskClient(storeId);
        EasyMock.expect(store.getSpaceProperties(EasyMock.isA(String.class)))
                .andReturn(new HashMap<String, String>());

        EasyMock.expect(taskClient.createSnapshot(spaceId,
                                                  description,
                                                  userEmail))
                .andReturn(new CreateSnapshotTaskResult());

        response.setStatus(HttpStatus.SC_ACCEPTED);
        EasyMock.expectLastCall().once();
        response.setHeader("Content-Type", "application/json");
        setupUserDetails();

        replayAll();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.create(request,
                                               response,
                                               "spaceId",
                                               storeId,
                                               "description"));
    }

    @Test
    public void testRestoreSnapshot() throws Exception {

        EasyMock.expect(taskClient.restoreSnapshot(snapshotId, userEmail))
                .andReturn(new RestoreSnapshotTaskResult());
        setupUserDetails();
        setupGetTaskClient(storeId);
        replayAll();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.restore(request, storeId, snapshotId));
    }

    @Test
    public void testRequestRestoreSnapshot() throws Exception {

        EasyMock.expect(taskClient.requestRestoreSnapshot(snapshotId, userEmail))
                .andReturn(new RequestRestoreSnapshotTaskResult());
        setupUserDetails();
        setupGetTaskClient(storeId);
        replayAll();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.requestRestore(request, storeId, snapshotId));
    }

    protected void setupUserDetails() {
        EasyMock.expect(request.getUserPrincipal()).andReturn(new Principal() {
            @Override
            public String getName() {
                return username;
            }
        });
        SecurityUserBean userBean =
            new SecurityUserBean(username,
                                 "password",
                                 userEmail,
                                 "",
                                 true,
                                 true,
                                 true,
                                 true,
                                 null,
                                 null);
        EasyMock.expect(userDetailsService.getUserByUsername(username))
                .andReturn(userBean);
    }

    @Test
    public void testGetSnapshot() throws Exception {
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
        replayAll();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.get("storeId", "spaceId"));
    }

    @Test
    public void testGetHistory() throws Exception {
        Integer page = 0;

        Date historyDate = new Date();
        long totalCount = 201;
        List<SnapshotHistoryItem> items = new ArrayList<>();
        for (int i = 0; i < totalCount - 1; i++) {
            SnapshotHistoryItem item = new SnapshotHistoryItem();
            item.setHistory("history" + i);
            item.setHistoryDate(new Date(historyDate.getTime() + i));
            items.add(item);
        }
        GetSnapshotHistoryTaskResult result =
            new GetSnapshotHistoryTaskResult();
        result.setHistoryItems(items);
        result.setTotalCount(totalCount);

        setupGetTaskClient(storeId);
        EasyMock.expect(taskClient.getSnapshotHistory(snapshotId, page, 200))
                .andReturn(result);

        HttpServletResponse response = createMock(HttpServletResponse.class);

        File file = File.createTempFile("test", "json");
        file.deleteOnExit();
        PrintWriter writer = new PrintWriter(new FileOutputStream(file));
        EasyMock.expect(response.getWriter()).andReturn(writer);
        replayAll();

        SnapshotController controller = createController();
        ModelAndView mav =
            controller.getHistory(storeId, snapshotId, page, false, response);
        Assert.assertNull(mav);

        JsonFactory factory = new JsonFactory();
        JsonParser jParser =
            factory.createJsonParser(new FileInputStream(file));
        int topLevelPropCount = 0;
        while (jParser.nextToken() != JsonToken.END_OBJECT) {

            String fieldname = jParser.getCurrentName();
            if ("storeId".equals(fieldname)) {
                jParser.nextToken();
                Assert.assertEquals(storeId, jParser.getText());
            } else if ("snapshotId".equals(fieldname)) {
                jParser.nextToken();
                Assert.assertEquals(snapshotId, jParser.getText());
            } else if ("page".equals(fieldname)) {
                jParser.nextToken();
                Assert.assertEquals(page.intValue(), jParser.getIntValue());
            } else if ("nextPage".equals(fieldname)) {
                jParser.nextToken();
                Assert.assertEquals(page.intValue() + 1, jParser.getIntValue());
            } else if ("totalCount".equals(fieldname)) {
                jParser.nextToken();
                Assert.assertEquals(totalCount, jParser.getLongValue());
            } else if ("historyItems".equals(fieldname)) {
                jParser.nextToken(); // current token is "[", move next
                int i = 0;
                while (jParser.nextToken() != JsonToken.END_ARRAY) {
                    while (jParser.nextToken() != JsonToken.END_OBJECT) {
                        String itemFieldName = jParser.getCurrentName();
                        if ("historyDate".equals(itemFieldName)) {
                            jParser.nextToken();
                            Assert.assertEquals(historyDate.getTime() + i,
                                                jParser.getLongValue());
                        } else if ("history".equals(fieldname)) {
                            jParser.nextToken();
                            Assert.assertEquals("history" + i,
                                                jParser.getText());
                        }
                    }

                    i++;
                }
            }

            topLevelPropCount++;

        }

        Assert.assertEquals(6, topLevelPropCount - 1);
        jParser.close();
    }

    @Test
    public void testGetSnapshots() throws Exception {

        setupGetTaskClient(storeId);
        EasyMock.expect(this.taskClient.getSnapshots())
                .andReturn(new GetSnapshotListTaskResult());
        replayAll();

        SnapshotController controller = createController();

        Assert.assertNotNull(controller.getSnapshotList(storeId));
    }

    @Test
    public void testGetSnapshotFromTaskClient() throws Exception {
        setupGetTaskClient(storeId);
        EasyMock.expect(this.taskClient.getSnapshot(snapshotId))
                .andReturn(new GetSnapshotTaskResult());
        replayAll();

        SnapshotController controller = createController();

        Assert.assertNotNull(controller.getSnapshot(storeId, snapshotId));
    }

    @Test
    public void testGetRestoreBySnaphshotId() throws Exception {
        setupGetTaskClient(storeId);
        EasyMock.expect(this.taskClient.getRestoreBySnapshot(snapshotId))
                .andReturn(new GetRestoreTaskResult());
        replayAll();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.getRestore(storeId, snapshotId));
    }

    @Test
    public void testGetRestore() throws Exception {
        setupGetTaskClient(storeId);
        String restoreId = "restore-id";
        EasyMock.expect(this.taskClient.getRestore(restoreId))
                .andReturn(new GetRestoreTaskResult());

        replayAll();
        SnapshotController controller = createController();
        Assert.assertNotNull(controller.getRestoreByRestoreId(storeId,
                                                              restoreId));
    }

    @Test
    public void testGetContent() throws Exception {
        Integer page = 0;
        String prefix = "prefix";

        List<SnapshotContentItem> items = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            SnapshotContentItem item = new SnapshotContentItem();
            item.setContentId("content-id" + i);
            item.setContentProperties(new HashMap<String, String>());
            items.add(item);
        }
        GetSnapshotContentsTaskResult result =
            new GetSnapshotContentsTaskResult();
        result.setContentItems(items);

        setupGetTaskClient(storeId);
        EasyMock.expect(taskClient.getSnapshotContents(snapshotId,
                                                       page,
                                                       200,
                                                       prefix))
                .andReturn(result);

        replayAll();

        SnapshotController controller = createController();
        ModelAndView mav =
            controller.getContent(storeId, snapshotId, page, prefix);
        Map<String, Object> model = mav.getModel();
        Assert.assertEquals(model.get("storeId"), storeId);
        Assert.assertEquals(model.get("snapshotId"), snapshotId);
        Assert.assertEquals(model.get("prefix"), prefix);
        Assert.assertEquals(model.get("page"), page);
        Assert.assertEquals(model.get("nextPage"), page + 1);
    }

    protected void setupGetTaskClient(String storeId)
        throws ContentStoreException {
        EasyMock.expect(this.taskClientFactory.get(storeId))
                .andReturn(taskClient);
    }

    protected SnapshotController createController() {
        return new SnapshotController(storeManager,
                                      userDetailsService,
                                      taskClientFactory);
    }

}
