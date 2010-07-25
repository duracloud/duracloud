/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class ContentStoreSelectorTest
        extends ContentStoreProviderTestBase {

    @Test
    public void testSelectStore() throws Exception {
        String storeId = this.contentStoreProvider.getSelectedContentStoreId();
        Assert.assertNotNull(storeId);
        for (ContentStore store : getContentStores()) {
            String sId = store.getStoreId();
            if (sId != storeId) {
                Assert.assertNotSame(sId, contentStoreProvider
                        .getContentStore().getStoreId());
                this.contentStoreProvider.setSelectedContentStoreId(sId);
                Assert.assertSame(sId, contentStoreProvider.getContentStore()
                        .getStoreId());
                break;
            }
        }
    }

    private Collection<ContentStore> getContentStores() throws ContentStoreException {
        Collection<ContentStore> stores = contentStoreProvider.getContentStores();
        Assert.assertNotNull(stores);
        Assert.assertTrue(stores.size() > 0);
        return stores;
    }
}
