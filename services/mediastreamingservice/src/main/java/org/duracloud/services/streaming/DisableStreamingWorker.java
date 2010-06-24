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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: Jun 3, 2010
 */
public class DisableStreamingWorker implements Runnable {

    private static final String DISABLE_STREAMING_TASK = "disable-streaming";    

    private final Logger log = LoggerFactory.getLogger(DisableStreamingWorker.class);

    private ContentStore contentStore;
    private String mediaSourceSpaceId;

    public DisableStreamingWorker(ContentStore contentStore,
                                  String mediaSourceSpaceId) {
        this.contentStore = contentStore;
        this.mediaSourceSpaceId = mediaSourceSpaceId;
    }

    @Override
    public void run() {
        try {
            // Disable distribution
            String results =
                contentStore.performTask(DISABLE_STREAMING_TASK,
                                         mediaSourceSpaceId);
            log("Results of performing " + DISABLE_STREAMING_TASK + " task: " +
                results);
        } catch(Exception e) {
            log("Error encountered performing " + DISABLE_STREAMING_TASK +
                " task: " + e.getMessage(), e);
        }
    }

    private void log(String logMsg) {
        log.info(logMsg);
    }

    private void log(String logMsg, Exception e) {
        log.error(logMsg, e);
        log.error(ExceptionUtil.getStackTraceAsString(e));
    }
}
