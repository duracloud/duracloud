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
public class DeployMessage {

    private String serviceId;

    private String serviceHost;

    private int deploymentId;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    public int getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(int deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DeployMessage[");
        sb.append("serviceId:'" + serviceId + "'");
        sb.append("|serviceHost:'" + serviceHost + "'");
        sb.append("|deploymentId:'" + deploymentId + "'");
        sb.append("]\n");
        return sb.toString();
    }

}
