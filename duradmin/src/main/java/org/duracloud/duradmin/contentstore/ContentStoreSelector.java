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

public class ContentStoreSelector {

    private String selectedId;

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    public String getSelectedId(ContentStoreManager contentStoreManager)
        throws ContentStoreException {
        if (selectedId == null) {
            ContentStore store = contentStoreManager.getPrimaryContentStore();
            setSelectedId(store.getStoreId());
        }
        return selectedId;
    }

}
