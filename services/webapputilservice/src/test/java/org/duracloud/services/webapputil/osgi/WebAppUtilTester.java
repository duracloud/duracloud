/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.osgi;

import org.apache.commons.io.FileUtils;
import org.duracloud.services.webapputil.WebAppUtil;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: Dec 7, 2009
 */
public class WebAppUtilTester extends WebAppUtilTestBase {

    private WebAppUtil webappUtil;
    private String SERVICE_ID = "howdy";
    private String BINARIES_FILE_NAME = "apache-tomcat-6.0.20.zip";

    private final static String BASE_DIR_PROP = "base.dir";

    public WebAppUtilTester(WebAppUtil webappUtil) throws Exception {
        this.webappUtil = webappUtil;

        populateBundleHome();
    }

    private void populateBundleHome() throws Exception {
        File serviceWork = new File(webappUtil.getServiceWorkDir());
        File tomcatBinaries = new File(getResourceDir(), BINARIES_FILE_NAME);

        FileUtils.copyFileToDirectory(tomcatBinaries, serviceWork);
    }

    public void testWebAppUtil() {
        Throwable error = null;
        try {
            doTest();
        } catch (Throwable e) {
            error = e;
        } finally {
            doTearDown(webappUtil);
        }

        String msg = (error == null ? "no error" : error.getMessage());
        Assert.assertNull(msg, error);
    }

    private void doTest() throws Exception {
        super.war = getWar();
        super.url = webappUtil.deploy(SERVICE_ID, war);
        verifyDeployment(url, true);

        webappUtil.unDeploy(url);
        verifyDeployment(url, false);
    }

    protected InputStream getWar() throws FileNotFoundException {
        File zipBagFile = new File(getResourceDir(), getWarFilename());
        return new FileInputStream(zipBagFile);
    }

    private String getWarFilename() {
        return "hellowebapp-" + getVersion() + ".war";
    }

    private String getResourceDir() {
        String baseDir = System.getProperty(BASE_DIR_PROP);
        Assert.assertNotNull(baseDir);

        return baseDir + File.separator + "src/test/resources/";
    }
}
