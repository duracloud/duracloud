/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

import org.duracloud.storage.domain.DatabaseConfig;

/**
 * @author: Bill Branan
 * Date: Jan 15, 2010
 */
public class AdminInit {

    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String amaUrl;
    private String duraBossContext;
    private boolean millDbEnabled;


    public String getDuraStoreHost() {
        return duraStoreHost;
    }

    public void setDuraStoreHost(String duraStoreHost) {
        this.duraStoreHost = duraStoreHost;
    }

    public String getDuraStorePort() {
        return duraStorePort;
    }

    public void setDuraStorePort(String duraStorePort) {
        this.duraStorePort = duraStorePort;
    }

    public String getDuraStoreContext() {
        return duraStoreContext;
    }

    public void setDuraStoreContext(String duraStoreContext) {
        this.duraStoreContext = duraStoreContext;
    }

    public String getAmaUrl() {
        return amaUrl;
    }

    public void setAmaUrl(String amaUrl) {
        this.amaUrl = amaUrl;
    }

    public void setDuraBossContext(String durabossContext) {
        this.duraBossContext = durabossContext;
    }

    public String getDuraBossContext() {
        return duraBossContext;
    }

    public void setMillDbEnabled(boolean millDbEnabled) {
        this.millDbEnabled = millDbEnabled;
    }
    
    public boolean isMillDbEnabled() {
        return millDbEnabled;
    }
}