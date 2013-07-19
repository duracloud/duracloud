/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.azurestorage.AzureStorageProvider;
import org.duracloud.durastore.test.MockRetryStorageProvider;
import org.duracloud.durastore.test.MockVerifyCreateStorageProvider;
import org.duracloud.durastore.test.MockVerifyDeleteStorageProvider;
import org.duracloud.emcstorage.EMCStorageProvider;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.hpstorage.HPStorageProvider;
import org.duracloud.irodsstorage.IrodsStorageProvider;
import org.duracloud.rackspacestorage.RackspaceStorageProvider;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.sdscstorage.SDSCStorageProvider;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides access to StorageProvider implementations
 *
 * @author Bill Branan
 */
public class StorageProviderFactoryImpl extends ProviderFactoryBase
    implements StorageProviderFactory {

    private Logger log = LoggerFactory.getLogger(StorageProviderFactoryImpl.class);

    private StatelessStorageProvider statelessProvider;
    private Map<String, StorageProvider> storageProviders;
    private boolean cacheStorageProvidersOnInit = false;

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider) {
        this(storageAccountManager, statelessStorageProvider, false);
    }

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider,
                                      boolean cacheStorageProvidersOnInit) {

        super(storageAccountManager);
        this.statelessProvider = statelessStorageProvider;
        this.storageProviders = new ConcurrentHashMap<String, StorageProvider>();
        this.cacheStorageProvidersOnInit = cacheStorageProvidersOnInit;
    }

    @Override
    public void initialize(InputStream accountXml)
            throws StorageException {
        super.initialize(accountXml);
        initializeStorageProviders();
    }

    private void initializeStorageProviders() {
        this.storageProviders = new ConcurrentHashMap<String, StorageProvider>();
        if(this.cacheStorageProvidersOnInit){
            log.info("Caching storage providers on init is enabled: building storage provider cache...");
            Iterator<String> ids = getAccountManager().getStorageAccountIds();
            while(ids.hasNext()){
                getStorageProvider(ids.next());
            }
        }
    }

    /**
     * This method returns all of the registered storage accounts.
     *
     * @return list of storage accounts
     */
    @Override
    public List<StorageAccount> getStorageAccounts() {
        List<StorageAccount> accts = new ArrayList<StorageAccount>();

        Iterator<String> ids = getAccountManager().getStorageAccountIds();
        while (ids.hasNext()) {
            accts.add(getAccountManager().getStorageAccount(ids.next()));
        }
        return accts;
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
        if(null == storageAccountId) {
            storageAccountId =
                getAccountManager().getPrimaryStorageAccount().getId();
        }

        if(storageProviders.containsKey(storageAccountId)) {
            return storageProviders.get(storageAccountId);
        }

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
            storageProvider = new S3StorageProvider(username,
                                                    password,
                                                    account.getOptions());
        } else if (type.equals(StorageProviderType.AMAZON_GLACIER)) {
            storageProvider = new GlacierStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.MICROSOFT_AZURE)) {
            storageProvider = new AzureStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.RACKSPACE)) {
            storageProvider = new RackspaceStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.SDSC)) {
            storageProvider = new SDSCStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.HP)) {
            storageProvider = new HPStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.EMC)) {
            storageProvider = new EMCStorageProvider(username, password);

        } else if (type.equals(StorageProviderType.IRODS)) {
            storageProvider = new IrodsStorageProvider(username,
                                                       password,
                                                       account.getOptions());

        } else if (type.equals(StorageProviderType.TEST_RETRY)) {
            storageProvider = new MockRetryStorageProvider();
        } else if (type.equals(StorageProviderType.TEST_VERIFY_CREATE)) {
            storageProvider = new MockVerifyCreateStorageProvider();
        } else if (type.equals(StorageProviderType.TEST_VERIFY_DELETE)) {
            storageProvider = new MockVerifyDeleteStorageProvider();
        }

        StorageProvider aclProvider = new ACLStorageProvider(storageProvider);
        StorageProvider brokeredProvider =
            new BrokeredStorageProvider(statelessProvider,
                                        aclProvider,
                                        type,
                                        storageAccountId);

        storageProviders.put(storageAccountId, brokeredProvider);
        return brokeredProvider;
    }

    /**
     * Removes a particular storage provider from the cache, which will
     * require that the connection be recreated on the next call.
     *
     * @param storageAccountId - the ID of the storage provider account
     */
    @Override
    public void expireStorageProvider(String storageAccountId) {
        storageProviders.remove(storageAccountId);
    }

}
