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
import org.duracloud.common.util.UserUtil;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;

/**
 * This class is responsible for loading and caching global account information 
 * from a remote data store.
 * @author Daniel Bernstein
 */
public class StorageProviderFactoryCache extends AbstractAccountComponentCache<StorageProviderFactory> {
    private StorageAccountManagerFactory storageAccountManagerFactory;
    private StatelessStorageProvider statelessStorageProvider;
    private UserUtil userUtil;
    
    public StorageProviderFactoryCache(StorageAccountManagerFactory storageAccountManagerFactory,
                              StatelessStorageProvider statelessStorageProvider,
                              UserUtil userUtil) {
        super();
        this.storageAccountManagerFactory = storageAccountManagerFactory;
        this.statelessStorageProvider = statelessStorageProvider;
        this.userUtil = userUtil;
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
    protected StorageProviderFactory createInstance (String accountId) {
        
        // retrieve account info from db
        StorageAccountManager storageAccountManager =
            this.storageAccountManagerFactory.createInstance();
        
        StorageProviderFactoryImpl factory =
            new StorageProviderFactoryImpl(storageAccountManager,
                                           statelessStorageProvider,
                                           userUtil);
        return factory;
    }
   
}
