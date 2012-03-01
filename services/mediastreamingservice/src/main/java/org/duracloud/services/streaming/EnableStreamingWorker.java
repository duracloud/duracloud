/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ExceptionUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Jun 3, 2010
 */
public class EnableStreamingWorker implements Runnable {

    private static final String ENABLE_STREAMING_TASK = "enable-streaming";

    private final Logger log = LoggerFactory.getLogger(EnableStreamingWorker.class);

    private ContentStore contentStore;
    private String mediaSourceSpaceId;

    private boolean complete = false;
    private String streamHost = null;
    private String enableStreamingResult = null;
    private String error = null;

    public EnableStreamingWorker(ContentStore contentStore,
                                 String mediaSourceSpaceId) {
        this.contentStore = contentStore;
        this.mediaSourceSpaceId = mediaSourceSpaceId;
    }

    @Override
    public void run() {
        try {
            createDistribution();

        } catch(Exception e) {
            log("Error encountered performing " + ENABLE_STREAMING_TASK +
                " task: " + e.getMessage(), e);
            error = e.getMessage();
        }
        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getStreamHost() {
        return streamHost;
    }

    public String getEnableStreamingResult() {
        return enableStreamingResult;
    }

    public String getError() {
        return error;        
    }

    public String getMediaSourceSpaceId() {
        return mediaSourceSpaceId;
    }

    /*
     * Create/enable distribution
     */
    private void createDistribution() throws ContentStoreException {
        String enableStreamingResponse =
            contentStore.performTask(ENABLE_STREAMING_TASK, mediaSourceSpaceId);
        Map<String, String> responseMap =
            SerializationUtil.deserializeMap(enableStreamingResponse);
        streamHost = responseMap.get("domain-name");
        enableStreamingResult = responseMap.get("results");
    }

    private void log(String logMsg, Exception e) {
        log.error(logMsg, e);
        log.error(ExceptionUtil.getStackTraceAsString(e));
    }
}
