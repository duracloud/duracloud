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
    private static final int NUM_SERVICES = 3;

    @Test
    public void testBuildServiceList() {
        String ver = getVersion();

        ServiceXmlGenerator serviceXmlGenerator = new ServiceXmlGenerator(ver);
        List<ServiceInfo> serviceInfos = serviceXmlGenerator.getServices();
        Assert.assertNotNull(serviceInfos);

        Assert.assertEquals(NUM_SERVICES, serviceInfos.size());

        boolean foundMediaStreaming = false;
        boolean foundFixity = false;
        boolean foundFixityTools = false;

        for (ServiceInfo serviceInfo : serviceInfos) {
            String contentId = serviceInfo.getContentId();
            Assert.assertNotNull(contentId);

            if (contentId.equals(
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

            } else {
                Assert.fail("unexpected contentId: " + contentId);
            }
        }

        Assert.assertTrue(foundMediaStreaming);
        Assert.assertTrue(foundFixity);
        Assert.assertTrue(foundFixityTools);
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
        int numUserConfigs = 1;
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

    private void verifyServiceInfo(int numUserConfigs,
                                   int numSystemConfigs,
                                   ServiceInfo serviceInfo) {
        List<UserConfigModeSet> userConfigModeSets =
            serviceInfo.getUserConfigModeSets();
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
        String xmlTrialName = registry.getNameTrial() + ".xml";

        // verify all xml files were created
        File xmlAll = new File(outputDirPath, xmlAllName);
        File xmlProfessional = new File(outputDirPath, xmlProfessionalName);
        File xmlTrial = new File(outputDirPath, xmlTrialName);

        Assert.assertTrue(xmlAll.getPath(), xmlAll.exists());
        Assert.assertTrue(xmlProfessional.getPath(), xmlProfessional.exists());
        Assert.assertTrue(xmlTrial.getPath(), xmlTrial.exists());

        // verify each xml files has correct services
        verifyServiceXmlAll(xmlAll);
        verifyServiceXmlProfessional(xmlProfessional);
        verifyServiceXmlTrial(xmlTrial);
    }

    private void verifyServiceXmlAll(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = NUM_SERVICES;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "fixityservice-", 0);
        verifyService(services.get(1), "bitintegritytoolsservice-", 1);
        verifyService(services.get(2), "mediastreamingservice-", 2);
    }

    private void verifyServiceXmlProfessional(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = 3;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "fixityservice-", 0);
        verifyService(services.get(1), "bitintegritytoolsservice-", 1);
        verifyService(services.get(2), "mediastreamingservice-", 2);
    }

    private void verifyServiceXmlTrial(File xmlFile)
        throws FileNotFoundException {
        List<ServiceInfo> services = getServicesFromXml(xmlFile);
        Assert.assertNotNull(services);

        int count = 3;
        Assert.assertEquals(xmlFile.getName(), count, services.size());

        verifyService(services.get(0), "fixityservice-", 0);
        verifyService(services.get(1), "bitintegritytoolsservice-", 1);
        verifyService(services.get(2), "mediastreamingservice-", 2);
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