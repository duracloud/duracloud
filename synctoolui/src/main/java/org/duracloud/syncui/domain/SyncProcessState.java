/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public enum SyncProcessState {
    STOPPED, // the sync process is not running
    STARTING, 
    RUNNING, // the sync process is running - ie it is monitoring
                       // directories and trying to upload
    STOPPING, 
    ERROR, // the system is not running.
    PAUSING,
    PAUSED, // the sync process is not running; upon resuming it will begin
           // processing the queue from where it left off.
    RESUMING
}