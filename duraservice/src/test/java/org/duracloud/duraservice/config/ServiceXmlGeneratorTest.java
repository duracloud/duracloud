/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.config;

import org.apache.commons.io.IOUtils;
import org.duracloud.common.model.ServiceRegistryName;
import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServicesConfigDocument;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrew Woods
 *         Date: Jan 31, 2010
 */
public class ServiceXmlGeneratorTest {

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";
    private static final int NUM_SERVICES = 12;

    @Test
    public void testBuildServiceList() {
        String ver = getVersion();

        ServiceXmlGenerator serviceXmlGenerator = new ServiceXmlGenerator(ver);
        List<ServiceInfo> serviceInfos = serviceXmlGenerator.getServices();
        Assert.assertNotNull(serviceInfos);

        Assert.assertEquals(NUM_SERVICES, serviceInfos.size());

        boolean foundHello = false;
        boolean foundDuplication = false;
        boolean foundImagemagick = false;
        boolean foundWebapputil = false;
        boolean foundHellowebappwrapper = false;
        boolean foundJ2k = false;
        boolean foundImageconversion = false;
        boolean foundMediaStreaming = false;
        boolean foundFixity = false;
        boolean foundFixityTools = false;
        boolean foundBulkImageConversion = false;
        boolean foundAmazonFixity = false;
        boolean foundRepOnDemand = false;
        boolean foundCloudSync = false;

        for (ServiceInfo serviceInfo : serviceInfos) {
            String contentId = serviceInfo.getContentId();
            Assert.assertNotNull(contentId);
            if (contentId.equals("helloservice-" + ver + ".jar")) {
                foundHello = true;
                verifyHello();

            } else if (contentId.equals("duplicationservice-" + ver + ".zip")) {
                foundDuplication = true;
                verifyDuplication(serviceInfo);

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

            } else if (contentId.equals(
                "fixityservice-" + ver + ".zip")) {
                foundFixity = true;
                verifyFixity(serviceInfo);

            } else if (contentId.equals(
                "bitintegritytoolsservice-" + ver + ".zip")) {
                foundFixityTools = true;
                verifyFixityTools(serviceInfo);

            } else if (contentId.equals(
                "bulkimageconversionservice-" + ver + ".zip")) {
                foundBulkImageConversion = true;
                verifyBulkImageconversion(serviceInfo);

            } else if (contentId.equals(
                "amazonfixityservice-" + ver + ".zip")) {
                foundAmazonFixity = true;
                verifyAmazonFixity(serviceInfo);

            } else if (contentId.equals(
                "replication-on-demand-service-" + ver + ".zip")) {
                foundRepOnDemand = true;
                verifyRepOnDemand(serviceInfo);

            } else if (contentId.equals(
                "cloudsyncservice-" + ver + ".zip")) {
                foundCloudSync = true;
                verifyCloudSync(serviceInfo);

            } else {
                Assert.fail("unexpected contentId: " + contentId);
            }
        }

        //Assert.assertTrue(foundHello);
        //Assert.assertTrue(foundReplication);
        Assert.assertTrue(foundDuplication);
        Assert.assertTrue(foundImagemagick);
        Assert.assertTrue(foundWebapputil);
        //Assert.assertTrue(foundHellowebappwrapper);
        Assert.assertTrue(foundJ2k);
        Assert.assertTrue(foundImageconversion);
        Assert.assertTrue(foundMediaStreaming);
        Assert.assertTrue(foundFixity);
        Assert.assertTrue(foundFixityTools);
        Assert.assertTrue(foundBulkImageConversion);
        Assert.assertTrue(foundAmazonFixity);
        Assert.assertTrue(foundRepOnDemand);
    }

    private void verifyHello() {
        Assert.assertTrue("I need an implementation", true);
    }

    private void verifyDuplication(ServiceInfo serviceInfo) {
        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        Assert.assertNotNull(systemConfigs);
        Assert.assertEquals(7, systemConfigs.size());

        verifyDurastoreCredential(systemConfigs);
    }

    private void verifyImagemagick(ServiceInfo serviceInfo) {
        Assert.assertTrue(serviceInfo.isSystemService());
        Map<String, String> dependencies = serviceInfo.getDependencies();
        Assert.assertNull(dependencies);
    }

