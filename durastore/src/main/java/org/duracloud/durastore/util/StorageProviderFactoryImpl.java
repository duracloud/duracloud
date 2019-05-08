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
import org.duracloud.common.changenotifier.AccountChangeNotifier;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.aws.SQSTaskQueue;
import org.duracloud.common.queue.noop.NoopTaskQueue;
import org.duracloud.common.queue.rabbitmq.RabbitMQTaskQueue;
import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.common.util.UserUtil;
import org.duracloud.durastore.test.MockRetryStorageProvider;
import org.duracloud.durastore.test.MockVerifyCreateStorageProvider;
import org.duracloud.durastore.test.MockVerifyDeleteStorageProvider;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.irodsstorage.IrodsStorageProvider;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.snapshotstorage.ChronopolisStorageProvider;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.duracloud.storage.util.StorageProviderFactory;
import org.duracloud.swiftstorage.SwiftStorageProvider;
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
    private DuraCloudRequestContextUtil contextUtil;
    private AccountChangeNotifier notifier;

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider,
                                      UserUtil userUtil,
                                      DuraCloudRequestContextUtil contextUtil,
                                      AccountChangeNotifier notifier) {
        this(storageAccountManager,
             statelessStorageProvider,
             userUtil,
             contextUtil,
             notifier,
             false);
    }

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider,
                                      UserUtil userUtil,
                                      DuraCloudRequestContextUtil contextUtil,
                                      AccountChangeNotifier notifier,
                                      boolean cacheStorageProvidersOnInit) {
        super(storageAccountManager);
        this.statelessProvider = statelessStorageProvider;
        this.storageProviders = new ConcurrentHashMap<>();
        this.userUtil = userUtil;
        this.cacheStorageProvidersOnInit = cacheStorageProvidersOnInit;
        this.contextUtil = contextUtil;
        this.notifier = notifier;

    }

    public StorageProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                      StatelessStorageProvider statelessStorageProvider,
                                      UserUtil userUtil,
                                      DuraCloudRequestContextUtil contextUtil,
                                      AccountChangeNotifier notifier,
                                      AuditConfig auditConfig) {
        this(storageAccountManager, statelessStorageProvider, userUtil, contextUtil, notifier);
        configureAuditQueue(auditConfig);
    }

    @Override
    public void initialize(DuraStoreInitConfig initConfig,
                           String instanceHost,
                           String instancePort,
                           String accountId)
        throws StorageException {
        super.initialize(initConfig, instanceHost, instancePort, accountId);
        configureAuditQueue();
        initializeStorageProviders();
    }

    private void initializeStorageProviders() {
        this.storageProviders = new ConcurrentHashMap<>();
        if (this.cacheStorageProvidersOnInit) {
            log.info("Caching storage providers on init is enabled: building storage provider cache...");
            Iterator<String> ids = getAccountManager().getStorageAccountIds();
            while (ids.hasNext()) {
                getStorageProvider(ids.next());
            }
        }
    }

    private void configureAuditQueue() {
        configureAuditQueue(getInitConfig().getAuditConfig());
    }

    private void configureAuditQueue(AuditConfig auditConfig) {
        if (null == auditConfig) {
            // If no audit config defined, turn off auditing
            this.auditQueue = new NoopTaskQueue();
        } else {
            String queueName = auditConfig.getAuditQueueName();
            if (null == queueName) {
                // If no queue name is defined, turn off auditing
                this.auditQueue = new NoopTaskQueue();
            } else {
                String queueType = auditConfig.getAuditQueueType();
                if (queueType.equalsIgnoreCase("RABBITMQ")) {
                    //RabbitMQ
                    String host = auditConfig.getRabbitmqHost();
                    String exchange = auditConfig.getRabbitmqExchange();
                    String username = auditConfig.getRabbitmqUsername();
                    String password = auditConfig.getRabbitmqPassword();
                    this.auditQueue = new RabbitMQTaskQueue(host, exchange, username, password, queueName);
                } else {
                    //AWS - SQS
                    this.auditQueue = new SQSTaskQueue(queueName);
                }
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
     * If no storage ID is provided use the primary storage provider account
     * If no storage account can be found with the given ID, throw NotFoundException
     *
     * @param storageAccountId - the ID of the storage provider account
     * @return
     * @throws StorageException
     */
    @Override
    public StorageProvider getStorageProvider(String storageAccountId)
        throws StorageException {
        // If no store ID is provided, retrieves the primary store ID
        storageAccountId = checkStorageAccountId(storageAccountId);

        if (storageProviders.containsKey(storageAccountId)) {
            return storageProviders.get(storageAccountId);
        }

        StorageAccountManager storageAccountManager = getAccountManager();
        StorageAccount account =
            storageAccountManager.getStorageAccount(storageAccountId);

        if (account == null) {
            throw new NotFoundException("No store exists with ID " + storageAccountId);
        }

        String username = account.getUsername();
        String password = account.getPassword();
        StorageProviderType type = account.getType();

        StorageProvider storageProvider = null;
        if (type.equals(StorageProviderType.AMAZON_S3)) {
            storageProvider = new S3StorageProvider(username,
                                                    password,
                                                    account.getOptions());
        } else if (type.equals(StorageProviderType.SWIFT_S3)) {
            storageProvider = new SwiftStorageProvider(username,
                                                       password,
                                                       account.getOptions());
        } else if (type.equals(StorageProviderType.AMAZON_GLACIER)) {
            storageProvider = new GlacierStorageProvider(username,
                                                         password,
                                                         account.getOptions());
        } else if (type.equals(StorageProviderType.IRODS)) {
            storageProvider = new IrodsStorageProvider(username,
                                                       password,
                                                       account.getOptions());
        } else if (type.equals(StorageProviderType.CHRONOPOLIS)) {
            storageProvider = new ChronopolisStorageProvider(username, password, account.getOptions());
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

        if (storageProvider instanceof StorageProviderBase) {
            ((StorageProviderBase) storageProvider).setWrappedStorageProvider(auditProvider);
        }

        StorageProvider aclProvider = new ACLStorageProvider(auditProvider, notifier, contextUtil);
        StorageProvider brokeredProvider =
            new BrokeredStorageProvider(statelessProvider,
                                        aclProvider,
                                        type,
                                        storageAccountId);

        storageProviders.put(storageAccountId, brokeredProvider);
        return brokeredProvider;
    }

    private String checkStorageAccountId(String storageAccountId) {
        if (null == storageAccountId) {
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
