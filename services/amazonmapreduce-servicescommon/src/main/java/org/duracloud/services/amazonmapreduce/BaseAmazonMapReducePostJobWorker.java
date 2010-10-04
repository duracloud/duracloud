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

import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class is the base class of workers that are designed to be run
 * after the completion of a hadoop job.
 * The predecessor job that is provided to this class is polled for completion.
 * Once the predecessor has status 'complete' this class performs its work.
 *
 * @author Andrew Woods
 *         Date: Oct 1, 2010
 */
public abstract class BaseAmazonMapReducePostJobWorker implements AmazonMapReduceJobWorker {

    private final Logger log = LoggerFactory.getLogger(
        BaseAmazonMapReducePostJobWorker.class);

    private AmazonMapReduceJobWorker predecessor;
    private long sleepMillis;

    protected JobStatus status = JobStatus.WAITING;
    protected String error;

    public BaseAmazonMapReducePostJobWorker(AmazonMapReduceJobWorker predecessor) {
        this(predecessor, 120000);
    }

    public BaseAmazonMapReducePostJobWorker(AmazonMapReduceJobWorker predecessor,
                                            long sleepMillis) {
        this.predecessor = predecessor;
        this.sleepMillis = sleepMillis;
    }

    @Override
    public void run() {
        while (!status.isComplete()) {
            if (predecessor.getJobStatus().isComplete()) {
                status = JobStatus.POST_PROCESSING;
                work();
                status = JobStatus.COMPLETE;

            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("waiting for job-worker to complete: ");
                sb.append(predecessor.getClass());
                log.debug(sb.toString());

                sleep(sleepMillis);
            }
        }
        log.debug("post-processing complete: " + this.getClass());
    }

    /**
     * This abstract method performs the functional task of this post processor.
     */
    protected abstract void doWork();

    private void work() {
        log.debug("starting to perform post-processing: " + this.getClass());
        try {
            doWork();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public JobStatus getJobStatus() {
        return status;
    }

    @Override
    public Map<String, String> getJobDetailsMap() {
        return new HashMap<String, String>();
    }

    @Override
    public String getJobId() {
        throw new UnsupportedOperationException("getJobId() not supported");
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public void shutdown() {
        status = JobStatus.COMPLETE;
    }

    protected static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }
}
