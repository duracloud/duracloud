/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

public class ServiceCommand {

    public Integer serviceInfoId;

    public Integer deploymentId;
    
    public void setServiceInfoId(Integer serviceInfoId) {
        this.serviceInfoId = serviceInfoId;
    }

    public Integer getServiceInfoId() {
       return this.serviceInfoId;
    }

    @Override
    public String toString() {
        return "ServiceCommand [deploymentId=" + deploymentId
                + ", serviceInfoId=" + serviceInfoId + "]";
    }

    
    public Integer getDeploymentId() {
        return deploymentId;
    }

    
    public void setDeploymentId(Integer deploymentId) {
        this.deploymentId = deploymentId;
    }

}
