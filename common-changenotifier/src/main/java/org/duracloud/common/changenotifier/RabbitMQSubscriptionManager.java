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
 * @author Shibo Liu
 */

public class RabbitMQSubscriptionManager implements SubscriptionManager {
    private Logger log = LoggerFactory.getLogger(RabbitMQSubscriptionManager.class);
    private Channel mqChannel;
    private String mqHost;
    private String queueName;
    private String queueUrl;
    private String mqUsername;
    private String mqPassword;
    private String exchangeName;
    private String consumerName;
    private boolean initialized = false;
    private List<MessageListener> messageListeners = new ArrayList<>();
    public RabbitMQSubscriptionManager(String host, String exchange, String username, String password,
                                       String queueName) {
        mqHost = host;
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
            factory.setVirtualHost("/");
            factory.setHost(mqHost);
            factory.setPort(5672);
            Connection conn = factory.newConnection();
            mqChannel = conn.createChannel();
            queueUrl = "RabbitMQ-" + conn.getAddress();
            mqChannel.queueDeclare(queueName, true, false, false, null);
            mqChannel.queueBind(queueName, exchangeName, queueName);

            log.info("subscribing consumer {} to queue {} at URL {}", consumerName, queueName, queueUrl);
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
                        log.debug("consumer has been cancelled unexpectedly: " + consumerTag);
                    }

                    @Override
                    public void handleCancelOk(String consumerTag) {
                        // consumer has been cancelled explicitly
                        log.info("consumer has been cancelled successfully: " + consumerTag);
                    }

                    @Override
                    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                       // either the channel or the underlying connection has been shut down.
                        log.debug("either the channel or the underlying connection has been shut down for consumer {} because {}", consumerTag, sig.getReason().toString());
                        initialized = false;
                    }

                });

        } catch (Exception e) {
            log.debug("consumer failed to subscribe: " + e.getMessage(), e);
            initialized = false;
        }

    }

    private void dispatch(String message) {
        log.debug("dispatching message {}", message);
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessage(message);
            } catch (Exception ex) {
                log.error("failed to dispatch message " + message
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
            log.info("unsubscripbed consumer {}", consumerName);
        } catch (IOException e) {
            log.info("error unsubscribing consumer {}", consumerName, e);
        }
    }

    private void deleteQueue() {
        try {
            mqChannel.queueDelete(queueName);
            log.info("deleted queue {}", queueName);
        } catch (IOException e) {
            log.info("error deleting queue {}", queueName, e);
        }
    }

    @Override
    public void disconnect() {
        if (!this.initialized) {
            throw new DuraCloudRuntimeException("this manager is already disconnected");
        }
        log.info("disconnecting");
        log.info("unsubscribing {}", consumerName);
        cancelConsumer();
        log.info("deleting queue {}", queueName);
        deleteQueue();
        this.initialized = false;
        log.info("disconnection complete");
    }
}
