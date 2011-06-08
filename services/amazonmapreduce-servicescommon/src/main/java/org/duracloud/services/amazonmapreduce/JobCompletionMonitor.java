/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class monitors an AmazonMapReduceJobWorker until the underlying hadoop
 * framework reports that the job is complete.
 * Then this class shutsdown the AmazonMapReduceJobWorker.
 *
 * @author Andrew Woods
 *         Date: Sep 30, 2010
 */
public abstract class JobCompletionMonitor implements Runnable {

    private final Logger log = LoggerFactory.getLogger(JobCompletionMonitor.class);

    private AmazonMapReduceJobWorker worker;
    private long sleepMillis;

    public JobCompletionMonitor(AmazonMapReduceJobWorker worker) {
        this(worker, 30000);
    }

    public JobCompletionMonitor(AmazonMapReduceJobWorker worker, long sleepMillis) {
        this.worker = worker;
        this.sleepMillis = sleepMillis;
    }

    /**
     * This method spins until the job started by the AmazonMapReduceJobWorker
     * is complete, at which point this method shutsdown the worker.
     */
    @Override
    public void run() {
        AmazonMapReduceJobWorker.JobStatus status;
        while (!(status = worker.getJobStatus()).isComplete()) {

            preCompletionAction(worker);

            logStatus(status);
            sleep(sleepMillis);
        }
        postCompletionAction();
        log.info("Monitoring of " + worker.getClass() + " done.");
    }

    protected abstract void preCompletionAction(AmazonMapReduceJobWorker worker);

    protected abstract void postCompletionAction();

    private void logStatus(AmazonMapReduceJobWorker.JobStatus status) {
        StringBuilder sb = new StringBuilder("Monitoring status of: ");
        sb.append(worker.getClass());
        sb.append(" [");
        sb.append(status.getDescription());
        sb.append("]");
        log.debug(sb.toString());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
