/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.spaces.controller;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Jan 7, 2011
 */
public class ContentItemUploadTaskTest {

    private ContentItemUploadTask task;

    private ContentItem contentItem;
    private ContentStore contentStore;
    private InputStream stream;
    private String username;

    private String text = "hello";

    @Before
    public void setUp() throws Exception {
        contentItem = new ContentItem();
        contentItem.setSpaceId("space-id");
        contentItem.setContentId("content-id");
        contentItem.setContentMimetype("text/plain");

        contentStore = createMockContentStore();
        stream = IOUtils.toInputStream(text);
        username = "user-name";
    }

    private ContentStore createMockContentStore() throws ContentStoreException {
        ContentStore store = EasyMock.createMock("Store", ContentStore.class);
        EasyMock.expect(store.addContent(EasyMock.eq(contentItem.getSpaceId()),
                                         EasyMock.eq(contentItem.getContentId()),
                                         EasyMock.<InputStream>anyObject(),
                                         EasyMock.leq((long) text.length()),
                                         EasyMock.eq(contentItem.getContentMimetype()),
                                         EasyMock.<String>isNull(),
                                         EasyMock.<Map<String, String>>isNull()))
            .andReturn(null);

        EasyMock.replay(store);
        return store;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    @Test
    public void testExecute() throws Exception {
        task = new ContentItemUploadTask(contentItem,
                                         contentStore,
                                         stream,
                                         username);


        task.execute();

    }
}
