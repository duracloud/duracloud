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

    private static final String INIT_RESOURCE = "/init";

    public static final String QUALIFIER = "duradmin";
    public static final String duraStoreHostKey = "durastore-host";
    public static final String duraStorePortKey = "durastore-port";
    public static final String duraStoreContextKey = "durastore-context";
    public static final String duraServiceHostKey = "duraservice-host";
    public static final String duraServicePortKey = "duraservice-port";
    public static final String duraServiceContextKey = "duraservice-context";
    public static final String amaUrlKey = "ama-url";

    private String durastoreHost;
    private String durastorePort;
    private String durastoreContext;
    private String duraserviceHost;
    private String duraservicePort;
    private String duraserviceContext;
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

        } else if (key.equalsIgnoreCase(duraServiceHostKey)) {
            this.duraserviceHost = value;

        } else if (key.equalsIgnoreCase(duraServicePortKey)) {
            this.duraservicePort = value;

        } else if (key.equalsIgnoreCase(duraServiceContextKey)) {
            this.duraserviceContext = value;

        } else if (key.equalsIgnoreCase(amaUrlKey)) {
            this.amaUrl = value;

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
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

    public String getDuraserviceHost() {
        return duraserviceHost;
    }

    public void setDuraserviceHost(String duraserviceHost) {
        this.duraserviceHost = duraserviceHost;
    }

    public String getDuraservicePort() {
        return duraservicePort;
    }

    public void setDuraservicePort(String duraservicePort) {
        this.duraservicePort = duraservicePort;
    }

    public String getDuraserviceContext() {
        return duraserviceContext;
    }

    public void setDuraserviceContext(String duraserviceContext) {
        this.duraserviceContext = duraserviceContext;
    }

    public String getAmaUrl() {
        return amaUrl;
    }

    public void setAmaUrl(String amaUrl) {
        this.amaUrl = amaUrl;
    }
}
