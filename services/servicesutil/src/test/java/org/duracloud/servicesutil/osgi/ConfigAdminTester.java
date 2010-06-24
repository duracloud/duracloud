/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import junit.framework.Assert;
import static junit.framework.Assert.assertNotNull;
import org.duracloud.services.ComputeService;
import org.duracloud.servicesutil.util.DuraConfigAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConfigAdminTester {

    private final Logger log = LoggerFactory.getLogger(ConfigAdminTester.class);

    private final DuraConfigAdmin configAdmin;

    private final ComputeService hello;

    private final static String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    public ConfigAdminTester(DuraConfigAdmin configAdmin, ComputeService hello) {
        assertNotNull(configAdmin);
        assertNotNull(hello);

        this.configAdmin = configAdmin;
        this.hello = hello;
    }

    public void testConfigAdmin() throws Exception {
        StringBuffer sb = new StringBuffer("testing ConfigAdmin\n");

        String newValue = "tester.text";
        String key = "text";

        String origText = hello.describe();
        assertNotNull(sb.toString(), origText);
        sb.append("origText: '" + origText + "'\n");

        Map<String, String> props = configAdmin.getConfiguration(getConfigPID());
        assertNotNull(sb.toString(), props);

        props.put(key, "tester.text");

        configAdmin.updateConfiguration(getConfigPID(), props);

        // Make sure thread updating container props has time to complete.
        Thread.sleep(100);

        String newText = hello.describe();
        assertNotNull(sb.toString(), newText);
        sb.append("newText : '" + newText + "'\n");

        Assert.assertTrue(sb.toString(), !newText.equals(origText));
        Assert.assertTrue(sb.toString(), newText.indexOf(newValue) > -1);

        if (log.isDebugEnabled()) {
            sb.append(configDetailsText());
        }
        log.debug(sb.toString());
    }

    private String configDetailsText() throws Exception {
        StringBuffer sb = new StringBuffer();
        Map<String, String> props = configAdmin.getConfiguration(getConfigPID());
        sb.append("\tProps: ");
        assertNotNull(props);
        for (String key : props.keySet()) {
            String val = props.get(key);
            sb.append(" [" + key + "|" + val + "]");
        }
        sb.append("\n");
        return sb.toString();
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
