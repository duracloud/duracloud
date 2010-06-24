/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.common.util.ApplicationConfig;

import java.util.Properties;

/**
 * This class provides configuration properties associated with the duracloud
 * service client
 *
 * @author Bill Branan
 */
public class ServiceClientConfig
        extends ApplicationConfig {

    private static String SERVICECLIENT_PROPERTIES_NAME =
            "test-serviceclient.properties";

    private static String configFileName;

    private static String portKey = "port";

    private static Properties getProps() throws Exception {
        return getPropsFromResource(getConfigFileName());
    }

    public static String getPort() throws Exception {
        return getProps().getProperty(portKey);
    }

    public static void setConfigFileName(String name) {
        configFileName = name;
    }

    public static String getConfigFileName() {
        if (configFileName == null) {
            configFileName = SERVICECLIENT_PROPERTIES_NAME;
        }
        return configFileName;
    }

}