/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hellowebappwrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.services.webapputil.internal.WebAppUtilImpl;
import org.duracloud.services.webapputil.tomcat.TomcatUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 9, 2009
 */
public class HelloWebappWrapperTest {

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    private HelloWebappWrapper wrapper;
    private String serviceId = "howdywrapper";
    private String url = "http://example.org";
    private BundleHome bundleHome = new BundleHome();
    private String warName = "hellowebapp-"+getVersion()+".war";

    private WebAppUtilImpl webappUtil;
    private int port = 38080;

    private TomcatUtil tomcatUtil;
    private File resourceDir = new File("src/test/resources");
    private String binariesZipName = "apache-tomcat-6.0.20.zip";


    @Before
    public void setUp() throws IOException {
        tomcatUtil = new TomcatUtil();
        tomcatUtil.setBinariesZipName(binariesZipName);

        String webappUtilServiceId = "webapputilservice-"+getVersion();
        webappUtil = new WebAppUtilImpl();
        webappUtil.setServiceId(webappUtilServiceId);
        webappUtil.setNextPort(port);
        webappUtil.setServiceWorkDir(
            bundleHome.getServiceWork(webappUtilServiceId).getAbsolutePath());
        webappUtil.setTomcatUtil(tomcatUtil);

        wrapper = new HelloWebappWrapper();
        wrapper.setServiceStatus(ComputeService.ServiceStatus.INSTALLED);
        wrapper.setServiceId(serviceId);
        wrapper.setUrl(url);
        wrapper.setWebappWarName(warName);
        wrapper.setWebappUtil(webappUtil);
        wrapper.setServiceWorkDir(
            bundleHome.getServiceWork(serviceId).getAbsolutePath());

        File binaries = new File(resourceDir, binariesZipName);
        File webappWork = bundleHome.getServiceWork(webappUtil.getServiceId());
        FileUtils.copyFileToDirectory(binaries, webappWork);

        File war = new File(resourceDir, warName);
        File wrapperWork = bundleHome.getServiceWork(wrapper.getServiceId());
        FileUtils.copyFileToDirectory(war, wrapperWork);
    }

    @After
    public void tearDown() {
        try {
            wrapper.stop();
        } catch (Exception e) {
            // do nothing
        }
        tomcatUtil = null;
        webappUtil = null;
        wrapper = null;
    }

    @Test
    public void testStartStopCycle() throws Exception {
        verifyURL(url);

        ComputeService.ServiceStatus status = wrapper.getServiceStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.INSTALLED, status);

        wrapper.start();
        status = wrapper.getServiceStatus();
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED, status);

        String context = FilenameUtils.getBaseName(warName);
        String newURL = "http://127.\\d.\\d.1:" + port + "/" + context;
        verifyURL(newURL);

        wrapper.stop();
        status = wrapper.getServiceStatus();
        Assert.assertEquals(ComputeService.ServiceStatus.STOPPED, status);

        verifyURL(url);
    }

    private void verifyURL(String expectedURL) {
        Map<String, String> props = wrapper.getServiceProps();
        Assert.assertNotNull(props);

        String urlProp = props.get("url");
        Assert.assertNotNull(urlProp);
        Assert.assertTrue(urlProp.matches(expectedURL));
    }

    private static String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

}
