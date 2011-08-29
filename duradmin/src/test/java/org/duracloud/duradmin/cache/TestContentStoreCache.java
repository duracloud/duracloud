/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.cache;

import org.duracloud.client.ContentStore;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestContentStoreCache {

    private ContentStoreCache cache;
    private ContentStore contentStore;

    @Before
    public void setUp() throws Exception {
        cache = new ContentStoreCache();
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);

        cache.setContentStore(contentStore);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testGetContentStoreCache() {
        replayMocks();
        Assert.assertNotNull(cache.getContentStore());
    }

    @Test
    public void testGetSpaces() throws Exception {
        List<String> spaces = new ArrayList<String>();
        EasyMock.expect(contentStore.getSpaces()).andReturn(spaces);
        replayMocks();
        
        Assert.assertNotNull(cache.getSpaces());
    }
}
