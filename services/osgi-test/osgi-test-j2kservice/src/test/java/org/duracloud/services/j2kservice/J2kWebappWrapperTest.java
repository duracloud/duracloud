/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice;

import org.apache.commons.io.FileUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.services.j2kservice.osgi.J2kWebappWrapperTestBase;
import org.duracloud.services.webapputil.internal.WebAppUtilImpl;
import org.duracloud.services.webapputil.tomcat.TomcatUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrew Woods
 *         Date: Dec 20, 2009
 */
public class J2kWebappWrapperTest extends J2kWebappWrapperTestBase {

    private File resourceDir = new File("src/test/resources");

    private BundleHome bundleHome = new BundleHome();

    private String serviceId = "j2k-wrapper";
    private String webappId = "webapputilservice-1.0.0";
    private WebAppUtilImpl webappUtil;
    private TomcatUtil tomcatUtil;

    private String tomcatZipName = "apache-tomcat-6.0.20.zip";

    private int port = 38282;
    private final String APACHE_FLAG = "WITH_APACHE";

    @Before
    public void setUp() throws IOException {
        tomcatUtil = new TomcatUtil();
        tomcatUtil.setBinariesZipName(tomcatZipName);

        webappUtil = new WebAppUtilImpl();
        webappUtil.setServiceId(webappId);
        webappUtil.setNextPort(port);
        webappUtil.setTomcatUtil(tomcatUtil);
        webappUtil.setServiceWorkDir(bundleHome.getServiceWork(webappId).getAbsolutePath());

        wrapper = new J2kWebappWrapper();
        wrapper.setServiceStatus(ComputeService.ServiceStatus.INSTALLED);
        wrapper.setServiceId(serviceId);
        wrapper.setUrl(urlOrig);
        wrapper.setWarName(warName);
        wrapper.setWebappUtil(webappUtil);
        wrapper.setServiceWorkDir(bundleHome.getServiceWork(serviceId).getAbsolutePath());
        wrapper.setPlatform(getPlatform());
        wrapper.setJ2kZipName(zipName);

        File tomcatZip = new File(resourceDir, tomcatZipName);
        File webappWork = bundleHome.getServiceWork(webappUtil.getServiceId());
        FileUtils.copyFileToDirectory(tomcatZip, webappWork);

        File j2kZip = new File(resourceDir, zipName);
        File wrapperWork = bundleHome.getServiceWork(wrapper.getServiceId());
        FileUtils.copyFileToDirectory(j2kZip, wrapperWork);
    }

    private String getPlatform() {
        String os = System.getProperty("os.name");
        return os.equalsIgnoreCase("windows") ? "Win32" : "Linux-x86-32";
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

        System.clearProperty(APACHE_FLAG);
    }

    @Test
    public void testStartStopCycle() throws Exception {
        super.testStopStartCycle(getUrlPattern());
    }

    @Test
    public void testStartStopCycleWithApache() throws Exception {
        System.setProperty(APACHE_FLAG, "true");
        super.testStopStartCycle(getUrlPatternWithApache());
    }

    @Test
    public void testImageServing() throws Exception {
        super.testImageServing(getUrlPattern());
    }

    private String getUrlPattern() {
        return urlRunningBase + ":" + port + "/" + context;
    }

    private String getUrlPatternWithApache() {
        return urlRunningBase + "/" + context + "-p" + port;
    }
}
