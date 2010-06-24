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
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;

/**
 * Service which provides media streaming capabilities
 *
 * @author Bill Branan
 *         Date: May 12, 2010
 */
public class MediaStreamingService extends BaseService implements ComputeService,
                                                                  ManagedService {

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
    private EnableStreamingWorker worker;

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
       
        // Start worker thread
        worker = new EnableStreamingWorker(contentStore,
                                           mediaViewerSpaceId,
                                           mediaSourceSpaceId,
                                           playlistCreator,
                                           workDir);
        Thread workerThread = new Thread(worker);
        workerThread.start();

        this.setServiceStatus(ServiceStatus.STARTED);        
    }

    @Override
    public void stop() throws Exception {
        log("Stopping Media Streaming Service");
        this.setServiceStatus(ServiceStatus.STOPPING);
        
        // Start worker thread
        DisableStreamingWorker worker =
            new DisableStreamingWorker(contentStore, mediaSourceSpaceId);
        Thread workerThread = new Thread(worker);
        workerThread.start();
        
        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();

        boolean complete = false;
        if(worker != null) {
            String streamHost = worker.getStreamHost();
            String enableStreamingResult = worker.getEnableStreamingResult();
            String error = worker.getError();
            complete = worker.isComplete();

            if(streamHost != null) {
                props.put("Streaming Host", streamHost);
            }

            if(enableStreamingResult != null) {
                props.put("Results of Enabling Streaming",
                          enableStreamingResult);
            }

            if(error != null) {
                props.put("Errors Encountered", error);
            }
        }

        if(complete) {
            props.put("Service Status", "Complete");
        } else {
            props.put("Service Status", "Enabling Streaming...");
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

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}
