/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.servicesutil.osgi;

import junit.framework.Assert;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.duracloud.services.ComputeService;
import org.duracloud.servicesutil.util.DuraConfigAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrew Woods
 *         Date: Jan 1, 2010
 */
public class ConfigAdminTester {

    private final Logger log = LoggerFactory.getLogger(ConfigAdminTester.class);

    private final DuraConfigAdmin configAdmin;

    private final static String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    public ConfigAdminTester(DuraConfigAdmin configAdmin) {
        assertNotNull(configAdmin);

        this.configAdmin = configAdmin;
    }

    public void testConfigAdmin() throws Exception {
        StringBuffer sb = new StringBuffer("testing ConfigAdmin\n");

        String key0 = "key-0";
        String key1 = "key-1";
        String val0 = "tester.text-0";
        String val1 = "tester.text-1";
        String val2 = "tester.text-2";

        Map<String, String> config = new HashMap<String, String>();
        configAdmin.updateConfiguration(getConfigPID(), config);

        Map<String, String> props = configAdmin.getConfiguration(getConfigPID());
        assertNotNull(sb.toString(), props);
        int baseSize = props.size();

        config.put(key0, val0);
        config.put(key1, val1);
        verifyConfigUpdate(config, baseSize);

        config = new HashMap<String, String>();
        config.put(key0, val2);
        verifyConfigUpdate(config, baseSize);

        config = new HashMap<String, String>();
        verifyConfigUpdate(config, baseSize);
    }

    private void verifyConfigUpdate(Map<String, String> config, int baseSize)
        throws Exception {
        configAdmin.updateConfiguration(getConfigPID(), config);

        // Make sure thread updating container props has time to complete.
        Thread.sleep(100);

        Map<String, String> props = configAdmin.getConfiguration(getConfigPID());
        assertNotNull(props);
        Assert.assertEquals(config.toString() + " ?= " + props.toString(),
                            baseSize + config.size(),
                            props.size());
        for (String key : config.keySet()) {
            String val = config.get(key);
            Assert.assertTrue(key, props.containsKey(key));
            Assert.assertTrue(val, props.containsValue(val));
        }
    }

    private String getConfigPID() {
        return "helloservice-" + getVersion() + ".jar";
    }

    private String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

}
