/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.error.ContentStoreException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ContentStoreProviderTest {

    private ContentStoreProvider contentStoreProvider;
    private ContentStoreManager contentStoreManager;
    private ContentStore contentStore;

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
        contentStoreManager = EasyMock.createMock("ContentStoreManager",
                                                  ContentStoreManager.class);

        ContentStoreSelector contentStoreSelector = new ContentStoreSelector();
        this.contentStoreProvider =
            new ContentStoreProvider(contentStoreManager, contentStoreSelector);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore, contentStoreManager);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore, contentStoreManager);
    }

    @Test
    public void testGetContentStoreSelector() throws ContentStoreException {
        String storeId = "0";
        createGetContentStoreSelectorMocks(storeId);
        replayMocks();

        String selectedId =
            this.contentStoreProvider.getSelectedContentStoreId();
        Assert.assertNotNull(selectedId);

        String newSelectedId = "new-store-id";
        this.contentStoreProvider.setSelectedContentStoreId(newSelectedId);

        selectedId = this.contentStoreProvider.getSelectedContentStoreId();
        Assert.assertNotNull(selectedId);
        Assert.assertEquals(newSelectedId, selectedId);
    }

    @Test
    public void testGetContentStore() throws Exception {
        String storeId = "1";
        createGetContentStoreMocks(storeId);
        replayMocks();
        Assert.assertNotNull(this.contentStoreProvider.getContentStore());
    }

    private void createGetContentStoreSelectorMocks(String storeId)
        throws ContentStoreException {
        EasyMock.expect(contentStoreManager.getPrimaryContentStore()).andReturn(
            contentStore);
        EasyMock.expect(contentStore.getStoreId()).andReturn(storeId);
    }

    private void createGetContentStoreMocks(String storeId)
        throws ContentStoreException {
        createGetContentStoreSelectorMocks(storeId);
        EasyMock.expect(contentStoreManager.getContentStore(storeId)).andReturn(
            contentStore);
    }

}