    private void verifyWebapputil(ServiceInfo serviceInfo) {
        Assert.assertTrue(serviceInfo.isSystemService());
        Map<String, String> dependencies = serviceInfo.getDependencies();
        Assert.assertNull(dependencies);
    }

    private void verifyHellowebappwrapper(ServiceInfo serviceInfo) {
        Assert.assertTrue("I need an implementation", true);
    }

    private void verifyJ2k(ServiceInfo serviceInfo) {
        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        Assert.assertNotNull(systemConfigs);
        Assert.assertEquals(5, systemConfigs.size());

        verifyDurastoreCredential(systemConfigs);

        String ver = getVersion();
        String dependencyServiceId = "10";
        String dependencyContentId = "webapputilservice-" + ver + ".zip";
        verifyDependencies(serviceInfo,
                           dependencyServiceId,
                           dependencyContentId);
    }

    private void verifyImageconversion(ServiceInfo serviceInfo) {
        int numUserConfigs = 6;
        int numSystemConfigs = 7;
        verifyServiceInfo(numUserConfigs, numSystemConfigs, serviceInfo);

        String ver = getVersion();
        String dependencyServiceId = "9";
        String dependencyContentId = "imagemagickservice-" + ver + ".zip";
        verifyDependencies(serviceInfo,
                           dependencyServiceId,
                           dependencyContentId);
    }

    private void verifyDependencies(ServiceInfo serviceInfo,
                                    String dependencyServiceId,
                                    String dependencyContentId) {
        Assert.assertFalse(serviceInfo.isSystemService());

        Map<String, String> dependencies = serviceInfo.getDependencies();
        Assert.assertNotNull(dependencies);
        Assert.assertEquals(1, dependencies.size());

        Assert.assertTrue(dependencies.containsKey(dependencyServiceId));
        Assert.assertEquals(dependencyContentId, dependencies.get(
            dependencyServiceId));
    }

    private void verifyMediaStreaming(ServiceInfo serviceInfo) {
        int numUserConfigs = 2;
        int numSystemConfigs = 7;
        verifyServiceInfo(numUserConfigs, numSystemConfigs, serviceInfo);
    }

    private void verifyFixity(ServiceInfo serviceInfo) {
        int numUserConfigs = 0;
        int numSystemConfigs = 6;
        verifyServiceInfo(numUserConfigs, numSystemConfigs, serviceInfo);

        List<List<Integer>> setsModesConfigs = new ArrayList<List<Integer>>();
        setsModesConfigs.add(Arrays.asList(0, 1));
        verifyServiceModes(setsModesConfigs, serviceInfo);
    }

    private void verifyFixityTools(ServiceInfo serviceInfo) {
        int numUserConfigs = 0;
        int numSystemConfigs = 6;
        verifyServiceInfo(numUserConfigs, numSystemConfigs, serviceInfo);

        List<List<Integer>> setsModesConfigs = new ArrayList<List<Integer>>();
        setsModesConfigs.add(Arrays.asList(1, 2, 2));
        verifyServiceModes(setsModesConfigs, serviceInfo);
    }

    private void verifyBulkImageconversion(ServiceInfo serviceInfo) {
        int numUserConfigs = 0;
        int numSystemConfigs = 7;
        verifyServiceInfo(numUserConfigs, numSystemConfigs, serviceInfo);

        List<List<Integer>> setsModesConfigs = new ArrayList<List<Integer>>();
        setsModesConfigs.add(Arrays.asList(6,8));
        verifyServiceModes(setsModesConfigs, serviceInfo);
    }

    private void verifyAmazonFixity(ServiceInfo serviceInfo) {
        int numUserConfigs = 0;
        int numSystemConfigs = 7;
        verifyServiceInfo(numUserConfigs, numSystemConfigs, serviceInfo);

        List<List<Integer>> setsModesConfigs = new ArrayList<List<Integer>>();
        setsModesConfigs.add(Arrays.asList(1, 3));
        verifyServiceModes(setsModesConfigs, serviceInfo);
    }

