/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services;

import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: Oct 21, 2009
 */
public abstract class BaseService implements ComputeService {

    private final Logger log = LoggerFactory.getLogger(BaseService.class);
    private String serviceId;
    private String serviceWorkDir;
    private String reportId;
    private String errorReportId;
    private String startTime;
    private String endTime;
    private String error;
    private String svcLaunchingUser;

    private ServiceStatus serviceStatus = ServiceStatus.INSTALLED;

    public void start() throws Exception {
        this.reportId = null;
        this.startTime = getCurrentTime();
        this.endTime = null;
        this.error = null;
        this.errorReportId = null;
    }

    public void stop() throws Exception {
        setServiceStatus(ServiceStatus.STOPPED);
    }

    public void doneWorking() {
        this.endTime = getCurrentTime();

        if (null != error) {
            setServiceStatus(ComputeService.ServiceStatus.FAILED);
        } else {
            setServiceStatus(ComputeService.ServiceStatus.SUCCESS);
        }
    }

    private String getCurrentTime() {
        return DateUtil.now();
    }

    public Map<String, String> getServiceProps() {
        Map<String, String> props = new HashMap<String, String>();
        props.put(SYSTEM_PREFIX + "serviceId", getServiceId());
        props.put(STATUS_KEY, getServiceStatus().name());

        if (startTime != null) {
            props.put(STARTTIME_KEY, startTime);
        }

        if (endTime != null) {
            props.put(STOPTIME_KEY, endTime);
        }

        if (error != null) {
            props.put(ERROR_KEY, error);
        }

        if (reportId != null && serviceStatus.isComplete()) {
            props.put(REPORT_KEY, reportId);
        }

        if (svcLaunchingUser != null) {
            props.put(SVC_LAUNCHING_USER, getSvcLaunchingUser());
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

    public void setReportId(String spaceId, String contentId) {
        setReportId(spaceId, contentId, null);
    }

    public void setReportId(String spaceId, String contentId, String storeId) {
        String id = spaceId + "/" + contentId;
        if(storeId != null){
            id+="?storeId="+storeId;
        }
        this.reportId = id;
    }

    public void setErrorReportId(String spaceId, String errorReportId) {
        setErrorReportId(spaceId, errorReportId, null);
    }

    public void setErrorReportId(String spaceId, String contentId, String storeId) {
        String id = spaceId + "/" + contentId;
        if(storeId != null){
            id+="?storeId="+storeId;
        }
        this.errorReportId = id;
    } 

    public String getSvcLaunchingUser() {
        return svcLaunchingUser;
    }

    public void setSvcLaunchingUser(String svcLaunchingUser) {
        this.svcLaunchingUser = svcLaunchingUser;
    }
    
    protected String getErrorReportId(){
        return this.errorReportId;
    }

    protected void addErrorReport(Map<String, String> properties) {
        if(properties.containsKey(FAILURE_COUNT_KEY)){
            String failureCount = properties.get(FAILURE_COUNT_KEY);
            if(failureCount != null){
                try{
                    if(Long.parseLong(failureCount) > 0){
                        properties.put(ERROR_REPORT_KEY, getErrorReportId());
                    }
                }catch(NumberFormatException ex){
                    log.error("failure count is not parseable: " + failureCount, ex);
                }
            }
        }
    }
}
