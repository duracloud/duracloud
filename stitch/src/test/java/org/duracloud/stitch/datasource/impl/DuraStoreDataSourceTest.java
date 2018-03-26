/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.datasource.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.stitch.error.DataSourceException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 * Date: 9/2/11
 */
public class DuraStoreDataSourceTest {

    private DuraStoreDataSource dataSource;
    private ContentStore store;

    private String spaceId = "spaceId";
    private String contentId = "contentId";

    @Before
    public void setUp() throws Exception {
        store = EasyMock.createMock("ContentStore", ContentStore.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(store);
    }

    private void replayMocks() {
        EasyMock.replay(store);
    }

    @Test
    public void testGetContent() throws Exception {
        createGetContentMocks(true);
        replayMocks();

        dataSource = new DuraStoreDataSource(store);
        dataSource.getContent(spaceId, contentId);
    }

    @Test
    public void testGetContentError() throws Exception {
        createGetContentMocks(false);
        replayMocks();

        dataSource = new DuraStoreDataSource(store);

        try {
            dataSource.getContent(spaceId, contentId);
            Assert.fail("exception expected");

        } catch (DataSourceException e) {
            Assert.assertNotNull(e);
        }
    }

    private void createGetContentMocks(boolean valid)
        throws ContentStoreException {
        if (valid) {
            Content content = new Content();
            EasyMock.expect(store.getContent(spaceId, contentId)).andReturn(
                content);

        } else {
            EasyMock.expect(store.getContent(spaceId, contentId))
                    .andThrow(new DataSourceException("canned-exception", null));
        }
    }
}
