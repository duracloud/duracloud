/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient.webapp;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.junit.Assert;

import java.io.File;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 14, 2009
 */
public class ServicePropsFinderTester extends ServiceInstallTestBase {

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    public ServicePropsFinderTester(File testBundle,
                                    ServicesAdminClient client) {
        super(client, testBundle);
    }

    public void testGetProps() throws Exception {
        // Allow tomcat to come up.
        Thread.sleep(5000);

        // install service
        installTestBundle();

        // Allow test-service to come up.
        Thread.sleep(5000);

        String serviceId = FilenameUtils.getBaseName(getTestBundle().getName());

        Map<String, String> props = getClient().getServiceProps(serviceId);
        Assert.assertNotNull(props);
        Assert.assertTrue(props.size() > 0);

        String value = props.get("serviceId");
        Assert.assertNotNull(value);
        Assert.assertEquals("helloservice-" + getVersion(), value);

        uninstallTestBundle();
    }

    private String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

}