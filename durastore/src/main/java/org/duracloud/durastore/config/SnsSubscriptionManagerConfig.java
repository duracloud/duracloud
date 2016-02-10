/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.config;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.duracloud.account.db.model.GlobalProperties;
import org.duracloud.account.db.repo.GlobalPropertiesRepo;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.durastore.util.GlobalStorageProviderStore;
import org.duracloud.durastore.util.MessageListener;
import org.duracloud.durastore.util.SnsSubscriptionManager;
import org.duracloud.security.impl.GlobalUserDetailsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;

/**
 * 
 * @author Daniel Bernstein
 */
@Configuration
public class SnsSubscriptionManagerConfig {
    private Logger log = LoggerFactory.getLogger(SnsSubscriptionManagerConfig.class);
    
    @Bean(destroyMethod="disconnect", initMethod="connect")
    public SnsSubscriptionManager
           snsSubscriptionManager(GlobalPropertiesRepo globalPropertiesRepo,
                                  final GlobalUserDetailsStore userDetails,
                                  final GlobalStorageProviderStore providerStore) {
        try {

            GlobalProperties props = globalPropertiesRepo.findAll().get(0);
            String queueName =
                "node-queue-" + Inet4Address.getLocalHost().getHostName();
            SnsSubscriptionManager subscriptionManager =
                new SnsSubscriptionManager(new AmazonSQSClient(),
                                           new AmazonSNSClient(),
                                           props.getInstanceNotificationTopicArn(),
                                           queueName);

            subscriptionManager.addListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    log.info("message received: " + message);
                    log.info("message body: " + message.getBody());
                    userDetails.removeAll();
                    providerStore.removeAll();
                    
                    
                    
                }
            });
            
            return subscriptionManager;
        } catch (UnknownHostException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }
}