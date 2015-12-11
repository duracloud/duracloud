/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.AccountIdUtil;
import org.duracloud.common.util.AccountStoreConfig;
import org.duracloud.security.impl.GlobalAccountStore;
import org.duracloud.storage.util.StorageProviderFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class StorageProviderFactoryBean extends AbstractFactoryBean<StorageProviderFactory>{
    private StorageProviderFactory localStorageProviderFactory;
    private GlobalAccountStore globalAccountStore;
    private AccountIdUtil accountIdUtil = new AccountIdUtil();
    
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
            return this.globalAccountStore.getStorageProviderFactory(accountIdUtil.getAccountId());
        }
    }

    @Override
    public Class<StorageProviderFactory> getObjectType() {
        return StorageProviderFactory.class;
    }

    public GlobalAccountStore getGlobalAccountStore() {
        return globalAccountStore;
    }

    public void setGlobalAccountStore(GlobalAccountStore globalAccountStore) {
        this.globalAccountStore = globalAccountStore;
    }

    public AccountIdUtil getAccountIdUtil() {
        return accountIdUtil;
    }

    public void setAccountIdUtil(AccountIdUtil accountIdUtil) {
        this.accountIdUtil = accountIdUtil;
    }

}
