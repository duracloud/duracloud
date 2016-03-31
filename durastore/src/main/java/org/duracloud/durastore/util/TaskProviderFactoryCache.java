/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.common.cache.AbstractAccountComponentCache;
import org.duracloud.common.event.AccountChangeEvent;
import org.duracloud.common.event.AccountChangeEvent.EventType;
import org.duracloud.common.util.AccountStoreConfig;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.provider.TaskProviderFactory;
import org.duracloud.storage.util.StorageProviderFactory;

/**
 * This class is responsible for loading and caching global account information 
 * from a remote data store.
 * @author Daniel Bernstein
 */
public class TaskProviderFactoryCache extends AbstractAccountComponentCache<TaskProviderFactory> {
    private StorageAccountManagerFactory storageAccountManagerFactory;
    private StorageProviderFactory storageProviderFactory;
    private ManifestStore manifestStore;
    private TaskProviderFactoryImpl localFactory;
     
    public TaskProviderFactoryCache(StorageAccountManagerFactory storageAccountManagerFactory,
                                    StorageProviderFactory storageProviderFactory,
                              ManifestStore manifestStore) {
        super();
        this.storageAccountManagerFactory = storageAccountManagerFactory;
        this.storageProviderFactory = storageProviderFactory;
        this.manifestStore = manifestStore;
    }
    
    
    @Override
    public void onEvent(AccountChangeEvent event) {
        String accountId = event.getAccountId();
        EventType eventType = event.getEventType();
        if(accountId != null){
            if(eventType.equals(EventType.STORAGE_PROVIDERS_CHANGED)|| 
                eventType.equals(EventType.ACCOUNT_CHANGED)){
                remove(accountId);
            }
        }else if(eventType.equals(EventType.ALL_ACCOUNTS_CHANGED)){
            removeAll();
        }
    }
    
    @Override
    protected TaskProviderFactory createInstance (String accountId) {
        if(AccountStoreConfig.accountStoreIsLocal()){
            if(this.localFactory == null){
                this.localFactory = new TaskProviderFactoryImpl(storageAccountManagerFactory.createInstance(), storageProviderFactory, manifestStore);
            }
            return this.localFactory;
        }
        // retrieve account info from db
        StorageAccountManager storageAccountManager =
            this.storageAccountManagerFactory.createInstance();
        return  new TaskProviderFactoryImpl(storageAccountManager, storageProviderFactory, manifestStore);
    }
   
}
