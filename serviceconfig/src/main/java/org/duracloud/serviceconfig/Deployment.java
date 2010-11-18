/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.duracloud.serviceconfig.user.UserConfig;

import java.io.Serializable;
import java.util.List;

/**
 * This class holds the name, status, and configuration of an deployed service.
 *
 * @author Bill Branan
 *         Date: Nov 9, 2009
 */
public class Deployment implements Serializable {

    private static final long serialVersionUID = -5554753103296039412L;

    public enum Status {
        STOPPED, STARTED;
    }

    /** The identifier of this service deployment */
    private int id;

    /** The name of the host on which this service is deployed */
    private String hostname;

    /** The status of this deployed service */
    private Status status;

    /** The system configuration settings for this deployed service */
    private List<SystemConfig> systemConfigs;

    /** The user configuration settings for this deployed service */
    private List<UserConfig> userConfigs;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }    

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<SystemConfig> getSystemConfigs() {
        return systemConfigs;
    }

    public void setSystemConfigs(List<SystemConfig> systemConfigs) {
        this.systemConfigs = systemConfigs;
    }

    public List<UserConfig> getUserConfigs() {
        return userConfigs;
    }

    public void setUserConfigs(List<UserConfig> userConfigs) {
        this.userConfigs = userConfigs;
    }

}
