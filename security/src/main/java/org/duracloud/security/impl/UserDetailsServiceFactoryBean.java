/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.duracloud.common.rest.DuraCloudRequestContextUtil;
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
    private DuraCloudRequestContextUtil accountIdUtil = new DuraCloudRequestContextUtil();
    private UserDetailsServiceCache userDetailsServiceCache;
    
    @Override
    protected DuracloudUserDetailsService createInstance() throws Exception {
        return this.userDetailsServiceCache.get(accountIdUtil.getAccountId());
    }

    @Override
    public Class<DuracloudUserDetailsService> getObjectType() {
        return DuracloudUserDetailsService.class;
    }

    public DuraCloudRequestContextUtil getAccountIdUtil() {
        return accountIdUtil;
    }

    public void setAccountIdUtil(DuraCloudRequestContextUtil accountIdUtil) {
        this.accountIdUtil = accountIdUtil;
    }

    public UserDetailsServiceCache getUserDetailsServiceCache() {
        return userDetailsServiceCache;
    }

    public void setUserDetailsServiceCache(UserDetailsServiceCache userDetailsServiceCache) {
        this.userDetailsServiceCache = userDetailsServiceCache;
    }
}
