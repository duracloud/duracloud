/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.common.util.AccountStoreConfig;
import org.duracloud.security.DuracloudUserDetailsService;
import org.springframework.beans.factory.config.AbstractFactoryBean;
/**
 * This class  creates a user details service bean according to the configuration
 * of account store and account id of the caller's request context.
 * @author Daniel Bernstein
 *
 */
public class UserDetailsServiceFactoryBean
    extends AbstractFactoryBean<DuracloudUserDetailsService> {
    private DuracloudUserDetailsService localUserDetailsService;
    private DuraCloudRequestContextUtil accountIdUtil = new DuraCloudRequestContextUtil();
    private GlobalUserDetailsStore globalUserDetailsStore;
    
    @Override
    protected DuracloudUserDetailsService createInstance() throws Exception {
        if(AccountStoreConfig.accountStoreIsLocal()){
            return this.localUserDetailsService;
        }else{
            return this.globalUserDetailsStore.get(accountIdUtil.getAccountId());
        }
    }

    @Override
    public Class<DuracloudUserDetailsService> getObjectType() {
        return DuracloudUserDetailsService.class;
    }

    public DuracloudUserDetailsService getLocalUserDetailsService() {
        return localUserDetailsService;
    }

    public void setLocalUserDetailsService(DuracloudUserDetailsService localUserDetailsService) {
        this.localUserDetailsService = localUserDetailsService;
    }

    public DuraCloudRequestContextUtil getAccountIdUtil() {
        return accountIdUtil;
    }

    public void setAccountIdUtil(DuraCloudRequestContextUtil accountIdUtil) {
        this.accountIdUtil = accountIdUtil;
    }

    public GlobalUserDetailsStore getGlobalUserDetailsStore() {
        return globalUserDetailsStore;
    }

    public void setGlobalUserDetailsStore(GlobalUserDetailsStore globalUserDetailsStore) {
        this.globalUserDetailsStore = globalUserDetailsStore;
    }
}
