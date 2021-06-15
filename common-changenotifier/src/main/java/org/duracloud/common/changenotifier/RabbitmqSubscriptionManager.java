/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.changenotifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles connections to and interactions with RabbitMQ
 *
 * @author Shibo Liu
 * Feb 29, 2020
 */
public class RabbitmqSubscriptionManager implements SubscriptionManager {
    private Logger log = LoggerFactory.getLogger(RabbitmqSubscriptionManager.class);
    private Channel mqChannel;
    private String mqHost;
    private Integer mqPort;
    private String mqVhost;
    private String queueName;
    private String queueUrl;
    private String mqUsername;
    private String mqPassword;
    private String exchangeName;
    private String consumerName;
    private boolean initialized = false;
    private List<MessageListener> messageListeners = new ArrayList<>();
    public RabbitmqSubscriptionManager(String host, Integer port, String vhost,
                                       String exchange, String username, String password,
                                       String queueName) {
        mqHost = host;
        mqPort = port;
        mqVhost = vhost;
        exchangeName = exchange;
        mqUsername = username;
        mqPassword = password;
        this.queueName = queueName;
        consumerName = "consumer-" + queueName;
    }

    @Override
    public void addListener(MessageListener listener) {

        this.messageListeners.add(listener);
    }

    @Override
    public synchronized void connect() {
        if (initialized) {
            throw new DuraCloudRuntimeException("this manager is already connected");
        }

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(mqUsername);
            factory.setPassword(mqPassword);
            factory.setVirtualHost(mqVhost);
            factory.setHost(mqHost);
            factory.setPort(mqPort);
            Connection conn = factory.newConnection();
            mqChannel = conn.createChannel();
            queueUrl = "(RabbitMQ) " + conn.getAddress();
            mqChannel.queueDeclare(queueName, true, false, false, null);
            mqChannel.queueBind(queueName, exchangeName, queueName);

            log.info("Subscribing consumer {} to queue {} on vhost {} at URL {}",
                     consumerName, queueName, mqVhost, queueUrl);
            startConsumer();

        } catch (Exception ex) {
            initialized = false;
            log.error("failed to estabilish connection to RabbitMQ with queue name {} and URL {} because {}", queueName,
                      queueUrl, ex.getMessage());
            throw new DuraCloudRuntimeException(ex);
        }

    }

    private void startConsumer() {
        try {
            mqChannel.basicConsume(queueName, false, consumerName,
                new DefaultConsumer(mqChannel) {

                    @Override
                    public void handleDelivery(String consumerTag,
                                              Envelope envelope,
                                              AMQP.BasicProperties properties,
                                              byte[] body)
                        throws IOException {
                        long deliveryTag = envelope.getDeliveryTag();
                        String message = new String(body);
                        dispatch(message);
                        log.debug("{} dispatched", message);
                        mqChannel.basicAck(deliveryTag, false);
                        log.debug("{} deleted", message);
                    }

                    @Override
                    public void handleConsumeOk(String consumerTag) {
                        log.info("Consumer registered: {}", consumerTag);
                        initialized = true;
                    }

                    @Override
                    public void handleCancel(String consumerTag) {
                        // consumer has been cancelled unexpectedly
                        log.warn("Consumer has been cancelled unexpectedly: " + consumerTag);
                    }

                    @Override
                    public void handleCancelOk(String consumerTag) {
                        // consumer has been cancelled explicitly
                        log.info("Consumer has been cancelled successfully: " + consumerTag);
                    }

                    @Override
                    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                       // either the channel or the underlying connection has been shut down.
                        log.warn("Either the channel or the underlying connection has been shut down" +
                                  " for consumer {} because {}", consumerTag, sig.getReason().toString());
                        initialized = false;
                    }

                });

        } catch (Exception e) {
            log.error("Consumer failed to subscribe: " + e.getMessage(), e);
            initialized = false;
        }

    }

    private void dispatch(String message) {
        log.debug("Dispatching message {}", message);
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessage(message);
            } catch (Exception ex) {
                log.error("Failed to dispatch message " + message
                          + " to "
                          + listener
                          + "due to "
                          + ex.getMessage(),
                          ex);
            }
        }
    }

    private void cancelConsumer() {
        try {
            mqChannel.basicCancel(consumerName);
            log.info("Unsubscripbed consumer {}", consumerName);
        } catch (IOException e) {
            log.error("Error unsubscribing consumer {}", consumerName, e);
        }
    }

    private void deleteQueue() {
        try {
            mqChannel.queueDelete(queueName);
            log.info("Deleted queue {}", queueName);
        } catch (IOException e) {
            log.error("Error deleting queue {}", queueName, e);
        }
    }

    @Override
    public void disconnect() {
        if (!this.initialized) {
            throw new DuraCloudRuntimeException("This manager is already disconnected");
        }
        log.info("Disconnecting");
        log.info("Unsubscribing {}", consumerName);
        cancelConsumer();
        log.info("Deleting queue {}", queueName);
        deleteQueue();
        this.initialized = false;
        log.info("Disconnection complete");
    }
}
