/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.error.ContentStoreException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ContentStoreSelectorTest {

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
    public void testSelectStore() throws Exception {
        createSelectStoreMocks();
        replayMocks();

        String storeId = this.contentStoreProvider.getSelectedContentStoreId();
        Assert.assertNotNull(storeId);
        for (ContentStore store : getContentStores()) {
            String sId = store.getStoreId();
            if (sId != storeId) {
                Assert.assertNotSame(sId,
                                     contentStoreProvider.getContentStore()
                                         .getStoreId());
                this.contentStoreProvider.setSelectedContentStoreId(sId);
                Assert.assertSame(sId,
                                  contentStoreProvider.getContentStore()
                                      .getStoreId());
                break;
            }
        }
    }

    private void createSelectStoreMocks() throws ContentStoreException {
        EasyMock.expect(contentStoreManager.getPrimaryContentStore()).andReturn(
            contentStore);

        String storeId = "0";
        EasyMock.expect(contentStore.getStoreId()).andReturn(storeId).times(2);

        Map<String, ContentStore> storesMap =
            new HashMap<String, ContentStore>();
        storesMap.put(storeId, contentStore);
        EasyMock.expect(contentStoreManager.getContentStores()).andReturn(
            storesMap);
    }

    private Collection<ContentStore> getContentStores()
        throws ContentStoreException {
        Collection<ContentStore> stores =
            contentStoreProvider.getContentStores();
        Assert.assertNotNull(stores);
        Assert.assertTrue(stores.size() > 0);
        return stores;
    }

}
