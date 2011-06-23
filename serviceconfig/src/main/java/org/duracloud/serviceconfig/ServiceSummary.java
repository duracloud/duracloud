/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import java.util.Map;

/**
 * This bean contains the complete details of a completed service:
 * - configuration info
 * - service output properties
 *
 * @author Andrew Woods
 *         Date: 6/22/11
 */
public class ServiceSummary {

    private ServiceInfo serviceInfo;
    private Map<String, String> serviceProperties;

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public Map<String, String> getServiceProperties() {
        return serviceProperties;
    }

    public void setServiceProperties(Map<String, String> serviceProperties) {
        this.serviceProperties = serviceProperties;
    }
}
