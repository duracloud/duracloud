/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import org.duracloud.sync.mgmt.SyncSummary;
import org.duracloud.syncui.domain.SyncProcessState;
import org.duracloud.syncui.domain.SyncProcessStats;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public interface SyncProcess {

    /**
     * Starts the sync process. Invocations are ignored if the sync process is
     * already running.
     */
    public void start() throws SyncProcessException;

    /**
     * Resumes the sync process from the paused state. Invocations are ignored
     * if the sync process is already running.
     */
    public void resume() throws SyncProcessException;

    /**
     * Stops the sync process. Invocations are ignored if the sync process is
     * already stopped. All work state is deleted. On start, the sync will start
     * from scratch after this method has been invoked.
     */
    public void stop();

    /**
     * Pauses the sync process. Any any stored queue state information will be
     * preserved when the sync process is restarted. It can be invoked from any
     * state.
     */
    public void pause();

    /**
     * Returns an enum designating the runtime state of the sync process
     * 
     * @return
     */
    public SyncProcessState getProcessState();

    /**
     * Returns stats related the sync process
     * 
     * @return
     */
    public SyncProcessStats getProcessStats();


}
