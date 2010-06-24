/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hellowebappwrapper.osgi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.hellowebappwrapper.HelloWebappWrapper;
import static org.duracloud.services.hellowebappwrapper.osgi.AbstractDuracloudOSGiTestBasePax.BASE_DIR_PROP;
import org.junit.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 10, 2009
 */
public class HelloWebappWrapperTester {

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";
    private String context = FilenameUtils.getBaseName(getWarName());
    private int port = 18080;

    private String urlOrig = "http://example.org";
    private String urlRunning = "http://127.\\d.\\d.1:" + port + "/" + context;
    private HelloWebappWrapper wrapper;

    public HelloWebappWrapperTester(HelloWebappWrapper wrapper)
        throws IOException {
        this.wrapper = wrapper;

        // set up war to deploy
        File war = getWar();
        String serviceId = wrapper.getServiceId();
        File workDir = new File(wrapper.getServiceWorkDir());

        FileUtils.copyFileToDirectory(war, workDir);
    }

      protected File getWar() throws FileNotFoundException {
        String baseDir = System.getProperty(BASE_DIR_PROP);
        Assert.assertNotNull(baseDir);

        String resourceDir = baseDir + File.separator + "src/test/resources/";
        return new File(resourceDir, getWarName());
    }

    private String getWarName() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return "hellowebapp-" + version + ".war";
    }

    protected void testHelloWebappWrapper() throws Exception {
        Throwable error = null;
        try {
            doTest();
        } catch (Throwable e) {
            error = e;
        } finally {
            doTearDown();
        }

        String msg = (error == null ? "no error" : error.getMessage());
        Assert.assertNull(msg, error);
    }

    private void doTearDown() {
        try {
            wrapper.stop();
        } catch (Exception e) {
            // do nothing.
        }
    }

    private void doTest() throws Exception {
        verifyURL(urlOrig);

        ComputeService.ServiceStatus status = wrapper.getServiceStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.INSTALLED, status);

        wrapper.start();
        status = wrapper.getServiceStatus();
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED, status);

        verifyURL(urlRunning);

        wrapper.stop();
        status = wrapper.getServiceStatus();
        Assert.assertEquals(ComputeService.ServiceStatus.STOPPED, status);

        verifyURL(urlOrig);
    }

    private void verifyURL(String expectedURL) {
        Map<String, String> props = wrapper.getServiceProps();
        Assert.assertNotNull(props);

        String urlProp = props.get("url");
        Assert.assertNotNull(urlProp);
        Assert.assertTrue(urlProp.matches(expectedURL));
    }
}
