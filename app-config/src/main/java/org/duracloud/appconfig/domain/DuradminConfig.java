/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.appconfig.xml.DuradminInitDocumentBinding;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the configuration elements for duradmin.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class DuradminConfig extends BaseConfig implements AppConfig {
    private final Logger log = LoggerFactory.getLogger(DuradminConfig.class);

    public static final String QUALIFIER = "duradmin";
    public static final String duraStoreHostKey = "durastore-host";
    public static final String duraStorePortKey = "durastore-port";
    public static final String duraStoreContextKey = "durastore-context";
    public static final String duraBossContextKey = "duraboss-context";
    public static final String millDbEnabledKey = "milldb.enabled";
    public static final String amaUrlKey = "ama-url";

    private String durastoreHost;
    private String durastorePort;
    private String durastoreContext;
    private String durabossContext;
    private boolean millDbEnabled = false;
    private String amaUrl;

    public String asXml() {
        return DuradminInitDocumentBinding.createDocumentFrom(this);
    }

    public String getInitResource() {
        return INIT_RESOURCE;
    }

    protected String getQualifier() {
        return QUALIFIER;
    }

    protected void loadProperty(String key, String value) {
        key = key.toLowerCase();
        if (key.equalsIgnoreCase(duraStoreHostKey)) {
            this.durastoreHost = value;

        } else if (key.equalsIgnoreCase(duraStorePortKey)) {
            this.durastorePort = value;

        } else if (key.equalsIgnoreCase(duraStoreContextKey)) {
            this.durastoreContext = value;

        } else if (key.equalsIgnoreCase(duraBossContextKey)) {
            this.durabossContext = value;
            this.durabossContext = value;
        } else if (key.equalsIgnoreCase(amaUrlKey)) {
            this.amaUrl = value;
        } else if (key.equalsIgnoreCase(millDbEnabledKey)) {
            this.millDbEnabled = Boolean.valueOf(value);
        }  else {
            if(!subclassLoadProperty(key, value)) {
                String msg = "unknown key: " + key + " (" + value + ")";
                log.error(msg);
                throw new DuraCloudRuntimeException(msg);
            }
        }
    }

    protected boolean subclassLoadProperty(String key, String value) {
        return false;
    }

    public String getDurastoreHost() {
        return durastoreHost;
    }

    public void setDurastoreHost(String durastoreHost) {
        this.durastoreHost = durastoreHost;
    }

    public String getDurastorePort() {
        return durastorePort;
    }

    public void setDurastorePort(String durastorePort) {
        this.durastorePort = durastorePort;
    }

    public String getDurastoreContext() {
        return durastoreContext;
    }

    public void setDurastoreContext(String durastoreContext) {
        this.durastoreContext = durastoreContext;
    }

    public String getAmaUrl() {
        return amaUrl;
    }

    public void setAmaUrl(String amaUrl) {
        this.amaUrl = amaUrl;
    }

    public String getDurabossContext() {
        return durabossContext;
    }

    public void setDurabossContext(String durabossContext) {
        this.durabossContext = durabossContext;
    }
    
    public Boolean isMillDbEnabled() {
        return millDbEnabled;
    }
    
    public void setMillDbEnabled(boolean millDbEnabled) {
        this.millDbEnabled = millDbEnabled;
    }

    
}
