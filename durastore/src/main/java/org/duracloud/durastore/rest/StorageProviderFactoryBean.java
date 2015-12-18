/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.common.util.AccountStoreConfig;
import org.duracloud.durastore.util.GlobalStorageProviderStore;
import org.duracloud.storage.util.StorageProviderFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class StorageProviderFactoryBean extends AbstractFactoryBean<StorageProviderFactory>{
    private StorageProviderFactory localStorageProviderFactory;
    private GlobalStorageProviderStore globalStorageAccountManager;
    private DuraCloudRequestContextUtil accountIdUtil = new DuraCloudRequestContextUtil();
    
    public StorageProviderFactoryBean(){
        super();
    }

    public StorageProviderFactory getlocalStorageProviderFactory() {
        return localStorageProviderFactory;
    }
    
    public void
           setlocalStorageProviderFactory(StorageProviderFactory localStorageProviderFactory) {
        this.localStorageProviderFactory = localStorageProviderFactory;
    }
    
    @Override
    public StorageProviderFactory createInstance() throws Exception {
        if(AccountStoreConfig.accountStoreIsLocal()){
            return this.localStorageProviderFactory;
        }else{
            return this.globalStorageAccountManager.getStorageProviderFactory(accountIdUtil.getAccountId());
        }
    }

    @Override
    public Class<StorageProviderFactory> getObjectType() {
        return StorageProviderFactory.class;
    }

    public GlobalStorageProviderStore getGlobalStorageProviderStore() {
        return globalStorageAccountManager;
    }

    public void setGlobalStorageProviderStore(GlobalStorageProviderStore globalStorageProviderStore) {
        this.globalStorageAccountManager = globalStorageProviderStore;
    }

    public DuraCloudRequestContextUtil getAccountIdUtil() {
        return accountIdUtil;
    }

    public void setAccountIdUtil(DuraCloudRequestContextUtil accountIdUtil) {
        this.accountIdUtil = accountIdUtil;
    }

}
