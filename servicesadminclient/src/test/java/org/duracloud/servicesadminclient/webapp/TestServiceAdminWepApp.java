/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient.webapp;

import junit.framework.Assert;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TestServiceAdminWepApp {

    private final static Logger log = LoggerFactory.getLogger(
        TestServiceAdminWepApp.class);

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";
    protected static final String BASE_DIR_PROP = "base.dir";

    private final static String TEST_SERVICE_NAME = "HelloService";

    private ServicesAdminClient client;

    @Test
    public void testServiceInstallUninstallFlow() throws Exception {
        ServiceInstallUninstallFlowTester tester = new ServiceInstallUninstallFlowTester(
            getTestBundleFromResourceDir(),
            getClient());
        tester.testNewServiceFlow();
    }

    @Test
    public void testServiceStatus() throws Exception {
        ServiceStatusReporterTester tester = new ServiceStatusReporterTester(
            getTestBundleFromResourceDir(),
            getClient());
        tester.testGetStatus();
    }

    @Test
    public void testServiceProps() throws Exception {
        ServicePropsFinderTester tester = new ServicePropsFinderTester(
            getTestBundleFromResourceDir(),
            getClient());
        tester.testGetProps();
    }

    @Test
    public void testServiceConfiguration() throws Exception {
        ServiceConfigurationTester tester = new ServiceConfigurationTester(
            getClient());
        tester.testServiceConfiguration();
    }

    private File getTestBundleFromResourceDir() throws Exception {
        String baseDir = System.getProperty(BASE_DIR_PROP);
        Assert.assertNotNull(baseDir);

        String resourceDir = baseDir + File.separator + "src/test/resources/";

        return new File(resourceDir, getTestBundleFilename());
    }

    private ServicesAdminClient getClient() {
        if (client == null) {
            client = new ServicesAdminClient();
            client.setRester(new RestHttpHelper());
            client.setBaseURL(getBaseUrl());
        }
        return client;
    }

    protected static boolean testServiceFound(String serviceName) {
        return serviceName.contains(TEST_SERVICE_NAME);
    }

    private String getBaseUrl() {
        String version = getVersion().replace("-", ".");

        // Port:8089 is defined in the 'tomcatconfig' project
        return "http://localhost:8089/org.duracloud.services.admin_" + version;
    }

    private String getTestBundleFilename() {
        return "helloservice-" + getVersion() + ".jar";
    }

    private String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }
}
