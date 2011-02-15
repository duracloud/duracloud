/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
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
import java.util.Dictionary;

public class DuplicationService extends BaseService
        implements ComputeService, MessageListener, ManagedService {

    protected static final String STORE_ID = "storeId";

    protected static final String SPACE_ID = "spaceId";

    protected static final String CONTENT_ID = "contentId";

    private static final Logger log =
            LoggerFactory.getLogger(DuplicationService.class);

    private String host;

    private String port;

    private String context;

    private String brokerURL;

    private String username;

    private String password;

    private String fromStoreId;

    private String toStoreId;

    private String spaceCreateTopic;

    private String spaceUpdateTopic;

    private String spaceDeleteTopic;

    private String contentCreateTopic;

    private String contentUpdateTopic;

    private String contentDeleteTopic;

    private AbstractMessageListenerContainer jmsContainer;

    private ActiveMQConnectionFactory connectionFactory;

    private Destination destination;

    private SpaceDuplicator spaceDuplicator;

    private ContentDuplicator contentDuplicator;

    @Override
    public void start() throws Exception {
        log.info("Starting Duplication Service");

        Credential credential = new Credential(username, password);

        log.info("**********");
        log.info("Starting duplication service");
        log.info("host: " + host);
        log.info("port: " + port);
        log.info("context: " + context);
        log.info("brokerURL: " + brokerURL);
        log.info("credential: " + credential);
        log.info("fromStoreId: " + fromStoreId);
        log.info("toStoreId: " + toStoreId);

        jmsContainer = new DefaultMessageListenerContainer();
        connectionFactory.setBrokerURL(brokerURL);
        jmsContainer.setConnectionFactory(connectionFactory);
        jmsContainer.setDestination(destination);
        jmsContainer.setMessageSelector(STORE_ID + " = '" + fromStoreId + "'");
        jmsContainer.setMessageListener(this);
        jmsContainer.start();
        jmsContainer.initialize();

        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(host, port, context);

        storeManager.login(credential);

        ContentStore fromStore = null;
        ContentStore toStore = null;
        try {
            fromStore = storeManager.getContentStore(fromStoreId);
            toStore = storeManager.getContentStore(toStoreId);
        } catch(ContentStoreException cse) {
            String error = "Unable to create connections to content " +
            		       "stores for duplication " + cse.getMessage();
            log.error(error);
        }

        spaceDuplicator = new SpaceDuplicator(fromStore,
                                              toStore);

        contentDuplicator = new ContentDuplicator(fromStore,
                                              toStore);

        log.info("Listener container started: ");
        log.info("jmsContainer.isRunning()");
        log.info("**********");
        log.info("Duplication Service Listener Started");
        setServiceStatus(ServiceStatus.STARTED);
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping Duplication Service");
        jmsContainer.stop();
        setServiceStatus(ServiceStatus.STOPPED);
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

    public void onMessage(Message message) {
        if (log.isDebugEnabled()) {
            log.debug("Message recieved in Duplication Service: " + message);
        }

        String topic = null;

        try {
            topic = ((ActiveMQTopic)message.getJMSDestination()).getTopicName();
            log.debug("Message topic name- "+topic);
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
                    "Text message received in duplication service: " + msgText;
            log.warn(msg);
        } catch (JMSException je) {
            String error =
                    "Error occured processing text message: " + je.getMessage();
            log.error(error);
            throw new RuntimeException(error, je);
        }
    }

    private void handleMapMessage(MapMessage message, String topic) {
        try {
            String spaceId = message.getString(SPACE_ID);
            String contentId = message.getString(CONTENT_ID);

            if(getSpaceCreateTopic().equals(topic)) {
                spaceDuplicator.createSpace(spaceId);
            }
            else if(getSpaceUpdateTopic().equals(topic)) {
                spaceDuplicator.updateSpace(spaceId);
            }
            else if(getSpaceDeleteTopic().equals(topic)) {
                spaceDuplicator.deleteSpace(spaceId);
            }
            else if(getContentCreateTopic().equals(topic)) {
                contentDuplicator.createContent(spaceId, contentId);
            }
            else if(getContentUpdateTopic().equals(topic)) {
                contentDuplicator.updateContent(spaceId, contentId);
            }
            else if(getContentDeleteTopic().equals(topic)) {
                contentDuplicator.deleteContent(spaceId, contentId);
            }
        } catch (JMSException je) {
            String error =
                    "Error occured processing map message: " + je.getMessage();
            log.error(error);
            throw new RuntimeException(error, je);
        }
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary properties) throws ConfigurationException {
        // Implementation not needed. Update performed through setters.
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getContext() {
        log.debug("getContext(): " + context);
        return context;
    }

    public void setContext(String context) {
        log.debug("setContext(): " + context);
        this.context = context;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFromStoreId() {
        return fromStoreId;
    }

    public void setFromStoreId(String fromStoreId) {
        this.fromStoreId = fromStoreId;
    }

    public String getToStoreId() {
        return toStoreId;
    }

    public void setToStoreId(String toStoreId) {
        this.toStoreId = toStoreId;
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