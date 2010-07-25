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
import org.duracloud.duradmin.config.DuradminConfig;
import org.duracloud.error.ContentStoreException;

import java.util.ArrayList;
import java.util.List;

public class ContentStoreProvider {

    private ContentStoreManager contentStoreManager;

    private ContentStoreSelector contentStoreSelector;

    public ContentStoreProvider(ContentStoreManager contentStoreManager,
                                ContentStoreSelector contentStoreSelector) {
        this.contentStoreManager = contentStoreManager;
        this.contentStoreSelector = contentStoreSelector;
    }

    public String getSelectedContentStoreId() throws ContentStoreException {
        return contentStoreSelector.getSelectedId(contentStoreManager);
    }

    public void setSelectedContentStoreId(String storeId) {
        contentStoreSelector.setSelectedId(storeId);
    }

    public ContentStore getContentStore() throws ContentStoreException {
        String contentStoreId = getSelectedContentStoreId();
        return this.contentStoreManager.getContentStore(contentStoreId);
    }

    public List<ContentStore> getContentStores() throws ContentStoreException {
        List<ContentStore> stores = new ArrayList<ContentStore>();
        stores.addAll(this.contentStoreManager.getContentStores().values());
        return stores;        
    }

    public void reinitializeContentStoreManager() throws ContentStoreException {
        this.contentStoreManager.reinitialize(DuradminConfig.getDuraStoreHost(),
                                              DuradminConfig.getDuraStorePort(),
                                              DuradminConfig.getDuraStoreContext());
    }
}
