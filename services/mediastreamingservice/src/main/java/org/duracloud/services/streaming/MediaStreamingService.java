/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.services.ComputeService;
import org.duracloud.services.listener.BaseListenerService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.io.File;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service which provides media streaming capabilities
 *
 * @author Bill Branan
 *         Date: May 12, 2010
 */
public class MediaStreamingService extends BaseListenerService
    implements ComputeService, ManagedService, StreamingUpdateListener {

    private final Logger log = LoggerFactory.getLogger(MediaStreamingService.class);

    private static final String DEFAULT_DURASTORE_HOST = "localhost";
    private static final String DEFAULT_DURASTORE_PORT = "8080";
    private static final String DEFAULT_DURASTORE_CONTEXT = "durastore";
    private static final String DEFAULT_MEDIA_SOURCE_SPACE_ID = "media-source";
    private static final String DEFAULT_MEDIA_VIEWER_SPACE_ID = "media-viewer";

    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String username;
    private String password;    
    private String mediaViewerSpaceId;
    private String mediaSourceSpaceId;

    private ContentStore contentStore;
    private List<EnableStreamingWorker> streamingWorkers;
    private ExecutorService streamingExecutor;
    private ExecutorService updateExecutor;
    private int updateAdditions;
    private String[] mediaSourceSpaceIds;

    @Override
    public void start() throws Exception {
        log("Starting Media Streaming Service as " + username);
        this.setServiceStatus(ServiceStatus.STARTING);
        
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(duraStoreHost,
                                        duraStorePort,
                                        duraStoreContext);
        storeManager.login(new Credential(username, password));
        contentStore = storeManager.getPrimaryContentStore();

        PlaylistCreator playlistCreator = new PlaylistCreator();

        File workDir = new File(getServiceWorkDir());
        workDir.setWritable(true);

        mediaSourceSpaceIds = mediaSourceSpaceId.split(",");

        // Start enable streaming worker threads
        streamingWorkers = new LinkedList<EnableStreamingWorker>();
        streamingExecutor = Executors.newSingleThreadExecutor();
        for(String spaceId : mediaSourceSpaceIds) {
            EnableStreamingWorker worker =
                new EnableStreamingWorker(contentStore,
                                          mediaViewerSpaceId,
                                          spaceId,
                                          playlistCreator,
                                          workDir);
            streamingWorkers.add(worker);
            streamingExecutor.execute(worker);
        }

        updateAdditions = 0;
        updateExecutor = Executors.newCachedThreadPool();

        // Set message selector
        initializeMessaging(createMessageSelector(mediaSourceSpaceIds));

        super.start();
        setServiceStatus(ServiceStatus.PROCESSING);
    }

    protected String createMessageSelector(String[] spaceIds) {
        String messageSelector = "";
        Iterator<String> spaceIdsIterator =
            Arrays.asList(spaceIds).iterator();
        while(spaceIdsIterator.hasNext()) {
            String select = SPACE_ID + " = '" + spaceIdsIterator.next() + "'";
            messageSelector += "(" + select + ")";
            if(spaceIdsIterator.hasNext()) {
                messageSelector += " OR ";
            }
        }
        return messageSelector;
    }

    @Override
    public void stop() throws Exception {
        log("Stopping Media Streaming Service");
        this.setServiceStatus(ServiceStatus.STOPPING);

        // Stop listening for new additions to the space
        terminateMessaging();

        // Shut down update executor
        updateExecutor.shutdown();

        // Start disable streaming worker threads
        for(String spaceId : mediaSourceSpaceIds) {
            DisableStreamingWorker worker =
                new DisableStreamingWorker(contentStore, spaceId);
            streamingExecutor.execute(worker);
        }

        // Indicate that streaming executor should shut down after
        // all tasks have completed.
        streamingExecutor.shutdown();

        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public Map<String, String> getServiceProps() {
        // Get the base properties set
        Map<String, String> props = super.getServiceProps();

        // Set data from workers
        boolean complete = true;
        for(EnableStreamingWorker worker : streamingWorkers) {
            if(worker != null) {
                String spaceId = worker.getMediaSourceSpaceId();
                if(worker.isComplete()) {
                    String streamHost = worker.getStreamHost();
                    if(streamHost != null) {
                        props.put("Streaming Host for " + spaceId, streamHost);
                        props.put("RTMP Streaming URL for " + spaceId,
                                  "rtmp://"+streamHost+"/cfx/st");
                    }

                    String enableStreamingResult =
                        worker.getEnableStreamingResult();
                    if(enableStreamingResult != null) {
                        props.put("Enable Streaming Result for " + spaceId,
                                  enableStreamingResult);
                    }
                } else {
                    complete = false;
                }

                String error = worker.getError();
                if(error != null) {
                    props.put(ComputeService.ERROR_KEY, error);
                }
            }
        }

        if(complete) {
            this.setServiceStatus(ServiceStatus.STARTED);
            props.put(ComputeService.STATUS_KEY, getServiceStatus().name());
        }

        if(updateAdditions > 0) {
            props.put("Streaming files added after service start",
                      String.valueOf(updateAdditions));
        }

        return props;
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        log("Attempt made to update Media Streaming Service configuration " +
            "via updated method. Updates should occur via class setters.");
    }

    public String getDuraStoreHost() {
        return duraStoreHost;
    }

    public void setDuraStoreHost(String duraStoreHost) {
        if(duraStoreHost != null && !duraStoreHost.equals("") ) {
            this.duraStoreHost = duraStoreHost;
        } else {
            log("Attempt made to set duraStoreHost to " + duraStoreHost +
                ", which is not valid. Setting value to default: " +
                DEFAULT_DURASTORE_HOST);
            this.duraStoreHost = DEFAULT_DURASTORE_HOST;
        }
    }

    public String getDuraStorePort() {
        return duraStorePort;
    }

    public void setDuraStorePort(String duraStorePort) {
        if(duraStorePort != null) {
            this.duraStorePort = duraStorePort;
        } else {
            log("Attempt made to set duraStorePort to null, which is not " +
                "valid. Setting value to default: " + DEFAULT_DURASTORE_PORT);
            this.duraStorePort = DEFAULT_DURASTORE_PORT;
        }
    }

    public String getDuraStoreContext() {
        return duraStoreContext;
    }

    public void setDuraStoreContext(String duraStoreContext) {
        if(duraStoreContext != null && !duraStoreContext.equals("")) {
            this.duraStoreContext = duraStoreContext;
        } else {
            log("Attempt made to set duraStoreContext to null or empty, " +
                "which is not valid. Setting value to default: " +
                DEFAULT_DURASTORE_CONTEXT);
            this.duraStoreContext = DEFAULT_DURASTORE_CONTEXT;
        }
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

    public String getMediaViewerSpaceId() {
        return mediaViewerSpaceId;
    }

    public void setMediaViewerSpaceId(String mediaViewerSpaceId) {
        if(mediaViewerSpaceId != null && !mediaViewerSpaceId.equals("")) {
            this.mediaViewerSpaceId = mediaViewerSpaceId;
        } else {
            log("Attempt made to set mediaViewerSpaceId to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_MEDIA_VIEWER_SPACE_ID);
            this.mediaViewerSpaceId = DEFAULT_MEDIA_VIEWER_SPACE_ID;
        }
    } 
    
    public String getMediaSourceSpaceId() {
        return mediaSourceSpaceId;
    }

    public void setMediaSourceSpaceId(String mediaSourceSpaceId) {
        if(mediaSourceSpaceId != null && !mediaSourceSpaceId.equals("")) {
            this.mediaSourceSpaceId = mediaSourceSpaceId;
        } else {
            log("Attempt made to set mediaSourceSpaceId to to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_MEDIA_SOURCE_SPACE_ID);
            this.mediaSourceSpaceId = DEFAULT_MEDIA_SOURCE_SPACE_ID;
        }
    }

    @Override
    protected void handleMapMessage(MapMessage message, String topic) {
        try {
            String spaceId = message.getString(SPACE_ID);
            String contentId = message.getString(CONTENT_ID);

            if(getContentCreateTopic().equals(topic) ||
               getContentCopyTopic().equals(topic)) {
                log.warn("Content item {} added to media space {}, " +
                         "setting permissions for streaming", contentId,
                         spaceId);

                // Give time for the newly added item to become available and
                // ensure there are no conflicts with the add content actions.
                wait(5);

                // Push add item task to thread executor
                AddStreamingItemWorker addItemWorker =
                    new AddStreamingItemWorker(contentStore,
                                               spaceId,
                                               contentId,
                                               this);
                updateExecutor.execute(addItemWorker);
            }
        } catch (JMSException je) {
            String error =
                    "Error occured processing map message: " + je.getMessage();
            log.error(error);
            throw new RuntimeException(error, je);
        }
    }

    @Override
    public void successfulStreamingAddition(String mediaSpaceId,
                                            String mediaContentId) {
        log.info("Attempt to stream added content item {} in " +
                 "space {} was successful", mediaContentId, mediaSpaceId);
        ++updateAdditions;
    }

    @Override
    public void failedStreamingAddition(String mediaSpaceId,
                                        String mediaContentId,
                                        String failureMessage) {
        log.error("Attempt to stream added content item {} in " +
                  "space {} FAILED due to: {}",
                  new Object[] {mediaContentId, mediaSpaceId, failureMessage});
    }

    private void wait(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch(InterruptedException e) {
        }
    }

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}
