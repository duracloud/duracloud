/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.durastore.test.MockRetryStorageProvider;
import org.duracloud.durastore.test.MockVerifyCreateStorageProvider;
import org.duracloud.durastore.test.MockVerifyDeleteStorageProvider;
import org.duracloud.emcstorage.EMCStorageProvider;
import org.duracloud.rackspacestorage.RackspaceStorageProvider;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.provider.StorageProvider;

import java.util.Iterator;

/**
 * Provides access to StorageProvider implementations
 *
 * @author Bill Branan
 */
public class StorageProviderFactoryImpl extends ProviderFactoryBase implements StorageProviderFactory {

    private StatelessStorageProvider statelessProvider;

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider) {
        super(storageAccountManager);
        this.statelessProvider = statelessStorageProvider;
    }

    /**
     * Retrieves the ids for all available storage provider accounts
     *
     * @return
     * @throws StorageException
     */
    @Override
    public Iterator<String> getStorageProviderAccountIds()
            throws StorageException {
        return getAccountManager().getStorageAccountIds();
    }

    /**
     * Retrieves the id for the primary storage provider account
     *
     * @return
     * @throws StorageException
     */
    @Override
    public String getPrimaryStorageProviderAccountId()
            throws StorageException {
        return getAccountManager().getPrimaryStorageAccountId();
    }

    /**
     * Retrieves the primary storage provider for a given customer.
     *
     * @return
     * @throws StorageException
     */
    @Override
    public StorageProvider getStorageProvider()
            throws StorageException {
        return getStorageProvider(null);
    }

    /**
     * Retrieves a particular storage provider based on the storage account ID.
     * If a storage account cannot be retrieved, the primary storage provider
     * account is used.
     *
     * @param storageAccountId - the ID of the storage provider account
     * @return
     * @throws StorageException
     */
    @Override
    public StorageProvider getStorageProvider(String storageAccountId)
            throws StorageException {
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

        StorageProvider storageProvider = null;
        if (type.equals(StorageProviderType.AMAZON_S3)) {
            storageProvider = new S3StorageProvider(username, password);
        } else if (type.equals(StorageProviderType.MICROSOFT_AZURE)) {
            // TODO: Create Azure storage provider
        } else if (type.equals(StorageProviderType.RACKSPACE)) {
            storageProvider = new RackspaceStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.EMC)) {
            storageProvider = new EMCStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.TEST_RETRY)) {
            storageProvider = new MockRetryStorageProvider();
        } else if (type.equals(StorageProviderType.TEST_VERIFY_CREATE)) {
            storageProvider = new MockVerifyCreateStorageProvider();
        } else if (type.equals(StorageProviderType.TEST_VERIFY_DELETE)) {
            storageProvider = new MockVerifyDeleteStorageProvider();
        }

        return new BrokeredStorageProvider(statelessProvider,
                                           storageProvider,
                                           storageAccountId);
    }

    /**
     * Returns the type of the storage provider with the given account ID. If
     * no storage provider is available with that ID, the UNKNOWN type is returned.
     *
     * @param storageAccountId
     * @return
     * @throws StorageException
     */
    @Override
    public StorageProviderType getStorageProviderType(String storageAccountId)
            throws StorageException {
        StorageAccount account =
            getAccountManager().getStorageAccount(storageAccountId);
        if(account != null) {
            return account.getType();
        } else {
            return StorageProviderType.UNKNOWN;
        }
    }

}
