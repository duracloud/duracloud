/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Handles the syncing of a single changed file using the given endpoint.
 *
 * @author: Bill Branan
 * Date: Mar 15, 2010
 */
public class SyncWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SyncWorker.class);    

    private static final int MAX_RETRIES = 5;

    private ChangedFile syncFile;
    private File watchDir;
    private SyncEndpoint syncEndpoint;
    private StatusManager statusManager;
    private boolean complete;
    private MonitoredFile monitoredFile;

    /**
     * Creates a SyncWorker to handle syncing a file
     *
     * @param file the file to sync
     * @param endpoint the endpoint to which the file should be synced
     */
    public SyncWorker(ChangedFile file, File watchDir, SyncEndpoint endpoint) {
        this.syncFile = file;
        this.watchDir = watchDir;
        this.syncEndpoint = endpoint;
        this.statusManager = StatusManager.getInstance();
        this.complete = false;
        this.monitoredFile = new MonitoredFile(syncFile.getFile());
    }

    public void run() {
        boolean success;
        try {
            success = syncEndpoint.syncFile(monitoredFile, watchDir);
        } catch(Exception e) {
            logger.error("Exception syncing file " +
                         syncFile.getFile().getAbsolutePath() +
                         " was " + e.getMessage(), e);
            success = false;
        }

        if(success) {
            statusManager.successfulCompletion();
        } else {
            retryOnFailure();
        }
        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    private void retryOnFailure() {
        int syncAttempts = syncFile.getSyncAttempts();
        String syncFilePath = syncFile.getFile().getAbsolutePath();
        if(syncAttempts < MAX_RETRIES) {
            logger.info("Adding " + syncFilePath + " back to the changed " +
                        "list, another attempt will be made to sync file.");
            syncFile.incrementSyncAttempts();
            ChangedList.getInstance().addChangedFile(syncFile);
            statusManager.stoppingWork();
        } else {
            statusManager.failedCompletion(syncFile.getFile());
            logger.error("Failed to sync file " + syncFilePath + " after " +
                syncAttempts + " attempts. No further attempts will be made.");
        }
    }

    public MonitoredFile getMonitoredFile() {
        return monitoredFile;
    }
}
