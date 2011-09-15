/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.listener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.duracloud.services.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @author: Bill Branan
 * Date: 9/9/11
 */
public abstract class BaseListenerService extends BaseService
    implements MessageListener {

    protected static final String STORE_ID = "storeId";
    protected static final String SPACE_ID = "spaceId";
    protected static final String CONTENT_ID = "contentId";

    private static final Logger log =
            LoggerFactory.getLogger(BaseListenerService.class);

    private String spaceCreateTopic;
    private String spaceUpdateTopic;
    private String spaceDeleteTopic;
    private String contentCreateTopic;
    private String contentCopyTopic;
    private String contentUpdateTopic;
    private String contentDeleteTopic;

    protected String brokerURL;
    protected AbstractMessageListenerContainer jmsContainer;
    private ActiveMQConnectionFactory connectionFactory;
    private Destination destination;

    protected void initializeMessaging(String messageSelector) {
        initMessageListenerContainer();
        connectionFactory.setBrokerURL(brokerURL);
        jmsContainer.setConnectionFactory(connectionFactory);
        jmsContainer.setDestination(destination);
        jmsContainer.setMessageSelector(messageSelector);
        jmsContainer.setMessageListener(this);
        jmsContainer.start();
        jmsContainer.initialize();
    }

    private void initMessageListenerContainer() {
        if(null == jmsContainer) {
            jmsContainer = new DefaultMessageListenerContainer();
        }
    }

    protected void terminateMessaging() {
        jmsContainer.stop();
    }

    public void onMessage(Message message) {
        String topic = null;
        try {
            topic = ((ActiveMQTopic)message.getJMSDestination()).getTopicName();
            log.debug("Message recieved on topic: " + topic);
        } catch(JMSException jmse) {
            log.error("Error getting message topic name", jmse);
        }

        if (message instanceof MapMessage) {
            handleMapMessage((MapMessage) message, topic);
        } else if (message instanceof TextMessage) {
            handleTextMessage(((TextMessage) message));
        } else {
            String error =
                    "Message received which cannot be processed: " + message;
            log.warn(error);
        }
    }

    private void handleTextMessage(TextMessage message) {
        try {
            String msgText = message.getText();
            String msg =
                    "Text is not a supported message type, " +
                    "message content: " + msgText;
            log.warn(msg);
        } catch (JMSException je) {
            String error =
                    "Error occured processing text message: " + je.getMessage();
            log.error(error);
        }
    }

    protected abstract void handleMapMessage(MapMessage message, String topic);

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public ActiveMQConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getSpaceCreateTopic() {
        return spaceCreateTopic;
    }

    public void setSpaceCreateTopic(String spaceCreateTopic) {
        this.spaceCreateTopic = spaceCreateTopic;
    }

    public String getSpaceUpdateTopic() {
        return spaceUpdateTopic;
    }

    public void setSpaceUpdateTopic(String spaceUpdateTopic) {
        this.spaceUpdateTopic = spaceUpdateTopic;
    }

    public String getSpaceDeleteTopic() {
        return spaceDeleteTopic;
    }

    public void setSpaceDeleteTopic(String spaceDeleteTopic) {
        this.spaceDeleteTopic = spaceDeleteTopic;
    }

    public String getContentCreateTopic() {
        return contentCreateTopic;
    }

    public void setContentCreateTopic(String contentCreateTopic) {
        this.contentCreateTopic = contentCreateTopic;
    }

    public String getContentCopyTopic() {
        return contentCopyTopic;
    }

    public void setContentCopyTopic(String contentCopyTopic) {
        this.contentCopyTopic = contentCopyTopic;
    }

    public String getContentUpdateTopic() {
        return contentUpdateTopic;
    }

    public void setContentUpdateTopic(String contentUpdateTopic) {
        this.contentUpdateTopic = contentUpdateTopic;
    }

    public String getContentDeleteTopic() {
        return contentDeleteTopic;
    }

    public void setContentDeleteTopic(String contentDeleteTopic) {
        this.contentDeleteTopic = contentDeleteTopic;
    }

}
