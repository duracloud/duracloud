/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import java.net.InetAddress;

import org.duracloud.account.db.repo.DuracloudMillRepo;
import org.duracloud.common.cache.AbstractAccountComponentCache;
import org.duracloud.common.changenotifier.AccountChangeNotifier;
import org.duracloud.common.event.AccountChangeEvent;
import org.duracloud.common.event.AccountChangeEvent.EventType;
import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.common.util.UserUtil;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for loading and caching global account information
 * from a remote data store.
 *
 * @author Daniel Bernstein
 */
public class StorageProviderFactoryCache extends AbstractAccountComponentCache<StorageProviderFactory> {
    private StorageAccountManagerFactory storageAccountManagerFactory;
    private StatelessStorageProvider statelessStorageProvider;
    private UserUtil userUtil;
    private DuracloudMillRepo millRepo;
    private DuraCloudRequestContextUtil contextUtil;
    private AccountChangeNotifier notifier;

    private Logger log = LoggerFactory.getLogger(StorageProviderFactoryCache.class);

    public StorageProviderFactoryCache(StorageAccountManagerFactory storageAccountManagerFactory,
                                       StatelessStorageProvider statelessStorageProvider,
                                       UserUtil userUtil,
                                       DuracloudMillRepo millRepo,
                                       AccountChangeNotifier notifier,
                                       DuraCloudRequestContextUtil contextUtil) {
        super();
        this.storageAccountManagerFactory = storageAccountManagerFactory;
        this.statelessStorageProvider = statelessStorageProvider;
        this.userUtil = userUtil;
        this.millRepo = millRepo;
        this.contextUtil = contextUtil;
        this.notifier = notifier;
    }

    @Override
    public void onEvent(AccountChangeEvent event) {
        String accountId = event.getAccountId();
        EventType eventType = event.getEventType();
        if (accountId != null) {
            if (eventType.equals(EventType.STORAGE_PROVIDERS_CHANGED) ||
                eventType.equals(EventType.ACCOUNT_CHANGED) ||
                isAnotherNode(event)) {
                remove(accountId);
            }
        } else if (eventType.equals(EventType.ALL_ACCOUNTS_CHANGED)) {
            removeAll();
        }
    }

    private boolean isAnotherNode(AccountChangeEvent event) {
        if (event.getEventType()
                 .equals(EventType.STORAGE_PROVIDER_CACHE_ON_NODE_CHANGED)) {
            try {
                String host = InetAddress.getLocalHost().getHostName();
                if (!host.equals(event.getSourceHost())) {
                    log.debug("This node {} is identical to the event source.", host);
                    return true;
                } else {
                    log.debug("This node {} is different from event source.", host);
                }
            } catch (Exception ex) {
                log.warn("failed to check host: " + ex.getMessage(), ex);
            }
        }

        return false;
    }

    @Override
    protected StorageProviderFactory createInstance(String accountId) {

        // retrieve account info from db
        StorageAccountManager storageAccountManager =
            this.storageAccountManagerFactory.createInstance();

        AuditConfig auditConfig = new AuditConfigBuilder(millRepo).build();

        StorageProviderFactoryImpl factory =
            new StorageProviderFactoryImpl(storageAccountManager,
                                           statelessStorageProvider,
                                           userUtil,
                                           this.contextUtil,
                                           this.notifier,
                                           auditConfig);

        return factory;
    }

}