    private void verifyRepOnDemand(ServiceInfo serviceInfo) {
        int numUserConfigs = 0;
        int numSystemConfigs = 7;
        verifyServiceInfo(numUserConfigs, numSystemConfigs, serviceInfo);

        List<List<Integer>> setsModesConfigs = new ArrayList<List<Integer>>();
        setsModesConfigs.add(Arrays.asList(1,3));
        verifyServiceModes(setsModesConfigs, serviceInfo);
    }

    private void verifyCloudSync(ServiceInfo serviceInfo) {
        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        Assert.assertNotNull(systemConfigs);
        Assert.assertEquals(5, systemConfigs.size());

        verifyDurastoreCredential(systemConfigs);

        String ver = getVersion();
        String dependencyServiceId = "10";
        String dependencyContentId = "webapputilservice-" + ver + ".zip";
        verifyDependencies(serviceInfo,
                           dependencyServiceId,
                           dependencyContentId);
    }

    private void verifyServiceInfo(int numUserConfigs,
                                   int numSystemConfigs,
                                   ServiceInfo serviceInfo) {
        List<UserConfigModeSet> userConfigModeSets = serviceInfo.getUserConfigModeSets();
        Assert.assertNotNull(userConfigModeSets);

        UserConfigModeSet userConfigModeSet = userConfigModeSets.get(0);
        if (0 == numUserConfigs) {
            Assert.assertFalse(userConfigModeSet.hasOnlyUserConfigs());
        } else {
            Assert.assertTrue(userConfigModeSet.hasOnlyUserConfigs());
            Assert.assertEquals(1, userConfigModeSets.size());
            Assert.assertEquals(numUserConfigs,
                                userConfigModeSet.wrappedUserConfigs().size());
        }

        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        Assert.assertNotNull(systemConfigs);
        Assert.assertEquals(numSystemConfigs, systemConfigs.size());

        verifyDurastoreCredential(systemConfigs);
    }

    private void verifyServiceModes(List<List<Integer>> setsModesConfigs,
                                    ServiceInfo serviceInfo) {
        List<UserConfigModeSet> modeSets = serviceInfo.getUserConfigModeSets();

        int numModeSets = setsModesConfigs.size();
        if (numModeSets > 0) {
            Assert.assertNotNull(modeSets);
            Assert.assertEquals(numModeSets, modeSets.size());
        } else {
            return;
        }

        for (int i = 0; i < numModeSets; ++i) {
            List<Integer> modesConfigsI = setsModesConfigs.get(i);
            int numModes = modesConfigsI.size();
            if (numModes > 0) {
                UserConfigModeSet modeSet = modeSets.get(i);
                Assert.assertNotNull(modeSet);

                List<UserConfigMode> modes = modeSet.getModes();
                Assert.assertNotNull(modes);
                Assert.assertEquals(numModes, modes.size());
                if (numModes > 0) {
                    for (int j = 0; j < numModes; ++j) {
                        UserConfigMode mode = modes.get(j);
                        Assert.assertNotNull(mode);

                        int numConfigsJ = modesConfigsI.get(j);
                        Assert.assertEquals(numConfigsJ,
                                            mode.getUserConfigs().size());
                    }
                }
            }
        }
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
        String version = getVersion();
        String outputDirPath = getOutputDir().getAbsolutePath();

        // generate the xml files
        ServiceXmlGenerator xmlGenerator = new ServiceXmlGenerator(version);
        xmlGenerator.generateServiceXml(outputDirPath);

        ServiceRegistryName registry = new ServiceRegistryName(version);

        String xmlAllName = registry.getName() + ".xml";
        String xmlProfessionalName = registry.getNameProfessional() + ".xml";
        String xmlPreservationName = registry.getNamePreservation() + ".xml";
        String xmlMediaName = registry.getNameMedia() + ".xml";
        String xmlTrialName = registry.getNameTrial() + ".xml";

        // verify all xml files were created
        File xmlAll = new File(outputDirPath, xmlAllName);
        File xmlProfessional = new File(outputDirPath, xmlProfessionalName);
        File xmlPreservation = new File(outputDirPath, xmlPreservationName);
        File xmlMedia = new File(outputDirPath, xmlMediaName);
        File xmlTrial = new File(outputDirPath, xmlTrialName);

        Assert.assertTrue(xmlAll.getPath(), xmlAll.exists());
        Assert.assertTrue(xmlProfessional.getPath(), xmlProfessional.exists());
        Assert.assertTrue(xmlPreservation.getPath(), xmlPreservation.exists());
        Assert.assertTrue(xmlMedia.getPath(), xmlMedia.exists());
        Assert.assertTrue(xmlTrial.getPath(), xmlTrial.exists());

        // verify each xml files has correct services
        verifyServiceXmlAll(xmlAll);
        verifyServiceXmlProfessional(xmlProfessional);
        verifyServiceXmlPreservation(xmlPreservation);
        verifyServiceXmlMedia(xmlMedia);
        verifyServiceXmlTrial(xmlTrial);
    }

