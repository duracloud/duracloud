/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.duracloud.audit.provider.AuditStorageProvider;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.aws.SQSTaskQueue;
import org.duracloud.common.queue.noop.NoopTaskQueue;
import org.duracloud.common.util.UserUtil;
import org.duracloud.durastore.test.MockRetryStorageProvider;
import org.duracloud.durastore.test.MockVerifyCreateStorageProvider;
import org.duracloud.durastore.test.MockVerifyDeleteStorageProvider;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.irodsstorage.IrodsStorageProvider;
import org.duracloud.rackspacestorage.RackspaceStorageProvider;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.sdscstorage.SDSCStorageProvider;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to StorageProvider implementations
 *
 * @author Bill Branan
 */
public class StorageProviderFactoryImpl extends ProviderFactoryBase
    implements StorageProviderFactory {

    private Logger log =
        LoggerFactory.getLogger(StorageProviderFactoryImpl.class);

    private StatelessStorageProvider statelessProvider;
    private Map<String, StorageProvider> storageProviders;
    private UserUtil userUtil;
    private TaskQueue auditQueue;
    private boolean cacheStorageProvidersOnInit = false;

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider,
                                      UserUtil userUtil) {
        this(storageAccountManager,
             statelessStorageProvider,
             userUtil,
             false);
    }

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider,
                                      UserUtil userUtil,
                                      boolean cacheStorageProvidersOnInit) {
        super(storageAccountManager);
        this.statelessProvider = statelessStorageProvider;
        this.storageProviders = new ConcurrentHashMap<>();
        this.userUtil = userUtil;
        this.cacheStorageProvidersOnInit = cacheStorageProvidersOnInit;
    }

    @Override
    public void initialize(DuraStoreInitConfig initConfig,
                           String instanceHost,
                           String instancePort)
            throws StorageException {
        super.initialize(initConfig, instanceHost, instancePort);
        configureAuditQueue();
        initializeStorageProviders();
    }

    private void initializeStorageProviders() {
        this.storageProviders = new ConcurrentHashMap<>();
        if(this.cacheStorageProvidersOnInit){
            log.info("Caching storage providers on init is enabled: building storage provider cache...");
            Iterator<String> ids = getAccountManager().getStorageAccountIds();
            while(ids.hasNext()){
                getStorageProvider(ids.next());
            }
        }
    }

    private void configureAuditQueue() {
        AuditConfig auditConfig = getInitConfig().getAuditConfig();
        if(null == auditConfig) {
            // If no audit config defined, turn off auditing
            this.auditQueue = new NoopTaskQueue();
        } else {
            String queueName = auditConfig.getAuditQueueName();
            if(null == queueName) {
                // If no queue name is defined, turn off auditing
                this.auditQueue = new NoopTaskQueue();
            } else {
                // If username and pass are provided, push into system props
                // for the SQS client to pick up
                String auditUsername = auditConfig.getAuditUsername();
                String auditPassword = auditConfig.getAuditPassword();
                if(null != auditUsername && null != auditPassword) {
                    System.setProperty("aws.accessKeyId", auditUsername);
                    System.setProperty("aws.secretKey", auditPassword);
                }
                this.auditQueue = new SQSTaskQueue(queueName);
            }
        }
    }

    @Override
    public TaskQueue getAuditQueue() {
        return this.auditQueue;
    }
    /**
     * This method returns all of the registered storage accounts.
     *
     * @return list of storage accounts
     */
    @Override
    public List<StorageAccount> getStorageAccounts() {
        List<StorageAccount> accts = new ArrayList<>();

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
        storageAccountId = checkStorageAccountId(storageAccountId);

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
        } else if (type.equals(StorageProviderType.RACKSPACE)) {
            storageProvider = new RackspaceStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.SDSC)) {
            storageProvider = new SDSCStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.IRODS)) {
            storageProvider = new IrodsStorageProvider(username,
                                                       password,
                                                       account.getOptions());
        } else if (type.equals(StorageProviderType.SNAPSHOT)) {
            storageProvider = new SnapshotStorageProvider(username, password);
        } else if (type.equals(StorageProviderType.TEST_RETRY)) {
            storageProvider = new MockRetryStorageProvider();
        } else if (type.equals(StorageProviderType.TEST_VERIFY_CREATE)) {
            storageProvider = new MockVerifyCreateStorageProvider();
        } else if (type.equals(StorageProviderType.TEST_VERIFY_DELETE)) {
            storageProvider = new MockVerifyDeleteStorageProvider();
        } else {
            throw new StorageException("Unsupported storage provider type ("
                + type.name() + ")  associated with storage account ("
                + storageAccountId + "): unable to create");
        }

        StorageProvider auditProvider =
            new AuditStorageProvider(storageProvider,
                                     storageAccountManager.getAccountName(),
                                     storageAccountId,
                                     type.getName(),
                                     userUtil,
                                     auditQueue);
        
        if(storageProvider instanceof StorageProviderBase){
            ((StorageProviderBase)storageProvider).setWrappedStorageProvider(auditProvider);
        }
        
        StorageProvider aclProvider = new ACLStorageProvider(auditProvider);
        StorageProvider brokeredProvider =
            new BrokeredStorageProvider(statelessProvider,
                                        aclProvider,
                                        type,
                                        storageAccountId);

        storageProviders.put(storageAccountId, brokeredProvider);
        return brokeredProvider;
    }

    private String checkStorageAccountId(String storageAccountId) {
        if(null == storageAccountId) {
            return getAccountManager().getPrimaryStorageAccount().getId();
        }
        return storageAccountId;
    }

    /**
     * Removes a particular storage provider from the cache, which will
     * require that the connection be recreated on the next call.
     *
     * @param storageAccountId - the ID of the storage provider account
     */
    @Override
    public void expireStorageProvider(String storageAccountId) {
        storageAccountId = checkStorageAccountId(storageAccountId);

        log.info("Expiring storage provider connection!  Storage account id: {}", storageAccountId);
        storageProviders.remove(storageAccountId);
    }

}
