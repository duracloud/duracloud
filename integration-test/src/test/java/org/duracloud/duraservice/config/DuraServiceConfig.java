/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.config;

import org.duracloud.common.util.ApplicationConfig;

import java.util.Properties;

/**
 * This class provides configuration properties associated with the duracloud
 * duraservice.
 *
 * @author Bill Branan
 */
public class DuraServiceConfig
        extends ApplicationConfig {

    private static final String DURASERVICE_PROPERTIES_NAME =
        "test-duraservice.properties";
    
    private static final String PORT_KEY = "port";
    private static final String VERSION_KEY = "version";
    private static final String SERVICES_ADMIN_URL_KEY = "servicesAdminURL";

    private String configFileName = null;

    private Properties getProps() {
        return getPropsFromResource(getConfigFileName());
    }

    public String getPort() {
        return getProps().getProperty(PORT_KEY);
    }

    public String getServicesAdminUrl() {
        String baseUrl = getProps().getProperty(SERVICES_ADMIN_URL_KEY);
        String version = getProps().getProperty(VERSION_KEY);
        return baseUrl + version.replace("-", ".");
    }

    public void setConfigFileName(String name) {
        configFileName = name;
    }

    public String getConfigFileName() {
        if (configFileName == null) {
            configFileName = DURASERVICE_PROPERTIES_NAME;
        }
        return configFileName;
    }

}
