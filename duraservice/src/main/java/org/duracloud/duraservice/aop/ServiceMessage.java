/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.aop;

public class ServiceMessage {

    private String serviceId;

    private String deploymentId;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceMessage[");
        sb.append("serviceId:'" + serviceId + "'");
        sb.append("|deploymentId:'" + deploymentId + "'");
        sb.append("]\n");
        return sb.toString();
    }

}
