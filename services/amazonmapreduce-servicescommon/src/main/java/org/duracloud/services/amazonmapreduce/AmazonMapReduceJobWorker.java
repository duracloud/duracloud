/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

/**
 * This interface defines the contract for workers that manage jobs in the
 * hadoop framework.
 * 
 * @author Andrew Woods
 *         Date: Sep 29, 2010
 */
public interface AmazonMapReduceJobWorker extends Runnable {
    
    public boolean isComplete();

    public String getJobId();

    public String getError();

    public void shutdown();
}
