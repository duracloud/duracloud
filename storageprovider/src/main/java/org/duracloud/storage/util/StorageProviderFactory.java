/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.util;

import java.io.InputStream;
import java.util.List;

import org.duracloud.common.queue.TaskQueue;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;

/**
 * @author Andrew Woods
 *         Date: Aug 19, 2010
 */
public interface StorageProviderFactory {

    @Deprecated
    public void initialize(InputStream accountXml,
                           String instanceHost,
                           String instancePort);

    public void initialize(DuraStoreInitConfig initConfig,
                           String instanceHost,
                           String instancePort);

    public boolean isInitialized();

    public List<StorageAccount> getStorageAccounts();

    public StorageProvider getStorageProvider() throws StorageException;

    public StorageProvider getStorageProvider(String storageAccountId)
        throws StorageException;

    public void expireStorageProvider(String storageAccountId);
    
    public TaskQueue getAuditQueue();

}
