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
import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.DateUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.ComputeService;
import org.duracloud.services.duplication.impl.ContentDuplicatorImpl;
import org.duracloud.services.duplication.impl.ContentDuplicatorReportingImpl;
import org.duracloud.services.duplication.impl.DuplicatorImpl;
import org.duracloud.services.duplication.impl.SpaceDuplicatorImpl;
import org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImpl;
import org.duracloud.services.duplication.result.DuplicationResultListener;
import org.duracloud.services.duplication.result.ResultListener;
import org.duracloud.services.listener.BaseListenerService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class DuplicationService extends BaseListenerService implements ComputeService, ManagedService {

    private static final Logger log =
        LoggerFactory.getLogger(DuplicationService.class);

    private static final String HOST_PROP = "host";
    private static final String PORT_PROP = "port";
    private static final String CONTEXT_PROP = "context";
    private static final String USER_PROP = "username";
    private static final String PASS_PROP = "password";
    private static final String FROM_STORE_PROP = "fromStoreId";
    private static final String BROKER_PROP = "brokerURL";
    protected static final String SPACE_PREFIX = "spaceID-";
    private static final String SPACE_TO_SKIP = "none";
    protected static final String NEW_SPACE_DEFAULT = "default";

    private String host;
    private String port;
    private String context;
    private String username;
    private String password;
    private String fromStoreId;
    private String outputSpaceId;
    private String newSpaceDefault;

    protected Map<String, Duplicator> duplicators;

    private DuplicationResultListener resultListener;

    protected Map<String, String> spacesToWatch;

    /**
     * Used to set the configuration for this service.
     * @param properties
     */
    public void updateConfig(Map<String,?> properties) {
         spacesToWatch = new HashMap<String, String>();

        for(String key : properties.keySet()) {
            String value = (String)properties.get(key);

            if(HOST_PROP.equals(key)) {
                host = value;
            } else if (PORT_PROP.equals(key)) {
                port = value;
            } else if (CONTEXT_PROP.equals(key)) {
               context = value;
            } else if (USER_PROP.equals(key)) {
                username = value;
            } else if (PASS_PROP.equals(key)) {
                password = value;
            } else if (FROM_STORE_PROP.equals(key)) {
                fromStoreId = value;
            } else if (BROKER_PROP.equals(key)) {
                brokerURL = value;
            } else if (NEW_SPACE_DEFAULT.equals(key)) {
                newSpaceDefault = value;
            } else if (null != key && key.startsWith(SPACE_PREFIX)) {
                addWatchedSpace(key.substring(SPACE_PREFIX.length()), value);
            }
        }
    }

    private void addWatchedSpace(String spaceId, String dupStoreList) {
        if(dupStoreList != null &&
           !dupStoreList.isEmpty() &&
           !dupStoreList.equals(SPACE_TO_SKIP) &&
           !Constants.SYSTEM_SPACES.contains(dupStoreList)) {
            spacesToWatch.put(spaceId, dupStoreList);
        }
    }

    @Override
    public void start() throws Exception {
        StringBuilder startupNotice = new StringBuilder();

        Credential credential = new Credential(username, password);

        startupNotice.append("\n\n**********\n");
        startupNotice.append("Starting Duplication Service\n");
        startupNotice.append("host: " + host + "\n");
        startupNotice.append("port: " + port + "\n");
        startupNotice.append("context: " + context + "\n");
        startupNotice.append("brokerURL: " + brokerURL + "\n");
        startupNotice.append("credential: " + credential + "\n");
        startupNotice.append("fromStoreId: " + fromStoreId + "\n");
        startupNotice.append("----\n");
        startupNotice.append("New Space (to storeIds):" +
                             newSpaceDefault + "\n");
        startupNotice.append("Duplicating Spaces (to storeIDs)\n");
        for(String spaceId : spacesToWatch.keySet()) {
            startupNotice.append(
                spaceId + ": " + spacesToWatch.get(spaceId) + "\n");
        }
        startupNotice.append("----\n");

        String messageSelector = STORE_ID + " = '" + fromStoreId + "'";
        for(String systemSpace : Constants.SYSTEM_SPACES) {
            messageSelector +=
                (" AND " + SPACE_ID + " <> '" + systemSpace + "'");
        }
        initializeMessaging(messageSelector);
        log.info("Setting duplication message selector to: " + messageSelector);

        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(host, port, context);

        storeManager.login(credential);

        ContentStore primaryStore;
        ContentStore fromStore;
        Map<String, ContentStore> toStores;

        try {
            primaryStore = storeManager.getPrimaryContentStore();
            fromStore = storeManager.getContentStore(fromStoreId);
            toStores = storeManager.getContentStores();
        } catch (ContentStoreException cse) {
            String error = "Unable to create connections to content " +
                "stores for duplication " + cse.getMessage();
            log.error(error);
            super.setError(error);
            return;
        }

        super.start();

        String reportId = getReportContentId(fromStore);
        super.setReportId(outputSpaceId, reportId);

        String errorReportId = getErrorReportContentId(fromStore);
        super.setErrorReportId(outputSpaceId, errorReportId);

        // Create the result listener, which will record duplication results
        this.resultListener =
            new DuplicationResultListener(primaryStore,
                                          outputSpaceId,
                                          reportId,
                                          errorReportId,
                                          super.getServiceWorkDir());

        // Create the duplicators, one for each of the stores to which content
        // can be duplicated (meaning all stores except the from store).
        duplicators = new HashMap<String, Duplicator>();
        for(String storeId : toStores.keySet()) {
            if(!fromStoreId.equals(storeId)) {
                ContentStore toStore = toStores.get(storeId);
                Duplicator duplicator =
                    createDuplicator(fromStore, toStore, this.resultListener);
                duplicators.put(storeId, duplicator);
            }
        }

        startupNotice.append("reportSpaceId: " + outputSpaceId + "\n");
        startupNotice.append("reportId: " + reportId + "\n");
        startupNotice.append("Listener container started: " +
                             jmsContainer.isRunning() + "\n");
        startupNotice.append("**********\n");
        log.info(startupNotice.toString());

        setServiceStatus(ServiceStatus.STARTED);
    }

    private String getReportContentId(ContentStore fromStore) {
        return getReportContentId(fromStore, false);
    }

    private String getErrorReportContentId(ContentStore fromStore) {
        return getReportContentId(fromStore, true);
    }

    private String getReportContentId(ContentStore fromStore,
                                      boolean errorReport) {
        StringBuilder reportId = new StringBuilder("duplication-on-change/");
        reportId.append("from-");
        reportId.append(fromStore.getStorageProviderType());
        reportId.append("-report-");
        reportId.append(DateUtil.nowPlain());
        if(errorReport){
            reportId.append(".errors");
        }
        reportId.append(".tsv");
        return reportId.toString();
    }

    @Override
    public Map<String,String> getServiceProps(){
        Map<String,String> props = super.getServiceProps();
        DuplicationResultListener rl = this.resultListener;
        if(rl !=null) {
            long passCount = rl.getPassCount();
            long failedCount = rl.getFailedCount();
            long totalCount = passCount+failedCount;
            
            if(failedCount > 0){
                props.put(ComputeService.FAILURE_COUNT_KEY,
                          String.valueOf(failedCount));
                props.put(ComputeService.ERROR_REPORT_KEY,
                          getErrorReportId());
            }
            
            if(totalCount == 0){
                props.remove(ComputeService.REPORT_KEY);
            }
            
            props.put(ComputeService.PASS_COUNT_KEY,
                      String.valueOf(passCount));
            props.put(ComputeService.ITEMS_PROCESS_COUNT,
                      String.valueOf(totalCount));
        }
        return props;
    }
    @Override
    public void stop() throws Exception {
        log.info("Stopping Duplication Service");
        terminateMessaging();
        for(Duplicator duplicator : duplicators.values()) {
            duplicator.stop();
        }
        setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    protected void handleMapMessage(MapMessage message, String topic) {
        try {
            String spaceId = message.getString(SPACE_ID);
            String contentId = message.getString(CONTENT_ID);
            handleDuplicationMessage(topic, spaceId, contentId);
        } catch (JMSException je) {
            String error =
                "Error occured processing map message: " + je.getMessage();
            log.error(error);
            super.setError(error);
            throw new RuntimeException(error, je);
        }
    }

    protected void handleDuplicationMessage(String topic,
                                            String spaceId,
                                            String contentId) {
        if (getSpaceCreateTopic().equals(topic)) {
            addWatchedSpace(spaceId, newSpaceDefault);
        }

        String dupStores = spacesToWatch.get(spaceId);
        if(null != dupStores) {
            for(String storeId : dupStores.split(",")) {
                Duplicator duper = duplicators.get(storeId);
                if(null != duper) {
                    processDuplication(topic, spaceId, contentId, duper);
                }
            }
        }
    }

    protected void processDuplication(String topic,
                                      String spaceId,
                                      String contentId,
                                      Duplicator duplicator) {
        if (getSpaceCreateTopic().equals(topic)) {
            duplicator.createSpace(spaceId);
        } else if (getSpaceUpdateTopic().equals(topic)) {
            duplicator.updateSpace(spaceId);
        } else if (getSpaceUpdateAclTopic().equals(topic)) {
            duplicator.updateSpaceAcl(spaceId);
        } else if (getSpaceDeleteTopic().equals(topic)) {
            duplicator.deleteSpace(spaceId);
        } else if (getContentCreateTopic().equals(topic)) {
            duplicator.createContent(spaceId, contentId);
        } else if (getContentCopyTopic().equals(topic)) {
            duplicator.createContent(spaceId, contentId);
        } else if (getContentUpdateTopic().equals(topic)) {
            duplicator.updateContent(spaceId, contentId);
        } else if (getContentDeleteTopic().equals(topic)) {
            duplicator.deleteContent(spaceId, contentId);
        }
        log.debug("handling message for item: {}/{}, on topic: {}",
                 new Object[]{spaceId, contentId, topic});
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary properties) throws ConfigurationException {
        // Implementation not needed. Update performed through updateConfig().
    }

    private Duplicator createDuplicator(ContentStore fromStore,
                                        ContentStore toStore,
                                        ResultListener listener) {
        SpaceDuplicator sd = new SpaceDuplicatorImpl(fromStore, toStore);
        SpaceDuplicator spaceDuplicator =
            new SpaceDuplicatorReportingImpl(sd, listener);

        ContentDuplicator cd =
            new ContentDuplicatorImpl(fromStore, toStore, spaceDuplicator);
        ContentDuplicator contentDuplicator =
            new ContentDuplicatorReportingImpl(cd, listener);

        return new DuplicatorImpl(spaceDuplicator, contentDuplicator);
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getContext() {
        log.debug("getContext(): " + context);
        return context;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFromStoreId() {
        return fromStoreId;
    }

    public String getOutputSpaceId() {
        return outputSpaceId;
    }

    public void setOutputSpaceId(String outputSpaceId) {
        this.outputSpaceId = outputSpaceId;
    }

}