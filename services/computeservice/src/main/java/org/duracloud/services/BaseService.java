/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Oct 21, 2009
 */
public abstract class BaseService implements ComputeService {

    private String serviceId;
    private String serviceWorkDir;
    private String startTime;
    private String endTime;
    private String error;

    private ServiceStatus serviceStatus = ServiceStatus.INSTALLED;

    public void start() throws Exception {
        this.startTime = getCurrentTime();
        this.endTime = null;
        this.error = null;
        setServiceStatus(ServiceStatus.STARTED);
    }

    public void stop() throws Exception {
        setServiceStatus(ServiceStatus.STOPPED);
    }

    protected void doneWorking() {
        this.endTime = getCurrentTime();

        if (null != error) {
            setServiceStatus(ComputeService.ServiceStatus.FAILED);
        } else {
            setServiceStatus(ComputeService.ServiceStatus.SUCCESS);
        }
    }

    private String getCurrentTime() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return format.format(System.currentTimeMillis());
    }

    public Map<String, String> getServiceProps() {
        Map<String, String> props = new HashMap<String, String>();
        props.put(SYSTEM_PREFIX + "serviceId", getServiceId());
        props.put(ComputeService.STATUS_KEY, getServiceStatus().name());

        if (startTime != null) {
            props.put(ComputeService.STARTTIME_KEY, startTime);
        }

        if (endTime != null) {
            props.put(ComputeService.STOPTIME_KEY, endTime);
        }

        if (error != null) {
            props.put(ComputeService.ERROR_KEY, error);
        }
        
        return props;
    }

    public String describe() throws Exception {
        StringBuilder serviceDescription = new StringBuilder();
        serviceDescription.append("; Service ID: ");
        serviceDescription.append(serviceId);
        serviceDescription.append("; Service class: ");
        serviceDescription.append(getClass().getName());
        serviceDescription.append("; Service status: ");
        serviceDescription.append(serviceStatus);
        return serviceDescription.toString();
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceWorkDir() {
        return serviceWorkDir;
    }

    public void setServiceWorkDir(String serviceWorkDir) {
        this.serviceWorkDir = serviceWorkDir;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
