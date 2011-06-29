/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceapi.aop;

/**
 * @author Andrew Woods
 *         Date: June 20, 2011
 */
public class ServiceMessage {

    private int serviceId = -1;
    private int deploymentId = -1;

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(int deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceMessage[");
        sb.append("serviceId:'" + serviceId + "'");
        sb.append("|deploymentId:'" + deploymentId + "'");
        sb.append("]");
        return sb.toString();
    }

}
