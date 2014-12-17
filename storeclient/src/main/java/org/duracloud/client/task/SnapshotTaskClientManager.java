/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.task;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.error.ContentStoreException;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SnapshotTaskClientManager {
    private ContentStoreManager contentStoreManager;
    public SnapshotTaskClientManager(ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }
    
    public SnapshotTaskClient get(String storeId) throws ContentStoreException {
        ContentStore contentStore = contentStoreManager.getContentStore(storeId);
        return new SnapshotTaskClientImpl(contentStore);
    }
}
