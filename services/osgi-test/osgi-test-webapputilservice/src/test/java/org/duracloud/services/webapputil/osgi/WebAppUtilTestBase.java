/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.osgi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.services.webapputil.WebAppUtil;
import org.junit.Assert;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * This class aggregates some test methods used in both the unit and
 * osgi-integration tests for WebAppUtil.
 *
 * @author Andrew Woods
 *         Date: Dec 7, 2009
 */
public class WebAppUtilTestBase {

    private String PROJECT_VERSION_PROP = "PROJECT_VERSION";
    protected InputStream war;
    protected URL url;

    protected RestHttpHelper httpHelper = new RestHttpHelper();

    protected void doTearDown(WebAppUtil util) {
        try {
            util.unDeploy(url);
        } catch (Exception e) {
        }

        try {
            File workDir = new File(util.getServiceWorkDir());
            if(workDir.exists()) {
                FileUtils.deleteDirectory(workDir);
            }
        } catch (Exception e) {
        }

        util = null;
        IOUtils.closeQuietly(war);
    }

    protected void verifyDeployment(URL url, boolean success) throws Exception {
        Assert.assertNotNull(url);

        RestHttpHelper.HttpResponse response = null;
        try {
            response = httpHelper.get(url.toString());
            Assert.assertTrue(success);
        } catch (Exception e) {
            Assert.assertTrue("Error requesting: " + url, !success);
        }

        if (success) {
            Assert.assertNotNull(response);

            int maxTries = 10;
            int tries = 0;
            while (response.getStatusCode() != 200 && tries++ < maxTries) {
                Thread.sleep(1000);
                response = httpHelper.get(url.toString());
            }
            Assert.assertEquals(200, response.getStatusCode());

            String body = response.getResponseBody();
            Assert.assertNotNull(body);
            Assert.assertTrue(body, body.contains("Hello from DuraCloud"));
        }
    }

    protected String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }
}
