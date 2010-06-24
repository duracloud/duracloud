/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient.webapp;

import java.util.HashMap;
import java.util.Map;

import org.duracloud.servicesadminclient.ServicesAdminClient;

import junit.framework.Assert;

public class ServiceConfigurationTester {

    private final ServicesAdminClient client;

    private Map<String, String> configOrig;

    private Map<String, String> configNew;

    private final String configId = "org.duracloud.test.config";

    private final String servicePidKey = "service.pid";

    public ServiceConfigurationTester(ServicesAdminClient client) {
        Assert.assertNotNull(client);
        this.client = client;

        setUp();
    }

    private void setUp() {
        configOrig = new HashMap<String, String>();
        configNew = new HashMap<String, String>();

        for (int i = 0; i < 3; ++i) {
            String key = "key" + i;
            String val = "val" + i;
            configOrig.put(key, val + "-orig");
            configNew.put(key, val + "-new");
        }
    }

    public void testServiceConfiguration() throws Exception {
        // Allow tomcat to come up.
        Thread.sleep(5000);

        // Post and verify original-config.
        getClient().postServiceConfig(configId, configOrig);

        // Allow config to propagate.
        Thread.sleep(100);

        Map<String, String> props = getClient().getServiceConfig(configId);
        verifyConfiguration(configOrig, props);

        // Post and verify updated-config.
        getClient().postServiceConfig(configId, configNew);

        // Allow config to propagate.
        Thread.sleep(100);

        props = getClient().getServiceConfig(configId);
        verifyConfiguration(configNew, props);

    }

    private void verifyConfiguration(Map<String, String> configExpected,
                                     Map<String, String> configFound) {
        Assert.assertNotNull(configFound);

        // The 'servicePidKey' is automatically inserted by the OSGi framework.
        String pid = configFound.get(servicePidKey);
        Assert.assertNotNull(pid);
        Assert.assertEquals(configId, pid);
        configFound.remove(servicePidKey);

        Assert.assertEquals(configExpected.size(), configFound.size());

        for (String key : configExpected.keySet()) {
            Assert.assertTrue(configFound.containsKey(key));
            Assert.assertEquals(configExpected.get(key), configFound.get(key));
        }
    }

    private ServicesAdminClient getClient() {
        return client;
    }
}
