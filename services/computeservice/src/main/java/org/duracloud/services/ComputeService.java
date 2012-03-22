/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services;

import java.util.Map;

public interface ComputeService {

    public enum ServiceStatus {
        UNKNOWN,
        INSTALLED,
        STARTING,
        STARTED,
        WAITING,
        PROCESSING,
        POSTPROCESSING,
        FINALIZING,
        STOPPING,
        STOPPED,
        /* Means the service successfully ran to completion; does not
         * necessarily mean that all results of the service were also
         * successful. Check the value associated with FAILURE_COUNT_KEY
         * below to determine if all results were successful.
         */
        SUCCESS,
        /* Means the service failed due to some kind of unrecoverable
         * exception, OOM, failed hadoop startup, etc.
         */
        FAILED; 

        public boolean isComplete() {
            return this.equals(STARTED) || this.equals(SUCCESS) || this.equals(
                FAILED);
        }
    }

    public static final String SYSTEM_PREFIX = "System ";

    public static final String STATUS_KEY = "Service Status";
    public static final String ERROR_KEY = "Error Message";
    public static final String STARTTIME_KEY = "Start Time";
    public static final String STOPTIME_KEY = "Stop Time";
    public static final String REPORT_KEY = "Report";
    public static final String ERROR_REPORT_KEY = "Error Report";
    public static final String PASS_COUNT_KEY = "Passed Item Count";
    public static final String FAILURE_COUNT_KEY = "Failed Item Count";
    public static final String ITEMS_PROCESS_COUNT = "Total Item Count";

    public static final String SVC_LAUNCHING_USER = "Service Launched By";

    public static final char DELIM = '\t';



    public void start() throws Exception;

    public void stop() throws Exception;

    public Map<String, String> getServiceProps();

    public String describe() throws Exception;

    public String getServiceId();

    public ServiceStatus getServiceStatus() throws Exception;

    public String getServiceWorkDir();

    public void setServiceWorkDir(String serviceWorkDir);

}
