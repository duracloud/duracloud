/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replication.osgi;

import junit.framework.Assert;
import org.duracloud.services.duplication.DuplicationService;
import org.duracloud.servicesutil.util.DuraConfigAdmin;

import java.util.HashMap;
import java.util.Map;

public class DynamicConfigTester {

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    private static final String HOST = "host";

    private static final String PORT = "port";

    private static final String CONTEXT = "context";

    private static final String BROKER_URL = "brokerURL";

    private static final String FROM_STORE_ID = "fromStoreId";

    private final DuraConfigAdmin configAdmin;

    private final DuplicationService duplicationService;

    public DynamicConfigTester(DuraConfigAdmin configAdmin,
                               DuplicationService duplicationService) {
        Assert.assertNotNull(configAdmin);
        Assert.assertNotNull(duplicationService);

        this.configAdmin = configAdmin;
        this.duplicationService = duplicationService;
    }

    public void testDynamicConfig() throws Exception {
        Map<String, String> defaultConfig = new HashMap<String, String>();
        defaultConfig.put(HOST, "localhost");
        defaultConfig.put(PORT, "8080");
        defaultConfig.put(CONTEXT, "durastore");
        defaultConfig.put(BROKER_URL, "tcp://localhost:61617");
        defaultConfig.put(FROM_STORE_ID, "1");

        verifyConfig(defaultConfig);

        Map<String, String> newConfig = new HashMap<String, String>();
        newConfig.put(HOST, "localhost-new");
        newConfig.put(PORT, "9999");
        newConfig.put(CONTEXT, "durastore-new");
        newConfig.put(BROKER_URL, "tcp://localhost:99999");
        newConfig.put(FROM_STORE_ID, "2");

        configAdmin.updateConfiguration(
            "duplicationservice-" + getVersion() + ".zip", newConfig);

        // Give time for config to propagate
        Thread.sleep(100);

        verifyConfig(newConfig);
    }

    private void verifyConfig(Map<String, String> config) {
        String host = duplicationService.getHost();
        String port = duplicationService.getPort();
        String context = duplicationService.getContext();
        String url = duplicationService.getBrokerURL();
        String fromId = duplicationService.getFromStoreId();

        Assert.assertNotNull(host);
        Assert.assertNotNull(port);
        Assert.assertNotNull(context);
        Assert.assertNotNull(url);
        Assert.assertNotNull(fromId);

        Assert.assertEquals(config.get(HOST), host);
        Assert.assertEquals(config.get(PORT), port);
        Assert.assertEquals(config.get(CONTEXT), context);
        Assert.assertEquals(config.get(BROKER_URL), url);
        Assert.assertEquals(config.get(FROM_STORE_ID), fromId);
    }

    private static String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

}
