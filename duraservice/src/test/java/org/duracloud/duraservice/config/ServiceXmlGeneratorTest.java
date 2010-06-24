/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.config;

import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Properties;

public class ServiceXmlGeneratorTest {

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    @Test
    public void testBuildServiceList() {
        String ver = getVersion();

        ServiceXmlGenerator serviceXmlGenerator = new ServiceXmlGenerator(ver);
        List<ServiceInfo> serviceInfos = serviceXmlGenerator.buildServiceList();
        Assert.assertNotNull(serviceInfos);

        int NUM_SERVICES = 6;
        Assert.assertEquals(NUM_SERVICES, serviceInfos.size());

        boolean foundHello = false;
        boolean foundReplication = false;
        boolean foundImagemagick = false;
        boolean foundWebapputil = false;
        boolean foundHellowebappwrapper = false;
        boolean foundJ2k = false;
        boolean foundImageconversion = false;
        boolean foundMediaStreaming = false;

        for (ServiceInfo serviceInfo : serviceInfos) {
            String contentId = serviceInfo.getContentId();
            Assert.assertNotNull(contentId);
            if (contentId.equals("helloservice-" + ver + ".jar")) {
                foundHello = true;
                verifyHello();

            } else if (contentId.equals("replicationservice-" + ver + ".zip")) {
                foundReplication = true;
                verifyReplication(serviceInfo);

            } else if (contentId.equals("imagemagickservice-" + ver + ".zip")) {
                foundImagemagick = true;
                verifyImagemagick(serviceInfo);

            } else if (contentId.equals("webapputilservice-" + ver + ".zip")) {
                foundWebapputil = true;
                verifyWebapputil(serviceInfo);

            } else if (contentId.equals("hellowebappwrapper-" + ver + ".zip")) {
                foundHellowebappwrapper = true;
                verifyHellowebappwrapper(serviceInfo);

            } else if (contentId.equals("j2kservice-" + ver + ".zip")) {
                foundJ2k = true;
                verifyJ2k(serviceInfo);

            } else if (contentId.equals(
                "imageconversionservice-" + ver + ".zip")) {
                foundImageconversion = true;
                verifyImageconversion(serviceInfo);

            } else if (contentId.equals(
                "mediastreamingservice-" + ver + ".zip")) {
                foundMediaStreaming = true;
                verifyMediaStreaming(serviceInfo);

            } else {
                Assert.fail("unexpected contentId: " + contentId);
            }
        }

        //Assert.assertTrue(foundHello);
        Assert.assertTrue(foundReplication);
        Assert.assertTrue(foundImagemagick);
        Assert.assertTrue(foundWebapputil);
        //Assert.assertTrue(foundHellowebappwrapper);
        Assert.assertTrue(foundJ2k);
        Assert.assertTrue(foundImageconversion);
    }

    private void verifyHello() {
        Assert.assertTrue("I need an implementation", true);
    }

    private void verifyReplication(ServiceInfo serviceInfo) {
        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        Assert.assertNotNull(systemConfigs);
        Assert.assertEquals(6, systemConfigs.size());

        verifyDurastoreCredential(systemConfigs);
    }

    private void verifyImagemagick(ServiceInfo serviceInfo) {
        Assert.assertTrue("I need an implementation", true);
    }

    private void verifyWebapputil(ServiceInfo serviceInfo) {
        Assert.assertTrue("I need an implementation", true);
    }

    private void verifyHellowebappwrapper(ServiceInfo serviceInfo) {
        Assert.assertTrue("I need an implementation", true);
    }

    private void verifyJ2k(ServiceInfo serviceInfo) {
        Assert.assertTrue("I need an implementation", true);
    }

    private void verifyImageconversion(ServiceInfo serviceInfo) {
        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        Assert.assertNotNull(systemConfigs);
        Assert.assertEquals(6, systemConfigs.size());

        verifyDurastoreCredential(systemConfigs);
    }

    private void verifyMediaStreaming(ServiceInfo serviceInfo) {
        List<UserConfig> userConfigs = serviceInfo.getUserConfigs();
        Assert.assertNotNull(userConfigs);
        Assert.assertEquals(2, userConfigs.size());

        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        Assert.assertNotNull(systemConfigs);
        Assert.assertEquals(5, systemConfigs.size());

        verifyDurastoreCredential(systemConfigs);
    }

    private void verifyDurastoreCredential(List<SystemConfig> systemConfigs) {
        boolean foundUsername = false;
        boolean foundPassword = false;
        for (SystemConfig systemConfig : systemConfigs) {
            String name = systemConfig.getName();
            String value = systemConfig.getValue();
            Assert.assertNotNull(name);
            Assert.assertNotNull(value);

            if (name.equals("username")) {
                foundUsername = true;
                Assert.assertEquals("$DURASTORE-USERNAME", value);
            } else if (name.equals("password")) {
                foundPassword = true;
                Assert.assertEquals("$DURASTORE-PASSWORD", value);
            }
        }
        Assert.assertTrue(foundUsername);
        Assert.assertTrue(foundPassword);
    }

    @Test
    public void testGenerate() throws Exception {
        TestConfig config = new TestConfig();
        String targetDir = config.getTargetDir();
        URI targetDirUri = new URI(targetDir);
        File targetDirFile = new File(targetDirUri);

        ServiceXmlGenerator xmlGenerator = new ServiceXmlGenerator(getVersion());
        xmlGenerator.generateServiceXml(targetDirFile.getAbsolutePath());
    }

    private String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

    private class TestConfig extends ApplicationConfig {
        private String propName = "test-duraservice.properties";

        private Properties getProps() throws Exception {
            return getPropsFromResource(propName);
        }

        public String getTargetDir() throws Exception {
            return getProps().getProperty("targetdir");
        }
    }
}