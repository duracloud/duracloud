/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import java.util.ArrayList;
import java.util.Set;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.repo.DuracloudAccountRepo;
import org.duracloud.account.db.repo.UserFinderUtil;
import org.duracloud.common.event.AccountChangeEvent;
import org.duracloud.common.event.AccountChangeEvent.EventType;
import org.duracloud.common.cache.AbstractAccountComponentCache;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;

/**
 * This class is responsible for loading and caching global user details information 
 * from a remote data store.
 * @author Daniel Bernstein
 */
public class UserDetailsServiceCache extends AbstractAccountComponentCache<DuracloudUserDetailsService>{
    private DuracloudAccountRepo accountRepo;
    private UserFinderUtil userFinderUtil;
    
    public UserDetailsServiceCache(DuracloudAccountRepo accountRepo,
                              UserFinderUtil userFinderUtil) {
        super();
        this.accountRepo = accountRepo;
        this.userFinderUtil = userFinderUtil;
    }
    
    @Override
    public void onEvent(AccountChangeEvent event) {
        String accountId = event.getAccountId();
        EventType eventType = event.getEventType();
        if(accountId != null){
            if(eventType.equals(EventType.USERS_CHANGED)|| 
                eventType.equals(EventType.ACCOUNT_CHANGED)){
                remove(accountId);
            }
        }else if(eventType.equals(EventType.ALL_ACCOUNTS_CHANGED)){
            removeAll();
        }
    }
    
    @Override
    protected DuracloudUserDetailsService createInstance(String accountId) {
        UserDetailsServiceImpl userDetails = new UserDetailsServiceImpl();
        initializeUserDetails(userDetails, accountId);
        return userDetails;
    }

    private void initializeUserDetails(UserDetailsServiceImpl userDetails,
                                       String accountId) {
        AccountInfo info = this.accountRepo.findBySubdomain(accountId);
        Set<DuracloudUser> dusers = userFinderUtil.getAccountUsers(info);
        Set<SecurityUserBean> securityBeans = userFinderUtil.convertDuracloudUsersToSecurityUserBeans(info, dusers, true);
        userDetails.setUsers(new ArrayList<>(securityBeans));
    }
}
