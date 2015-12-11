/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.storage.util.StorageProviderFactory;

/**
 * This class is responsible for loading and caching global account information 
 * from a remote data store.
 * @author Daniel Bernstein
 */
public class GlobalAccountStore {
    private Map<String,StorageProviderFactory> factoryMap;
    private Map<String,DuracloudUserDetailsService> userDetailsMap;
    public GlobalAccountStore(){
        this.factoryMap = new HashMap<>();
        userDetailsMap = new HashMap<>();
    }
    
    public StorageProviderFactory getStorageProviderFactory(String accountId){
        loadAccountIfNotFound(accountId);
        return this.factoryMap.get(accountId);
    }

    private void loadAccountIfNotFound(String accountId) {
        if(!this.factoryMap.containsKey(accountId) || !this.userDetailsMap.containsKey(accountId)){
            //get account data
            //create and cache storageproviderfactory
            //create and cache userdetailsservice
            throw new NotImplementedException();
        }
    }

    public DuracloudUserDetailsService getUserDetailsService(String accountId){
        loadAccountIfNotFound(accountId);
        return this.userDetailsMap.get(accountId);
    }
}
