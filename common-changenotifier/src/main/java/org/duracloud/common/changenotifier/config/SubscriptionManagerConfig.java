/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.common.changenotifier.config;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.duracloud.account.db.model.GlobalProperties;
import org.duracloud.account.db.repo.GlobalPropertiesRepo;
import org.duracloud.common.cache.AccountComponentCache;
import org.duracloud.common.changenotifier.MessageListener;
import org.duracloud.common.changenotifier.RabbitMQSubscriptionManager;
import org.duracloud.common.changenotifier.SnsSubscriptionManager;
import org.duracloud.common.changenotifier.SubscriptionManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.event.AccountChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Daniel Bernstein
 */
@Configuration
public class SubscriptionManagerConfig {
    private Logger log = LoggerFactory.getLogger(SubscriptionManagerConfig.class);

    @Bean(destroyMethod = "disconnect", initMethod = "connect")
    public SubscriptionManager subscriptionManager(GlobalPropertiesRepo globalPropertiesRepo,
                                                   final List<AccountComponentCache<?>> componentCaches,
                                                   String appName) {
        try {

            GlobalProperties props = globalPropertiesRepo.findAll().get(0);
            String queueName = "node-queue-" + appName + "-" +
                               Inet4Address.getLocalHost().getHostName().replace(".", "_");

            SubscriptionManager subscriptionManager;

            if (props.getNotifierType().equalsIgnoreCase("RABBITMQ")) {
                //RabbitMQ
                subscriptionManager =
                    new RabbitMQSubscriptionManager(props.getRabbitmqHost(),
                                                    props.getRabbitmqPort(),
                                                    props.getRabbitmqVhost(),
                                                    props.getRabbitmqExchange(),
                                                    props.getRabbitmqUsername(),
                                                    props.getRabbitmqPassword(), queueName);
            } else {
                //SNS
                subscriptionManager =
                    new SnsSubscriptionManager(AmazonSQSClientBuilder.defaultClient(),
                                               AmazonSNSClientBuilder.defaultClient(),
                                               props.getInstanceNotificationTopicArn(), queueName);
            }

            subscriptionManager.addListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    log.info("message received: " + message);
                    log.debug("message body: " + message.getBody());
                    JsonFactory factory = new JsonFactory();
                    ObjectMapper mapper = new ObjectMapper(factory);
                    TypeReference<HashMap<String, String>> typeRef =
                        new TypeReference<HashMap<String, String>>() {
                        };
                    String body = message.getBody();
                    try {
                        Map<String, String> map = mapper.readValue(body, typeRef);
                        AccountChangeEvent event = AccountChangeEvent.deserialize(map.get("Message"));
                        for (AccountComponentCache<?> cache : componentCaches) {
                            cache.onEvent(event);
                        }
                    } catch (IOException e) {
                        log.warn("unable to dispatch message: " + message + " : " + e.getMessage(), e);
                    }
                }

                @Override
                public void onMessage(String message) {
                    log.info("message received: " + message);
                    try {
                        AccountChangeEvent event = AccountChangeEvent.deserialize(message);
                        for (AccountComponentCache<?> cache : componentCaches) {
                            cache.onEvent(event);
                        }
                    } catch (Exception e) {
                        log.warn("unable to dispatch message: " + message + " : " + e.getMessage(), e);
                    }
                }
            });

            return subscriptionManager;
        } catch (UnknownHostException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

}