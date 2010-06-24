/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.Assert;

import java.util.Properties;

/**
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class BaseTestUtil extends ApplicationConfig {

    private static String configFileName = "test-appconfig.properties";

    private static String port;
    private static final String defaultPort = "8080";

    private static final String PORT_KEY = "port";
    private static final String VERSION_KEY = "version";
    private static final String SERVICES_ADMIN_PORT_KEY = "servicesAdminPort";
    private static final String SERVICES_ADMIN_CONTEXT_KEY = "servicesAdminContext";

    private static Properties getProps() {
        return getPropsFromResource(configFileName);
    }

    public static String getPort() {
        if (port == null) {
            port = getProps().getProperty(PORT_KEY);
        }

        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = defaultPort;
        }

        return port;
    }

    public static String getServicesAdminPort() {
        return getProps().getProperty(SERVICES_ADMIN_PORT_KEY);
    }

    public static String getServicesAdminContext() {
        String baseUrl = getProps().getProperty(SERVICES_ADMIN_CONTEXT_KEY);
        String version = getProps().getProperty(VERSION_KEY);

        Assert.assertNotNull(version);
        return baseUrl + version.replace("-", ".");
    }

    protected static Credential getCredential(ResourceType resource) {
        UnitTestDatabaseUtil dbUtil = null;
        try {
            dbUtil = new UnitTestDatabaseUtil();
        } catch (Exception e) {
            System.err.println("ERROR from unitTestDB: " + e.getMessage());
        }

        Credential credential = null;
        try {
            credential = dbUtil.findCredentialForResource(resource);
        } catch (Exception e) {
            System.err.print("ERROR getting credential: " + e.getMessage());
        }
        return credential;
    }


}
