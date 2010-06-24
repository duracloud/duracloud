/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice.osgi;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.services.j2kservice.J2kWebappWrapper;
import org.duracloud.services.webapputil.WebAppUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Dec 20, 2009
 */
public class TestServices extends AbstractDuracloudOSGiTestBasePax {

    private final Logger log = LoggerFactory.getLogger(TestServices.class);

    private final int MAX_TRIES = 10;
    private String TOMCAT_ZIP_NAME = "apache-tomcat-6.0.20.zip";
    private String DJATOKA_ZIP_NAME = "adore-djatoka-1.1.zip";

    private J2kWebappWrapper j2kWebappWrapper;
    private WebAppUtil webappUtil;

    @Before
    public void setUp() throws Exception {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        populateWebappUtilResources();
    }

    protected void populateWebappUtilResources() throws Exception {
        String utilId = getWebappUtil().getServiceId();
        File tomcatWork = getBundleHome().getServiceWork(utilId);
        File tomcatZip = new File(getResourceDir(), TOMCAT_ZIP_NAME);
        FileUtils.copyFileToDirectory(tomcatZip, tomcatWork);

        String j2kId = getJ2kWebappWrapper().getServiceId();
        File j2kWork = getBundleHome().getServiceWork(j2kId);
        File j2kZip = new File(getResourceDir(), DJATOKA_ZIP_NAME);
        FileUtils.copyFileToDirectory(j2kZip, j2kWork);

        // push the workdirs to the services.
        getWebappUtil().setServiceWorkDir(tomcatWork.getAbsolutePath());
        getJ2kWebappWrapper().setServiceWorkDir(j2kWork.getAbsolutePath());
    }

    private String getResourceDir() {
        String baseDir = System.getProperty(BASE_DIR_PROP);
        org.junit.Assert.assertNotNull(baseDir);

        return baseDir + File.separator + "src/test/resources/";
    }

    private BundleHome getBundleHome() {
        String home = System.getProperty(BUNDLE_HOME_PROP);
        Assert.assertNotNull(home);

        return new BundleHome();
    }

    @After
    public void tearDown() throws Exception {
        String j2kWorkDir = getJ2kWebappWrapper().getServiceWorkDir();
        String utilWorkDir = getWebappUtil().getServiceWorkDir();

        FileUtils.deleteDirectory(new File(j2kWorkDir));
        FileUtils.deleteDirectory(new File(utilWorkDir));
    }

    @Test
    public void testDjatokaWebappWrapper() throws Exception {
        log.debug("testing J2kWebappWrapper");

        J2kWebappWrapperTester tester = new J2kWebappWrapperTester(
            getJ2kWebappWrapper());
        tester.testDjatokaWebappWrapper();
    }

    private Object getService(String serviceInterface) throws Exception {
        return getService(serviceInterface, null);
    }

    private Object getService(String serviceInterface, String filter)
        throws Exception {
        ServiceReference[] refs = getBundleContext().getServiceReferences(
            serviceInterface,
            filter);

        int count = 0;
        while ((refs == null || refs.length == 0) && count < MAX_TRIES) {
            count++;
            log.debug("Trying to find service: '" + serviceInterface + "'");
            Thread.sleep(1000);
            refs = getBundleContext().getServiceReferences(serviceInterface,
                                                           filter);
        }
        Assert.assertNotNull("service not found: " + serviceInterface, refs[0]);
        log.debug(getPropsText(refs[0]));
        return getBundleContext().getService(refs[0]);
    }

    private String getPropsText(ServiceReference ref) {
        StringBuilder sb = new StringBuilder("properties:");
        for (String key : ref.getPropertyKeys()) {
            sb.append("\tprop: [" + key);
            sb.append(":" + ref.getProperty(key) + "]\n");
        }
        return sb.toString();
    }

    private J2kWebappWrapper getJ2kWebappWrapper() throws Exception {
        if (j2kWebappWrapper == null) {
            j2kWebappWrapper = (J2kWebappWrapper) getService(ComputeService.class.getName(),
                                                             "(duraService=j2kservice)");
        }
        Assert.assertNotNull(j2kWebappWrapper);
        return j2kWebappWrapper;
    }

    private WebAppUtil getWebappUtil() throws Exception {
        if (webappUtil == null) {
            webappUtil = (WebAppUtil) getService(ComputeService.class.getName(),
                                                 "(duraService=webapputilservice)");
        }
        Assert.assertNotNull(webappUtil);
        return webappUtil;
    }

}