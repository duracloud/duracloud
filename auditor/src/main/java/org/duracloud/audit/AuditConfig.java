/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

/**
 * Defines a set of parameters necessary for initializing audit log access.
 * 
 * @author Daniel Bernstein
 * 
 */
public class AuditConfig {
    private String spaceId;
    private String storeId;
    private String duracloudHost;
    private int duracloudPort;
    private String duracloudUsername;
    private String duracloudPassword;

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getDuracloudHost() {
        return duracloudHost;
    }

    public void setDuracloudHost(String duracloudHost) {
        this.duracloudHost = duracloudHost;
    }

    public int getDuracloudPort() {
        return duracloudPort;
    }

    public void setDuracloudPort(int duracloudPort) {
        this.duracloudPort = duracloudPort;
    }

    public String getDuracloudUsername() {
        return duracloudUsername;
    }

    public void setDuracloudUsername(String duracloudUsername) {
        this.duracloudUsername = duracloudUsername;
    }

    public String getDuracloudPassword() {
        return duracloudPassword;
    }

    public void setDuracloudPassword(String duracloudPassword) {
        this.duracloudPassword = duracloudPassword;
    }
}
