/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;

/**
 * This class holds the name, status, and configuration of an deployed service.
 *
 * @author Bill Branan
 *         Date: Nov 9, 2009
 */
public class Deployment implements Serializable, Cloneable {

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
    private List<UserConfigModeSet> userConfigModeSets;

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

	public List<UserConfigModeSet> getUserConfigModeSets() {
		return userConfigModeSets;
	}

	public void setUserConfigModeSets(List<UserConfigModeSet> userConfigModeSets) {
		this.userConfigModeSets = userConfigModeSets;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deployment)) {
            return false;
        }

        Deployment that = (Deployment) o;

        if (id != that.id) {
            return false;
        }
        if (hostname != null ? !hostname.equals(that.hostname) :
            that.hostname != null) {
            return false;
        }
        if (status != that.status) {
            return false;
        }
        if (systemConfigs != null ? !systemConfigs.equals(that.systemConfigs) :
            that.systemConfigs != null) {
            return false;
        }
        if (userConfigModeSets !=
            null ? !userConfigModeSets.equals(that.userConfigModeSets) :
            that.userConfigModeSets != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result +
            (systemConfigs != null ? systemConfigs.hashCode() : 0);
        result = 31 * result +
            (userConfigModeSets != null ? userConfigModeSets.hashCode() : 0);
        return result;
    }

    public Deployment clone() throws CloneNotSupportedException {
        Deployment clone = (Deployment)super.clone();

        clone.setHostname(this.hostname);
        clone.setId(this.id);
        clone.setStatus(this.status);

        clone.setSystemConfigs(cloneSystemConfigs());
        clone.setUserConfigModeSets(cloneUserConfigModeSets());

        return clone;
    }

    private List<SystemConfig> cloneSystemConfigs()
        throws CloneNotSupportedException {
        List<SystemConfig> clones = null;
        if (null != this.systemConfigs) {
            clones = new ArrayList<SystemConfig>();
            for (SystemConfig systemConfig : this.systemConfigs) {
                clones.add(systemConfig.clone());
            }
        }
        return clones;
    }

    private List<UserConfigModeSet> cloneUserConfigModeSets()
        throws CloneNotSupportedException {
        List<UserConfigModeSet> clones = null;
        if (null != this.userConfigModeSets) {
            clones = new ArrayList<UserConfigModeSet>();
            for (UserConfigModeSet userConfigModeSet : this.userConfigModeSets) {
                clones.add(userConfigModeSet.clone());
            }
        }
        return clones;
    }

}
