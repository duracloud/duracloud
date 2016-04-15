/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.durastore.util.StorageProviderFactoryCache;
import org.duracloud.storage.util.StorageProviderFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class StorageProviderFactoryBean extends AbstractFactoryBean<StorageProviderFactory>{
    private StorageProviderFactoryCache storageProviderFactoryCache;
    private DuraCloudRequestContextUtil accountIdUtil = new DuraCloudRequestContextUtil();
    
    public StorageProviderFactoryBean(){
        super();
    }

    
    @Override
    public StorageProviderFactory createInstance() throws Exception {
        return this.storageProviderFactoryCache.get(accountIdUtil.getAccountId());
    }

    @Override
    public Class<StorageProviderFactory> getObjectType() {
        return StorageProviderFactory.class;
    }

    public StorageProviderFactoryCache getStorageProviderFactoryCache() {
        return storageProviderFactoryCache;
    }

    public void setStorageProviderFactoryCache(StorageProviderFactoryCache storageProviderFactoryCache) {
        this.storageProviderFactoryCache = storageProviderFactoryCache;
    }

    public DuraCloudRequestContextUtil getAccountIdUtil() {
        return accountIdUtil;
    }

    public void setAccountIdUtil(DuraCloudRequestContextUtil accountIdUtil) {
        this.accountIdUtil = accountIdUtil;
    }

}
