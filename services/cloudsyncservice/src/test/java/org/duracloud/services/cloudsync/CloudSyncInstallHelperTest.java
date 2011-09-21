/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.cloudsync;

import org.duracloud.services.cloudsync.error.CloudSyncWrapperException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 9/20/11
 */
public class CloudSyncInstallHelperTest {

    private CloudSyncInstallHelper helper;

    private static String warName = "cloudsync.war";
    private static File home = new File("target", "helper-test");

    @Before
    public void setUp() throws IOException {
        helper = new CloudSyncInstallHelper(home);
    }

    @Test
    public void testEnv() {
        Map<String, String> env = helper.getInstallEnv();
        Assert.assertNotNull(env);

        String cloudSyncHome = "CLOUDSYNC_HOME";
        String javaOpts = "JAVA_OPTS";

        Assert.assertEquals(2, env.size());
        Assert.assertNotNull(cloudSyncHome, env.get(cloudSyncHome));
        Assert.assertNotNull(javaOpts, env.get(javaOpts));

        Assert.assertNull(env.get("junk"));
    }

    @Test
    public void testWarError() {
        try {
            helper.getWarFile(warName);
            Assert.fail("exception expected");

        } catch (CloudSyncWrapperException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

}
