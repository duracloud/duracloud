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
 * This bean contains the complete details of a service.
 *
 * @author Andrew Woods
 *         Date: 6/22/11
 */
public class ServiceSummary {

    /** Unique identifier for this service */
    private int id;

    /** Unique identifier for this service deployment */
    private int deploymentId;

    /** User-friendly Service name */
    private String name;

    /** Release version number of the service software */
    private String version;

    /** User configuration for this service deployment */
    private Map<String, String> configs;

    /** Properties of this service deployment */
    private Map<String, String> properties;

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(int deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
