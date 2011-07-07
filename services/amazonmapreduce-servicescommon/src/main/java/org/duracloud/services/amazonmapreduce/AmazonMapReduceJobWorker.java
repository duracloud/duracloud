/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.duracloud.services.ComputeService;

import java.util.Map;

import static org.duracloud.services.ComputeService.ServiceStatus;

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
        STARTING("Starting Job...", ServiceStatus.PROCESSING),
        RUNNING("Running Job...", ServiceStatus.PROCESSING),
        COMPLETE("Job Complete", ServiceStatus.FINALIZING),
        WAITING("Waiting to post process...", ServiceStatus.WAITING),
        POST_PROCESSING("Post Job Processing...", ServiceStatus.POSTPROCESSING),
        UNKNOWN("Job status unknown", null);

        private String description;
        private ComputeService.ServiceStatus serviceStatus;

        JobStatus(String description, ComputeService.ServiceStatus serviceStatus) {
            this.description = description;
            this.serviceStatus =serviceStatus;
        }

        public String getDescription() {
            return description;
        }

        public boolean isComplete() {
            return this.equals(COMPLETE);
        }

        public ComputeService.ServiceStatus toServiceStatus() {
            return this.serviceStatus;
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
