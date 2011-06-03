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
        INSTALLED,
        STARTING,
        STARTED,
        WAITING,
        PROCESSING,
        POSTPROCESSING,
        STOPPING,
        STOPPED,
        SUCCESS,
        FAILED;
    }

    public static final String SYSTEM_PREFIX = "System ";

    public static final String STATUS_KEY = "Service Status";
    public static final String ERROR_KEY = "Error Message";
    public static final String STARTTIME_KEY = "Start Time";
    public static final String STOPTIME_KEY = "Stop Time";

    public void start() throws Exception;

    public void stop() throws Exception;

    public Map<String, String> getServiceProps();

    public String describe() throws Exception;

    public String getServiceId();

    public ServiceStatus getServiceStatus() throws Exception;

    public String getServiceWorkDir();

    public void setServiceWorkDir(String serviceWorkDir);
}
