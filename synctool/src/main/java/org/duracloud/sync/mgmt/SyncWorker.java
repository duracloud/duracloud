/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import java.io.File;
import java.util.Date;

import org.duracloud.sync.endpoint.SyncResultType;
import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the syncing of a single changed file using the given endpoint.
 * 
 * @author: Bill Branan Date: Mar 15, 2010
 */
public class SyncWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SyncWorker.class);

    private static final int MAX_RETRIES = 5;

    private ChangedFile syncFile;
    private File watchDir;
    private SyncEndpoint syncEndpoint;
    private StatusManager statusManager;
    private ChangedList changedList;
    private boolean complete;
    private MonitoredFile monitoredFile;
    private Date start, stop;

    /**
     * Creates a SyncWorker to handle syncing a file
     * 
     * @param file
     *            the file to sync
     * @param watchDir
     *            dir under watch where file exists or null if file does not
     *            reside in a watched directory
     * @param endpoint
     *            the endpoint to which the file should be synced
     * @param changedList
     *            the changeList
     * @param statusManager
     *            the statusManager
     */
    public SyncWorker(ChangedFile file,
                      File watchDir,
                      SyncEndpoint endpoint,
                      ChangedList changedList,
                      StatusManager status) {
        this.syncFile = file;
        this.watchDir = watchDir;
        this.syncEndpoint = endpoint;
        this.changedList = changedList;
        this.statusManager = status;
        this.complete = false;
        this.monitoredFile = new MonitoredFile(syncFile.getFile());
    }

    public void run() {
        SyncResultType result;
        start = new Date();
        File file = syncFile.getFile();
        String filePath = (null != file ? file.getAbsolutePath() : "null");

        try {
            result = syncEndpoint.syncFileAndReturnDetailedResult(monitoredFile, watchDir);
            stop = new Date();
        } catch (Exception e) {
            logger.error("Exception syncing file "
                             + filePath + " was "
                             + e.getMessage(),
                         e);
            result = SyncResultType.FAILED;
        }
        
        
        boolean willRetry = false;
        try{
            if (result != SyncResultType.FAILED) {
                SyncSummary summary =
                    new SyncSummary(file,
                                    start,
                                    stop,
                                    result,
                                    "");
                
                statusManager.successfulCompletion(summary);
            } else {
                willRetry = retryOnFailure();
            }
        }catch(Throwable e){
            logger.error("Unexpected error: " + e.getMessage()
                         + " - sync result = "
                         + result
                         + "; file="
                         + filePath, e);
            
        }
        //remove from the list.
        if(!willRetry){
            this.changedList.remove(this.syncFile);
        }

        complete = true;
        
    }

    public boolean isComplete() {
        return complete;
    }

    private boolean retryOnFailure() {
        int syncAttempts = syncFile.getSyncAttempts();
        String syncFilePath = syncFile.getFile().getAbsolutePath();
        if (syncAttempts < MAX_RETRIES) {
            logger.info("Adding "
                + syncFilePath + " back to the changed "
                + "list, another attempt will be made to sync file.");
            syncFile.incrementSyncAttempts();
            this.changedList.unreserve(syncFile);
            statusManager.stoppingWork();
            return true;
        } else {

            SyncSummary summary =
                new SyncSummary(syncFile.getFile(),
                                start,
                                stop,
                                SyncResultType.FAILED,
                                "Failed after " + syncAttempts + " attempts.");

            statusManager.failedCompletion(summary);
            logger.error("Failed to sync file "
                + syncFilePath + " after " + syncAttempts
                + " attempts. No further attempts will be made.");
            return false;
        }
    }

    public MonitoredFile getMonitoredFile() {
        return monitoredFile;
    }
}
