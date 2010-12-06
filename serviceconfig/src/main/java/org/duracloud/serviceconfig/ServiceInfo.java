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

import org.duracloud.serviceconfig.user.UserConfigModeSet;



/**
 * This class is the container for all of the settings and options available for
 *  a service.
 *
 * @author Andrew Woods
 *         Date: Nov 6, 2009
 */
public class ServiceInfo implements Serializable, Cloneable {

    private static final long serialVersionUID = -7958760599324208594L;

    /** Unique identifier for this service */
    private int id;

    /** DuraStore ID for retrieving the service binaries */
    private String contentId;

    /** User-friendly Service name */
    private String displayName;

    /** Release version number of the service software */
    private String serviceVersion;

    /** User configuration version, checked at deployment time */
    private String userConfigVersion;

    /** Text description of service capabilities */
    private String description;

    /**
     * Specifies number of deployments of this service that are allowed,
     * a value of -1 indicates that there is no limit
     */
    private int maxDeploymentsAllowed = -1;

    /** The default system configuration options */
    private List<SystemConfig> systemConfigs;

    /** The default user configuration options */
    private List<UserConfigModeSet> userConfigModeSets;

    /** Includes information necessary to deploy a new service of this type */
    private List<DeploymentOption> deploymentOptions;

    /** Includes information about existing deployments of this service */
    private List<Deployment> deployments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public int getMaxDeploymentsAllowed() {
        return maxDeploymentsAllowed;
    }

    public void setMaxDeploymentsAllowed(int maxDeploymentsAllowed) {
        this.maxDeploymentsAllowed = maxDeploymentsAllowed;
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

    public String getUserConfigVersion() {
        return userConfigVersion;
    }

    public void setUserConfigVersion(String userConfigVersion) {
        this.userConfigVersion = userConfigVersion;
    }

    public List<DeploymentOption> getDeploymentOptions() {
        return deploymentOptions;
    }

    public void setDeploymentOptions(List<DeploymentOption> deploymentOptions) {
        this.deploymentOptions = deploymentOptions;
    }

    public List<Deployment> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<Deployment> deployments) {
        this.deployments = deployments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    
    public int getDeploymentCount(){
        return this.deployments == null ? 0 : this.deployments.size();
    }
    
    public boolean isNewDeploymentAllowed(){
        return getDeploymentCount() < getMaxDeploymentsAllowed();
    }

    public ServiceInfo clone() throws CloneNotSupportedException {
        ServiceInfo clone = (ServiceInfo) super.clone();

        // immutable fields. int
        clone.setId(this.getId());
        clone.setMaxDeploymentsAllowed(this.getMaxDeploymentsAllowed());

        // immutable fields. String
        clone.setContentId(this.getContentId());
        clone.setDescription(this.getDescription());
        clone.setDisplayName(this.getDisplayName());
        clone.setServiceVersion(this.getServiceVersion());
        clone.setUserConfigVersion(this.getUserConfigVersion());

        // mutable fields
        clone.setDeploymentOptions(cloneDeploymentOptions());
        clone.setDeployments(cloneDeployments());
        clone.setSystemConfigs(cloneSystemConfigs());
        clone.setUserConfigModeSets(cloneUserConfigModeSets());

        return clone;
    }

    private List<DeploymentOption> cloneDeploymentOptions()
        throws CloneNotSupportedException {
        List<DeploymentOption> clone = null;
        if (null != this.getDeploymentOptions()) {
            clone = new ArrayList<DeploymentOption>();
            for (DeploymentOption source : this.getDeploymentOptions()) {
                clone.add(source.clone());
            }
        }
        return clone;
    }

    private List<Deployment> cloneDeployments()
        throws CloneNotSupportedException {
        List<Deployment> clone = null;
        if (null != this.getDeployments()) {
            clone = new ArrayList<Deployment>();
            for (Deployment source : this.getDeployments()) {
                clone.add(source.clone());
            }
        }
        return clone;
    }

    private List<SystemConfig> cloneSystemConfigs() throws CloneNotSupportedException {
        List<SystemConfig> clone = null;
        if (null != this.systemConfigs) {
            clone = new ArrayList<SystemConfig>();
            for (SystemConfig source : this.systemConfigs) {
                clone.add(source.clone());
            }
        }
        return clone;
    }

    private List<UserConfigModeSet> cloneUserConfigModeSets() throws CloneNotSupportedException {
        List<UserConfigModeSet> clone = null;
        if (null != this.userConfigModeSets) {
            clone = new ArrayList<UserConfigModeSet>();
            for (UserConfigModeSet source : this.userConfigModeSets) {
                clone.add(source.clone());
            }
        }
        return clone;
    }

}
