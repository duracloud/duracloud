package org.duracloud.storage.provider;

import org.duracloud.storage.error.TaskException;

public interface TaskProviderFactory {

    /**
     * Retrieves the primary task provider for a given customer.
     *
     * @return
     * @throws org.duracloud.storage.error.StorageException
     */
    public TaskProvider getTaskProvider();

    /**
     * Retrieves a particular task provider based on the storage account ID.
     *
     * @param storageAccountId - the ID of the provider account
     * @return
     */
    public TaskProvider getTaskProvider(String storageAccountId)
            throws TaskException;
}
