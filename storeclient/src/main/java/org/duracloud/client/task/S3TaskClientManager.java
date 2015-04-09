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
 * Provides a way to get access to an S3TaskClient. This simplifies
 * the process of making calls to S3 storage provider tasks.
 *
 * @author Bill Branan
 *
 */
public class S3TaskClientManager {

    private ContentStoreManager contentStoreManager;

    public S3TaskClientManager(ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    /**
     * Retrieve an S3TaskClient
     *
     * @param storeId id of the storage provider
     * @return client for calling S3 tasks
     * @throws ContentStoreException on error
     */
    public S3TaskClient get(String storeId) throws ContentStoreException {
        ContentStore contentStore = contentStoreManager.getContentStore(storeId);
        return new S3TaskClientImpl(contentStore);
    }

}