    private void verifyServiceXmlAll(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = NUM_SERVICES;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "fixityservice-", 0);
        verifyService(services.get(1), "amazonfixityservice-", 1);
        verifyService(services.get(2), "bitintegritytoolsservice-", 2);
        verifyService(services.get(3), "replication-on-demand-service-", 3);
        verifyService(services.get(4), "duplicationservice-", 4);
        verifyService(services.get(5), "j2kservice-", 5);
        verifyService(services.get(6), "imageconversionservice-", 6);
        verifyService(services.get(7), "bulkimageconversionservice-", 7);
        verifyService(services.get(8), "mediastreamingservice-", 8);
        verifyService(services.get(9), "imagemagickservice-", 9);
        verifyService(services.get(10), "webapputilservice-", 10);
        verifyService(services.get(11), "cloudsyncservice-", 11);
    }

    private void verifyServiceXmlProfessional(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = 9;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "fixityservice-", 0);
        verifyService(services.get(1), "amazonfixityservice-", 1);
        verifyService(services.get(2), "bitintegritytoolsservice-", 2);
        verifyService(services.get(3), "replication-on-demand-service-", 3);
        verifyService(services.get(4), "duplicationservice-", 4);
        verifyService(services.get(5), "j2kservice-", 5);
//        verifyService(services.get(6), "imageconversionservice-", 6);
//        verifyService(services.get(7), "bulkimageconversionservice-", 7);
        verifyService(services.get(6), "mediastreamingservice-", 8);
        verifyService(services.get(7), "imagemagickservice-", 9);
        verifyService(services.get(8), "webapputilservice-", 10);
    }

    private void verifyServiceXmlPreservation(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = 2;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "fixityservice-", 0);
        verifyService(services.get(1), "bitintegritytoolsservice-", 2);
    }

    private void verifyServiceXmlMedia(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = 4   ;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "j2kservice-", 5);
//        verifyService(services.get(1), "imageconversionservice-", 6);
        verifyService(services.get(1), "mediastreamingservice-", 8);
        verifyService(services.get(2), "imagemagickservice-", 9);
        verifyService(services.get(3), "webapputilservice-", 10);
    }

    private void verifyServiceXmlTrial(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = 7;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "fixityservice-", 0);
        verifyService(services.get(1), "bitintegritytoolsservice-", 2);
        verifyService(services.get(2), "duplicationservice-", 4);
        verifyService(services.get(3), "j2kservice-", 5);
//        verifyService(services.get(4), "imageconversionservice-", 6);
        verifyService(services.get(4), "mediastreamingservice-", 8);
        verifyService(services.get(5), "imagemagickservice-", 9);
        verifyService(services.get(6), "webapputilservice-", 10);
    }

    private void verifyService(ServiceInfo service, String prefix, int id) {
        String serviceContentId = service.getContentId();
        Assert.assertEquals(prefix + getVersion() + ".zip", serviceContentId);
        Assert.assertEquals(serviceContentId, id, service.getId());
    }

    private List<ServiceInfo> getServicesFromXml(File xmlFile)
        throws FileNotFoundException {
        InputStream xmlStream = new FileInputStream(xmlFile);
        List<ServiceInfo> services = ServicesConfigDocument.getServiceList(
            xmlStream);

        IOUtils.closeQuietly(xmlStream);
        return services;
    }

    private File getOutputDir() throws Exception {
        TestConfig config = new TestConfig();
        String targetDir = config.getTargetDir();
        URI targetDirUri = new URI(targetDir);
        return new File(targetDirUri);
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