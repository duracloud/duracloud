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
 * Date: Sept 13, 2010
 */
public class AddStreamingItemWorker implements Runnable {

    private static final String ADD_STREAMING_ITEM_TASK = "add-streaming-item";

    private final Logger log =
        LoggerFactory.getLogger(AddStreamingItemWorker.class);

    private ContentStore contentStore;
    private String mediaSpaceId;
    private String mediaContentId;
    private StreamingUpdateListener updateListener;

    private String addStreamingItemResult = null;
    private String error = null;

    public AddStreamingItemWorker(ContentStore contentStore,
                                  String mediaSpaceId,
                                  String mediaContentId,
                                  StreamingUpdateListener updateListener) {
        this.contentStore = contentStore;
        this.mediaSpaceId = mediaSpaceId;
        this.mediaContentId = mediaContentId;
        this.updateListener = updateListener;
    }

    public String getAddStreamingItemResult() {
        return addStreamingItemResult;
    }

    public String getError() {
        return error;
    }

    @Override
    public void run() {
        try {
            addStreamingItem();
        } catch(Exception e) {
            log("Error encountered performing " + ADD_STREAMING_ITEM_TASK +
                " task: " + e.getMessage(), e);
            error = e.getMessage();
        }
    }

    /*
     * Add streaming item
     */
    private void addStreamingItem() throws ContentStoreException {
        String taskParams = mediaSpaceId + ":" + mediaContentId;

        String addStreamingItemResponse =
            contentStore.performTask(ADD_STREAMING_ITEM_TASK, taskParams);

        Map<String, String> responseMap =
            SerializationUtil.deserializeMap(addStreamingItemResponse);
        addStreamingItemResult = responseMap.get("results");
        if(addStreamingItemResult.contains("completed")) {
            updateListener.successfulStreamingAddition(mediaSpaceId,
                                                       mediaContentId);
        }
    }

    private void log(String logMsg, Exception e) {
        log.error(logMsg, e);
        log.error(ExceptionUtil.getStackTraceAsString(e));
    }
}
