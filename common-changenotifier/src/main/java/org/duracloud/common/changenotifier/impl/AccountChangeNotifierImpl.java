/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.changenotifier.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.duracloud.account.db.model.GlobalProperties;
import org.duracloud.account.db.model.RabbitmqConfig;
import org.duracloud.account.db.repo.GlobalPropertiesRepo;
import org.duracloud.common.changenotifier.AccountChangeNotifier;
import org.duracloud.common.changenotifier.NotifierType;
import org.duracloud.common.event.AccountChangeEvent;
import org.duracloud.common.event.AccountChangeEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Bernstein
 */
@Component("accountChangeNotifier")
public class AccountChangeNotifierImpl implements AccountChangeNotifier {

    private AmazonSNS snsClient;

    private Channel rabbitmqChannel;

    private String rabbitmqExchange;

    private String rabbitmqVhost;

    private NotifierType notifierType;

    private GlobalPropertiesRepo globalPropertiesRepo;

    private static Logger log = LoggerFactory.getLogger(AccountChangeNotifierImpl.class);

    /**
     * @param globalPropertiesRepo
     */
    @Autowired
    public AccountChangeNotifierImpl(GlobalPropertiesRepo globalPropertiesRepo) {
        this.globalPropertiesRepo = globalPropertiesRepo;
        GlobalProperties props = null;
        try {
            props = globalPropertiesRepo.findAll().get(0);
            notifierType = NotifierType.fromString(props.getNotifierType());
        } catch (Exception e) {
            notifierType = NotifierType.SNS;
        }
        log.info("Notifier-Type: {}", notifierType.toString());
        if (notifierType == NotifierType.RABBITMQ) {
            rabbitmqExchange = props.getRabbitmqExchange();

            RabbitmqConfig rabbitmqConfig = props.getRabbitmqConfig();
            String rabbitmqHost = rabbitmqConfig.getHost();
            Integer rabbitmqPort = rabbitmqConfig.getPort();
            rabbitmqVhost = rabbitmqConfig.getVhost();
            String rabbitmqUsername = rabbitmqConfig.getUsername();
            String rabbitmqPassword = rabbitmqConfig.getPassword();

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitmqHost);
            factory.setPort(rabbitmqPort);
            factory.setVirtualHost(rabbitmqVhost);
            factory.setUsername(rabbitmqUsername);
            factory.setPassword(rabbitmqPassword);
            log.info("RabbitMQ Host: {}, Vhost: {}, Exchange: {}",
                     rabbitmqHost, rabbitmqVhost, rabbitmqExchange);
            try {
                Connection conn = factory.newConnection();
                rabbitmqChannel = conn.createChannel();
            } catch (Exception e) {
                log.error("Failed to connect to RabbitMQ because: " + e.getMessage(), e);
            }
        } else {
            // Default SNS Client
            log.info("Initiate default SNS client");
            try {
                this.snsClient = AmazonSNSClientBuilder.defaultClient();
            } catch (Exception e) {
                log.error("Failed to initiate SNS client because: " + e.getMessage(), e);
            }
        }
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
            if (notifierType == NotifierType.RABBITMQ) {
                rabbitmqChannel.basicPublish(rabbitmqExchange, "", null,
                                             AccountChangeEvent.serialize(event).getBytes());
                log.info("published event via RabbitMQ, vhost={}, exchange={}, event={}",
                         rabbitmqVhost, rabbitmqExchange, event);
            } else {
                GlobalProperties props = globalPropertiesRepo.findAll().get(0);
                this.snsClient.publish(props.getInstanceNotificationTopicArn(),
                                       AccountChangeEvent.serialize(event));
                log.info("published event via SNS, event={}", event);
            }
        } catch (Exception e) {
            log.error("Failed to publish event: " + event + " : " + e.getMessage(), e);
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
