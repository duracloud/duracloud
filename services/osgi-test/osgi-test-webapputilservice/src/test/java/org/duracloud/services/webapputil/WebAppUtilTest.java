/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil;

import org.apache.commons.io.FileUtils;
import org.duracloud.services.webapputil.internal.WebAppUtilImpl;
import org.duracloud.services.webapputil.osgi.WebAppUtilTestBase;
import org.duracloud.services.webapputil.tomcat.TomcatUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Nov 30, 2009
 */
public class WebAppUtilTest extends WebAppUtilTestBase {

    private WebAppUtilImpl webappUtil;
    private String serviceWorkPath = "target/webapputil-test";
    private String testResourcesPath = "src/test/resources";
    private String serviceId = "hello";
    private String binariesName = "apache-tomcat-6.0.20.zip";
    private String warName = "hellowebapp-" + getVersion() + ".war";
    private int port = 18080;

    @Before
    public void setUp() throws IOException {
        File serviceWorkDir = populateServiceWork();

        TomcatUtil tomcatUtil = new TomcatUtil();
        tomcatUtil.setBinariesZipName(binariesName);

        webappUtil = new WebAppUtilImpl();
        webappUtil.setServiceId(serviceId);
        webappUtil.setNextPort(port);
        webappUtil.setServiceWorkDir(serviceWorkDir.getAbsolutePath());
        webappUtil.setTomcatUtil(tomcatUtil);

        war = new FileInputStream(new File(serviceWorkDir, warName));
    }

    private File populateServiceWork() throws IOException {
        File serviceWorkDir = new File(serviceWorkPath);
        serviceWorkDir.mkdirs();

        File testResources = new File(testResourcesPath);
        File binaries = new File(testResources, binariesName);
        File warFile = new File(testResources, warName);

        FileUtils.copyFileToDirectory(binaries, serviceWorkDir);
        FileUtils.copyFileToDirectory(warFile, serviceWorkDir);

        return serviceWorkDir;
    }

    @After
    public void tearDown() {
        doTearDown(webappUtil);
    }

    @Test
    public void testDeploy() throws Exception {
        url = webappUtil.deploy(serviceId, war);
        Thread.sleep(3000);

        verifyDeployment(url, true);
    }

    @Test
    public void testDeployEnv() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put("argEnv", "argVal");
        url = webappUtil.deploy(serviceId, war, env);
        Thread.sleep(3000);

        verifyDeployment(url, true);
    }

    @Test
    public void testUnDeploy() throws Exception {
        url = webappUtil.deploy(serviceId, war);
        Thread.sleep(3000);
        verifyDeployment(url, true);

        webappUtil.unDeploy(url);
        verifyDeployment(url, false);
    }

}
