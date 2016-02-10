/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.repo.DuracloudAccountRepo;
import org.duracloud.account.db.repo.UserFinderUtil;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;

/**
 * This class is responsible for loading and caching global account information 
 * from a remote data store.
 * @author Daniel Bernstein
 */
public class GlobalUserDetailsStore implements GlobalStore{
    private Map<String,DuracloudUserDetailsService> userDetailsMap;
    private DuracloudAccountRepo accountRepo;
    private UserFinderUtil userFinderUtil;
    
    public GlobalUserDetailsStore(DuracloudAccountRepo accountRepo,
                              UserFinderUtil userFinderUtil) {
        this.accountRepo = accountRepo;
        this.userDetailsMap = new HashMap<>();
        this.userFinderUtil = userFinderUtil;
    }
    
    @Override
    public void remove(String accountId){
        this.userDetailsMap.remove(accountId);
    }

    @Override
    public void removeAll(){
        this.userDetailsMap.clear();
    }

    public DuracloudUserDetailsService getUserDetailsService(String accountId){
        ensureUserDetailsServiceIsLoaded(accountId);
        return this.userDetailsMap.get(accountId);
    }

    private void ensureUserDetailsServiceIsLoaded(String accountId) {
        if(!this.userDetailsMap.containsKey(accountId)){
            UserDetailsServiceImpl userDetails = new UserDetailsServiceImpl();
            initializeUserDetails(userDetails, accountId);
            this.userDetailsMap.put(accountId, userDetails);
        }
    }

    private void initializeUserDetails(UserDetailsServiceImpl userDetails,
                                       String accountId) {
        AccountInfo info = this.accountRepo.findBySubdomain(accountId);
        Set<DuracloudUser> dusers = userFinderUtil.getAccountUsers(info);
        Set<SecurityUserBean> securityBeans = userFinderUtil.convertDuracloudUsersToSecurityUserBeans(info, dusers, true);
        userDetails.setUsers(new ArrayList<>(securityBeans));
    }
}
