/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public enum RunMode {
    /*
     * signifies continuous execution (ie listen for files system changes
     * after queuing all matching files.
     */
    CONTINUOUS,  
    /*
     * just load matching files - do not listen for subsequent updates/additions/deletions.
     */
    SINGLE_PASS
}
