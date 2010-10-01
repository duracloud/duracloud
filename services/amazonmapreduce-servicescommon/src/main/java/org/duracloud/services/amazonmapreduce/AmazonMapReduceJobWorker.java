/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import java.util.Map;

/**
 * This interface defines the contract for workers that manage jobs in the
 * hadoop framework.
 *
 * @author Andrew Woods
 *         Date: Sep 29, 2010
 */
public interface AmazonMapReduceJobWorker extends Runnable {

    /**
     * This enum holds the states of a hadoop job.
     */
    public enum JobStatus {
        STARTING("Starting Job..."), RUNNING("Running Job..."), COMPLETE(
            "Job Complete"), WAITING("Waiting to post process..."),
        POST_PROCESSING("Post Job Processing..."),
        UNKNOWN("Job status unknown");

        private String description;

        JobStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isComplete() {
            return this.equals(COMPLETE);
        }
    }

    /**
     * This method returns the current job state.
     *
     * @return jobStatus
     */
    public JobStatus getJobStatus();

    /**
     * This method queries the hadoop framework and returns a listing of
     * details related to the currently running job.
     * Details include: jobId, job start time, job state, job end time
     *
     * @return map
     */
    public Map<String, String> getJobDetailsMap();

    /**
     * This method returns the jobId of the current job.
     *
     * @return jobId
     */
    public String getJobId();

    /**
     * This method returns any errors associated with the current running job.
     *
     * @return error text
     */
    public String getError();

    /**
     * This methods terminates the processing of this job.
     */
    public void shutdown();
}
