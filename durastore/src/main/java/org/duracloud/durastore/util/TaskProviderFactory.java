/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.s3task.S3TaskProvider;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.TaskProvider;

/**
 * Provides access to TaskProvider implementations
 *
 * @author Bill Branan
 * Date: May 20, 2010
 */
public class TaskProviderFactory extends ProviderFactoryBase {

    /**
     * Retrieves the primary task provider for a given customer.
     *
     * @return
     * @throws org.duracloud.storage.error.StorageException
     */
    public static TaskProvider getTaskProvider() {
        return getTaskProvider(null);
    }

    /**
     * Retrieves a particular task provider based on the storage account ID.
     *
     * @param storageAccountId - the ID of the provider account
     * @return
     */
    public static TaskProvider getTaskProvider(String storageAccountId)
            throws TaskException {
        StorageAccountManager storageAccountManager = getAccountManager();
        StorageAccount account =
            storageAccountManager.getStorageAccount(storageAccountId);
        if (account == null) {
            account = storageAccountManager.getPrimaryStorageAccount();
            storageAccountId = account.getId();
        }
        String username = account.getUsername();
        String password = account.getPassword();
        StorageProviderType type = account.getType();

        TaskProvider taskProvider = null;
        if (type.equals(StorageProviderType.AMAZON_S3)) {
            taskProvider = new S3TaskProvider(username, password);
        } else {
            throw new TaskException("No TaskProvider is available for " + type);
        }

        return taskProvider;
    }

}