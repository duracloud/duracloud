/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image.status;

/**
 * This interface defines the contract for a listener of work status.
 *
 * @author Andrew Woods
 *         Date: 5/30/11
 */
public interface StatusListener {

    /**
     * This method is called when a worker's job is complete.
     */
    public void doneWorking();

    /**
     * This method sets the error of a job.
     *
     * @param error message
     */
    public void setError(String error);

}
