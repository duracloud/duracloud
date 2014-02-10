package org.duracloud.duradmin.spaces.controller;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.domain.Content;
import org.duracloud.error.NotFoundException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

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

    @Before
    public void setUp() throws Exception {
        store = EasyMock.createMock(ContentStore.class);
        storeManager = EasyMock.createMock(ContentStoreManager.class);
        response = EasyMock.createMock(HttpServletResponse.class);
        request = EasyMock.createMock(HttpServletRequest.class);
        writer = EasyMock.createMock(PrintWriter.class);
        content = EasyMock.createMock(Content.class);
        EasyMock.expect(storeManager.getContentStore(EasyMock.isA(String.class)))
                .andReturn(store);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(store, storeManager, response, request, writer, content);
    }

    private void replay() {
        EasyMock.replay(store, storeManager, response, request, writer, content);
    }

    @Test
    public void testPost() throws Exception {
        EasyMock.expect(store.getContentProperties(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
                .andThrow(new NotFoundException("not found"));
        EasyMock.expect(store.performTask(EasyMock.isA(String.class),
                                          EasyMock.isA(String.class)))
                .andReturn("response");

        response.setStatus(202);
        response.setHeader(EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall();
        EasyMock.expect(response.getWriter()).andReturn(writer);
        writer.write(EasyMock.isA(String.class));
        EasyMock.expectLastCall();
        replay();
        SnapshotController controller =
            new SnapshotController(storeManager);
        Assert.isNull(controller.create(request,
                                        response,
                                        "spaceId",
                                        "storeId",
                                        "description"));
    }

    @Test
    public void testGet() throws Exception{
        EasyMock.expect(store.getContent(EasyMock.isA(String.class), EasyMock.isA(String.class))).andReturn(content);
        
        Properties props = new Properties();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        props.store(os, null);
        EasyMock.expect(content.getStream())
                .andReturn(new ByteArrayInputStream(os.toByteArray()));
        replay();
        SnapshotController controller =
            new SnapshotController(storeManager);
        Assert.notNull(controller.get("storeId", "spaceId"));
    }

}
