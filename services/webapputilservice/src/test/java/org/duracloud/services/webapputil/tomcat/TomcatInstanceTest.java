/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.tomcat;

import org.apache.commons.io.IOUtils;
import org.duracloud.common.web.RestHttpHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 2, 2009
 */
public class TomcatInstanceTest extends TomcatTestBase {

    private final String PROJECT_VERSION_PROP = "PROJECT_VERSION";
    private final int port = 12080;
    private final String tomcatURL = "http://localhost:" + port;
    private TomcatUtil tomcatUtil;
    private TomcatInstance tomcatInstance;
    private String context = "hello";

    private RestHttpHelper httpHelper = new RestHttpHelper();

    @Before
    public void setUp() throws IOException {
        tomcatUtil = new TomcatUtil();
        tomcatUtil.setResourceDir(getResourceDir());
        tomcatUtil.setBinariesZipName("apache-tomcat-6.0.20.zip");

        // Install required before tomcatUtil can be started.
        File installDir = getInstallDir("instance");
        tomcatInstance = tomcatUtil.installTomcat(installDir, port);

        int numFiles = 577;
        verifyInstall(tomcatInstance, numFiles);

        ensureNotStarted();
    }

    private void ensureNotStarted() {
        int maxTries = 20;
        int tries = 0;
        boolean isStarted = false;
        while (!isStarted && (tries++ < maxTries)) {
            try {
                verifyStarted(false);
                isStarted = true;
            } catch (Throwable e) {
                try {
                    tomcatInstance.stop();
                    Thread.sleep(500);
                } catch (Exception e1) {
                }
            }
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        tomcatUtil.unInstallTomcat(tomcatInstance);

        tomcatInstance = null;
        tomcatUtil = null;
    }

    @Test
    public void testStart() throws Exception {
        tomcatInstance.start();
        verifyStarted(true);
    }

    private void verifyStarted(boolean expected) throws Exception {
        RestHttpHelper.HttpResponse response = null;
        try {
            response = httpHelper.get(tomcatURL);
        } catch (Exception e) {
        }

        Assert.assertEquals(expected, response != null);
        if (expected) {
            Assert.assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testStartEnv() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put("argEnv", "valEnv");
        tomcatInstance.start(env);
        verifyStarted(true);
    }

    @Test
    public void testDeploy() throws Exception {
        tomcatInstance.start();
        verifyStarted(true);

        doDeploy();
        Assert.assertEquals(true, expectedDeployState(true));
    }

    private void doDeploy() throws Exception {
        String filename = "hellowebapp-" + getVersion() + ".war";
        File warFile = new File(getResourceDir(), filename);
        InputStream war = new FileInputStream(warFile);

        tomcatInstance.deploy(context, war);
        IOUtils.closeQuietly(war);
    }

    private String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        junit.framework.Assert.assertNotNull(version);
        return version;
    }

    private boolean expectedDeployState(boolean expected) {
        boolean done = false;
        int tries = 0;
        int maxTries = 5;
        while (!done && tries++ < maxTries) {
            try {
                verifyDeployed(expected);
                done = true;
            } catch (Throwable e) {
            }
        }

        return expected && done;
    }

    private void verifyDeployed(boolean expected) throws Exception {
        String url = tomcatURL + "/" + context;
        RestHttpHelper.HttpResponse response = httpHelper.get(url);
        Assert.assertNotNull(response);

        int expectedStatus = expected ? 200 : 404;

        int maxTries = 3;
        int tries = 0;
        while (response.getStatusCode() != expectedStatus &&
            tries++ < maxTries) {
            Thread.sleep(1000);
            response = httpHelper.get(url);
        }
        Assert.assertEquals(expectedStatus, response.getStatusCode());

        String body = response.getResponseBody();
        Assert.assertNotNull(body);
        if (expected) {
            Assert.assertTrue(body.contains("Hello from DuraCloud"));
        }

    }

    @Test
    public void testUnDeploy() throws Exception {
        tomcatInstance.start();
        verifyStarted(true);

        try {
            verifyDeployed(true);
        } catch (Throwable e) {
            doDeploy();
        }

        Assert.assertEquals(true, expectedDeployState(true));

        tomcatInstance.unDeploy(context);
        Assert.assertEquals(false, expectedDeployState(false));
    }

    @Test
    public void testStop() throws Exception {
        tomcatInstance.start();
        verifyStarted(true);

        tomcatInstance.stop();
        verifyStarted(false);
    }

}
