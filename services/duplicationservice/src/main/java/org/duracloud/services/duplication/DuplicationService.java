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
import org.duracloud.common.util.DateUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.duplication.impl.ContentDuplicatorImpl;
import org.duracloud.services.duplication.impl.ContentDuplicatorReportingImpl;
import org.duracloud.services.duplication.impl.SpaceDuplicatorImpl;
import org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImpl;
import org.duracloud.services.duplication.result.DuplicationResultListener;
import org.duracloud.services.duplication.result.ResultListener;
import org.duracloud.services.listener.BaseListenerService;
import org.duracloud.services.ComputeService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Dictionary;

public class DuplicationService extends BaseListenerService implements ComputeService, ManagedService {

    private static final Logger log =
        LoggerFactory.getLogger(DuplicationService.class);

    private String host;

    private String port;

    private String context;

    private String username;

    private String password;

    private String fromStoreId;

    private String toStoreId;

    private String outputSpaceId;

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

        ContentStoreManager storeManager = new ContentStoreManagerImpl(host,
                                                                       port,
                                                                       context);

        storeManager.login(credential);

        ContentStore fromStore;
        ContentStore toStore;
        ContentStore primaryStore;
        try {
            fromStore = storeManager.getContentStore(fromStoreId);
            toStore = storeManager.getContentStore(toStoreId);
            primaryStore = storeManager.getPrimaryContentStore();

        } catch (ContentStoreException cse) {
            String error = "Unable to create connections to content " +
                "stores for duplication " + cse.getMessage();
            log.error(error);
            super.setError(error);
            return;
        }

        super.start();

        String reportId = getReportContentId(fromStore, toStore);
        super.setReportId(outputSpaceId, reportId);

        ResultListener listener = new DuplicationResultListener(primaryStore,
                                                                outputSpaceId,
                                                                reportId,
                                                                super.getServiceWorkDir());

        spaceDuplicator = getSpaceDuplicator(fromStore, toStore, listener);
        contentDuplicator = getContentDuplicator(fromStore, toStore, listener);

        log.info("reportId: " + reportId);
        log.info("Listener container started: " + jmsContainer.isRunning());
        log.info("**********");
        log.info("Duplication Service Listener Started");

        setServiceStatus(ServiceStatus.STARTED);
    }

    private String getReportContentId(ContentStore fromStore,
                                      ContentStore toStore) {
        StringBuilder reportId = new StringBuilder("duplication-on-change/");
        reportId.append(fromStore.getStorageProviderType());
        reportId.append("-to-");
        reportId.append(toStore.getStorageProviderType());
        reportId.append("-report-");
        reportId.append(DateUtil.nowMid());
        reportId.append(".csv");
        return reportId.toString();
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping Duplication Service");
        terminateMessaging();
        contentDuplicator.stop();
        spaceDuplicator.stop();
        setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    protected void handleMapMessage(MapMessage message, String topic) {
        try {
            String spaceId = message.getString(SPACE_ID);
            String contentId = message.getString(CONTENT_ID);

            if (getSpaceCreateTopic().equals(topic)) {
                spaceDuplicator.createSpace(spaceId);

            } else if (getSpaceUpdateTopic().equals(topic)) {
                spaceDuplicator.updateSpace(spaceId);

            } else if (getSpaceDeleteTopic().equals(topic)) {
                spaceDuplicator.deleteSpace(spaceId);

            } else if (getContentCreateTopic().equals(topic)) {
                contentDuplicator.createContent(spaceId, contentId);

            } else if (getContentCopyTopic().equals(topic)) {
                contentDuplicator.createContent(spaceId, contentId);

            } else if (getContentUpdateTopic().equals(topic)) {
                contentDuplicator.updateContent(spaceId, contentId);

            } else if (getContentDeleteTopic().equals(topic)) {
                contentDuplicator.deleteContent(spaceId, contentId);
            }
        } catch (JMSException je) {
            String error =
                "Error occured processing map message: " + je.getMessage();
            log.error(error);
            super.setError(error);
            throw new RuntimeException(error, je);
        }
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary properties) throws ConfigurationException {
        // Implementation not needed. Update performed through setters.
    }

    private SpaceDuplicator getSpaceDuplicator(ContentStore fromStore,
                                               ContentStore toStore,
                                               ResultListener listener) {
        if (null == spaceDuplicator) {
            SpaceDuplicator sd = new SpaceDuplicatorImpl(fromStore, toStore);
            spaceDuplicator = new SpaceDuplicatorReportingImpl(sd, listener);
        }
        return spaceDuplicator;
    }

    private ContentDuplicator getContentDuplicator(ContentStore fromStore,
                                                   ContentStore toStore,
                                                   ResultListener listener) {
        if (null == contentDuplicator) {
            SpaceDuplicator sd = getSpaceDuplicator(fromStore,
                                                    toStore,
                                                    listener);
            ContentDuplicator cd = new ContentDuplicatorImpl(fromStore,
                                                             toStore,
                                                             sd);
            contentDuplicator = new ContentDuplicatorReportingImpl(cd,
                                                                   listener);
        }
        return contentDuplicator;
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

    public String getOutputSpaceId() {
        return outputSpaceId;
    }

    public void setOutputSpaceId(String outputSpaceId) {
        this.outputSpaceId = outputSpaceId;
    }
}