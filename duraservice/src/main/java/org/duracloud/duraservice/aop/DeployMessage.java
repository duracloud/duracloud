/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.aop;

public class DeployMessage {

    private String serviceId;

    private String serviceHost;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DeployMessage[");
        sb.append("serviceId:'" + serviceId + "'");
        sb.append("|serviceHost:'" + serviceHost + "'");
        sb.append("]\n");
        return sb.toString();
    }

}
