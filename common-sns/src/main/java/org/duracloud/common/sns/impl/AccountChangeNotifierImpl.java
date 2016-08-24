/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.sns.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.duracloud.account.db.model.GlobalProperties;
import org.duracloud.account.db.repo.GlobalPropertiesRepo;
import org.duracloud.common.event.AccountChangeEvent;
import org.duracloud.common.event.AccountChangeEvent.EventType;
import org.duracloud.common.sns.AccountChangeNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sns.AmazonSNSClient;
/**
 * 
 * @author Daniel Bernstein
 *
 */
@Component("accountChangeNotifier")
public class AccountChangeNotifierImpl implements AccountChangeNotifier {
    
    private AmazonSNSClient snsClient;
    
    private GlobalPropertiesRepo globalPropertiesRepo;

    private static Logger log = LoggerFactory.getLogger(AccountChangeNotifierImpl.class);
    /**
     * 
     * @param globalPropertiesConfigService
     */
    @Autowired
    public AccountChangeNotifierImpl(GlobalPropertiesRepo globalPropertiesRepo) {
        this.snsClient = new AmazonSNSClient();
        this.globalPropertiesRepo = globalPropertiesRepo;
    }
    
    @Override
    public void accountChanged(String account) {
        publish(AccountChangeEvent.EventType.ACCOUNT_CHANGED, account);
    }

    
    private void publish(EventType eventType, String account) {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("unable to resolve unknown host: " + e.getMessage(), e);
            host = "unknown";
        }

        AccountChangeEvent event = new AccountChangeEvent(eventType, account, host);
        
        try {
            log.debug("publishing event={}", event);
            GlobalProperties props = globalPropertiesRepo.findAll().get(0);
            this.snsClient.publish(props.getInstanceNotificationTopicArn(),
                    AccountChangeEvent.serialize(event));
            log.info("published event={}", event);
        }catch(Exception e){
            log.error("Failed to publish event: " + event +" : " + e.getMessage(), e);
        }
    }


    @Override
    public void storageProvidersChanged(String accountId) {
        publish(EventType.STORAGE_PROVIDERS_CHANGED,
                accountId);
    }

    @Override
    public void userStoreChanged(String accountId) {
        publish(EventType.USERS_CHANGED,
                accountId);
    }

    @Override
    public void rootUsersChanged() {
        publish(EventType.ALL_ACCOUNTS_CHANGED, null);
    }

    @Override
    public void storageProviderCacheOnNodeChanged(String account) {
        publish(EventType.STORAGE_PROVIDER_CACHE_ON_NODE_CHANGED, account);
    }
}
