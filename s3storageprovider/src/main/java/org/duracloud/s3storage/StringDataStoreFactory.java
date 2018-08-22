/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;

/**
 * A simple factory for StringDataStore object.
 *
 * @author Daniel Bernstein
 */
public class StringDataStoreFactory {

    private StorageAccountManager storageAccountManager;

    /**
     * Default constructor
     * @param storageAccountManager A storage account manager
     */
    public StringDataStoreFactory(StorageAccountManager storageAccountManager) {
        this.storageAccountManager = storageAccountManager;
    }

    /**
     * Create a StringDataStore
     * @param hiddenSpaceName The name of the hidden space in which to store data
     * @return
     */
    public StringDataStore create(final String hiddenSpaceName) {
        return new StringDataStore(hiddenSpaceName, getS3StorageProvider());
    }

    private S3StorageProvider getS3StorageProvider() {
        if (!storageAccountManager.isInitialized()) {
            throw new DuraCloudRuntimeException("storageAccountManager is not initialized!!!");
        }

        final StorageAccount account = storageAccountManager.getPrimaryStorageAccount();
        return new S3StorageProvider(account.getUsername(), account.getPassword(), account.getOptions());
    }
}
