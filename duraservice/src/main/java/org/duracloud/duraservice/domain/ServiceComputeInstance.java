/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.domain;

import org.duracloud.servicesadminclient.ServicesAdminClient;

/**
 * @author: Bill Branan
 * Date: Nov 11, 2009
 */
public class ServiceComputeInstance {

    private String hostName;
    private String displayName;

    // A locked service instance indicates that no additional services
    // should be deployed to this instance.
    private boolean locked;

    private ServicesAdminClient servicesAdmin;

    public ServiceComputeInstance(String hostName,
                           String displayName,
                           ServicesAdminClient servicesAdmin) {
        this.hostName = hostName;
        this.displayName = displayName;
        this.servicesAdmin = servicesAdmin;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public ServicesAdminClient getServicesAdmin() {
        return servicesAdmin;
    }

    public void setServicesAdmin(ServicesAdminClient servicesAdmin) {
        this.servicesAdmin = servicesAdmin;
    }    
}
