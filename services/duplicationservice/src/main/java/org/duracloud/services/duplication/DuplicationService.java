/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.listener.BaseListenerService;
import org.duracloud.services.ComputeService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Dictionary;

public class DuplicationService extends BaseListenerService
        implements ComputeService, ManagedService {

    private static final Logger log =
            LoggerFactory.getLogger(DuplicationService.class);

    private String host;

    private String port;

    private String context;

    private String username;

    private String password;

    private String fromStoreId;

    private String toStoreId;

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

        String messageSelector = STORE_ID + " = '" + fromStoreId + "'";
        initializeMessaging(messageSelector);

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

        log.info("Listener container started: " + jmsContainer.isRunning());
        log.info("**********");
        log.info("Duplication Service Listener Started");
        super.start();
        setServiceStatus(ServiceStatus.STARTED);
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping Duplication Service");
        terminateMessaging();
        setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    protected void handleMapMessage(MapMessage message, String topic) {
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
            else if(getContentCopyTopic().equals(topic)) {
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

}