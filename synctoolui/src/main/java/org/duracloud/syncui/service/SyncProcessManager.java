/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.io.File;
import java.util.List;

import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.mgmt.SyncSummary;

/**
 * The SyncProcessManager interface delineates the set of functions available
 * for controlling and retrieving state information about the current runtime
 * state of the sync process 
 * @author Daniel Bernstein
 *
 */
public interface SyncProcessManager extends SyncProcess{
      
    /**
     * 
     * @param syncStateChangeListener
     */
    public void addSyncStateChangeListener(SyncStateChangeListener syncStateChangeListener);

    /**
     * 
     * @param syncStateChangeListener
     */
    public void removeSyncStateChangeListener(SyncStateChangeListener syncStateChangeListener);

    /**
     * Returns a list of actively transfering (uploading) files
     * @return
     */
    public List<MonitoredFile> getMonitoredFiles();

    /**
     * 
     * @return
     */
    public List<File> getQueuedFiles();

    /**
     * 
     * @return
     */
    public SyncProcessError getError();

    /**
     * 
     */
    void clearError();

    List<SyncSummary> getFailures();

    List<SyncSummary> getRecentlyCompleted();

}
