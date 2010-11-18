/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Nov 18, 2009
 */
public class ServicesConfigDocumentTest {
    private int COUNT = 4;
    private int MODE_COUNT = 2;
    private int id = 100;
    private String contentId = "contentId-";
    private String displayName = "displayName-";
    private String serviceVersion = "serviceVersion-";
    private String userConfigVersion = "userConfigVersion-";
    private String description = "description-";
    private int maxDeploymentsAllowed = -2;
    private String systemConfigName = "systemConfigName-";
    private String systemConfigValue = "systemConfigValue-";
    private String systemConfigDefaultName = "systemConfigDefaultName-";
    private String userConfigName = "userConfigName-";
    private String userConfigDisplayName = "userConfigDisplayName-";
    private String userConfigValue = "userConfigValue-";
    private String userConfigExclusion = "userConfigExclusion-";
    private String optionDisplayName = "optionDisplayName-";
    private String optionValue = "optionValue-";
    private String deploymentOptionDisplayName = "deploymentOptionDisplayName-";
    private String deploymentOptionHostname = "deploymentOptionHostname-";
    private String deploymentHostname = "deploymentHostname-";


    private ServiceInfo createService(int tag) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(id + tag);
        serviceInfo.setContentId(contentId + tag);
        serviceInfo.setDisplayName(displayName + tag);
        serviceInfo.setServiceVersion(serviceVersion + tag);
        serviceInfo.setUserConfigVersion(userConfigVersion + tag);
        serviceInfo.setDescription(description + tag);
        serviceInfo.setMaxDeploymentsAllowed(maxDeploymentsAllowed + tag);

        serviceInfo.setSystemConfigs(createSystemConfigs(tag, COUNT));
        serviceInfo.setUserConfigs(createUserConfigs(tag, COUNT));
        serviceInfo.setModeSets(createUserConfigModeSets(tag));
        serviceInfo.setDeploymentOptions(createDeploymentOptions(tag, COUNT));
        serviceInfo.setDeployments(createDeployments(tag, COUNT));

        return serviceInfo;
    }

    private List<SystemConfig> createSystemConfigs(int tag, int count) {
        List<SystemConfig> systemConfigs = new ArrayList<SystemConfig>();
        for (int i = 0; i < count; ++i) {
            int newTag = tag + i;
            SystemConfig systemConfig = new SystemConfig(systemConfigName + newTag,
                                                         systemConfigValue +
                                                             newTag,
                                                         systemConfigDefaultName +
                                                             newTag);
            systemConfigs.add(systemConfig);
        }
        return systemConfigs;
    }

    private List<UserConfig> createUserConfigs(int tag, int count) {
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        for (int i = 0; i < count; ++i) {
            int newTag = tag + i;
            userConfigs.add(createUserConfig(newTag, count));
        }
        return userConfigs;
    }

    private UserConfig createUserConfig(int tag, int count) {
        switch (tag % 3) {
            case 0:
                return new TextUserConfig(userConfigName + tag,
                                          userConfigDisplayName + tag,
                                          userConfigValue + tag,
                                          userConfigExclusion + tag);
            case 1:
                return new SingleSelectUserConfig(userConfigName + tag,
                                                  userConfigDisplayName + tag,
                                                  createUserConfigOptions(tag,
                                                                          count),
                                                  userConfigExclusion + tag);
            case 2:
                return new MultiSelectUserConfig(userConfigName + tag,
                                                 userConfigDisplayName + tag,
                                                 createUserConfigOptions(tag,
                                                                         count),
                                                 userConfigExclusion + tag);
            default:
                throw new RuntimeException("Impossible.");
        }
    }

    private List<Option> createUserConfigOptions(int tag, int count) {
        List<Option> options = new ArrayList<Option>();
        for (int i = 0; i < count; ++i) {
            int newTag = tag + i;
            Option option = new Option(optionDisplayName + newTag,
                                       optionValue + newTag,
                                       i == 0);
            options.add(option);
        }
        return options;
    }

    private List<UserConfigModeSet> createUserConfigModeSets(int tag) {
        List<UserConfigModeSet> userConfigModeSets = new ArrayList<UserConfigModeSet>();
        for (int i = 0; i < MODE_COUNT; ++i) {
            UserConfigModeSet modeSet = new UserConfigModeSet();
            modeSet.setId(20 + i);
            modeSet.setModes(createUserConfigModes(tag));

            userConfigModeSets.add(modeSet);
        }
        return userConfigModeSets;
    }

    private List<UserConfigMode> createUserConfigModes(int tag) {
        List<UserConfigMode> userConfigModes = new ArrayList<UserConfigMode>();

        for (int i = 0; i < MODE_COUNT; ++i) {
            UserConfigMode mode = new UserConfigMode();

            mode.setUserConfigs(createUserConfigs(tag + i, COUNT));
            mode.setUserConfigModeSets(null);

            userConfigModes.add(mode);
        }

        return userConfigModes;
    }

    private List<DeploymentOption> createDeploymentOptions(int tag, int count) {
        List<DeploymentOption> deploymentOptions = new ArrayList<DeploymentOption>();
        for (int i = 0; i < count; ++i) {
            int newTag = tag + i;
            DeploymentOption deploymentOption = new DeploymentOption();
            deploymentOption.setDisplayName(
                deploymentOptionDisplayName + newTag);
            deploymentOption.setLocation(locationType(newTag));
            deploymentOption.setHostname(deploymentOptionHostname + newTag);
            deploymentOption.setState(state(newTag));
            deploymentOptions.add(deploymentOption);
        }
        return deploymentOptions;
    }

    private DeploymentOption.Location locationType(int tag) {
        switch (tag % 3) {
            case 0:
                return DeploymentOption.Location.EXISTING;
            case 1:
                return DeploymentOption.Location.NEW;
            case 2:
                return DeploymentOption.Location.PRIMARY;
            default:
                throw new RuntimeException("Impossible");
        }

    }

    private DeploymentOption.State state(int tag) {
        switch (tag % 2) {
            case 0:
                return DeploymentOption.State.AVAILABLE;
            case 1:
                return DeploymentOption.State.UNAVAILABLE;
            default:
                throw new RuntimeException("Impossible");
        }

    }

    private List<Deployment> createDeployments(int tag, int count) {
        List<Deployment> deployments = new ArrayList<Deployment>();
        for (int i = 0; i < count; ++i) {
            int newTag = tag + i;
            Deployment deployment = new Deployment();
            deployment.setId(newTag);
            deployment.setHostname(deploymentHostname + newTag);
            deployment.setStatus(status(newTag));
            deployment.setSystemConfigs(createSystemConfigs(newTag, count));
            deployment.setUserConfigs(createUserConfigs(newTag, count));
            deployments.add(deployment);
        }
        return deployments;
    }

    private Deployment.Status status(int tag) {
        switch (tag % 2) {
            case 0:
                return Deployment.Status.STARTED;
            case 1:
                return Deployment.Status.STOPPED;
            default:
                throw new RuntimeException("Impossible");
        }

    }

    @Test
    public void testService() {
        int tag = 1;
        ServiceInfo expected = createService(tag);
        verifyService(expected, tag);

        String xml = ServicesConfigDocument.getServiceAsXML(expected);
        verifyServiceXML(xml, tag);

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        ServiceInfo serviceInfo = ServicesConfigDocument.getService(inputStream);
        ServiceInfoVerifyHelper verifier = new ServiceInfoVerifyHelper(
            serviceInfo);
        verifier.verify(serviceInfo);

        String newXml = ServicesConfigDocument.getServiceAsXML(serviceInfo);
        Assert.assertEquals(xml, newXml);
        System.out.println(xml);
    }

    @Test
    public void testServiceList() {
        List<ServiceInfo> expectedServices = new ArrayList<ServiceInfo>();
        List<Integer> tags = new ArrayList<Integer>();
        for (int tag = 10; tag < 1000; tag *= 10) {
            expectedServices.add(createService(tag));
            tags.add(tag);
        }

        verifyServiceList(expectedServices, tags);

        String xml = ServicesConfigDocument.getServiceListAsXML(expectedServices);
        verifyServiceListXML(xml, tags);

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        List<ServiceInfo> services = ServicesConfigDocument.getServiceList(
            inputStream);
        verifyServiceListsEqual(expectedServices, services);

        String newXml = ServicesConfigDocument.getServiceListAsXML(services);
        Assert.assertEquals(xml, newXml);
    }

    private void verifyServiceList(List<ServiceInfo> expectedServices,
                                   List<Integer> tags) {
        Assert.assertNotNull(expectedServices);
        Assert.assertNotNull(tags);
        Assert.assertTrue(expectedServices.size() > 0);
        Assert.assertEquals(expectedServices.size(), tags.size());

        Iterator<ServiceInfo> servicesItr = expectedServices.iterator();
        for (int tag : tags) {
            verifyService(servicesItr.next(), tag);
        }
    }

    private void verifyService(ServiceInfo serviceInfo, int tag) {
        ServiceInfo expected = createService(tag);

        ServiceInfoVerifyHelper verifier = new ServiceInfoVerifyHelper(expected);
        verifier.verify(serviceInfo);
    }

    private void verifyServiceListsEqual(List<ServiceInfo> expectedServices,
                                         List<ServiceInfo> services) {
        Assert.assertNotNull(expectedServices);
        Assert.assertNotNull(services);
        Assert.assertEquals(expectedServices.size(), services.size());

        Iterator<ServiceInfo> expectedServicesItr = expectedServices.iterator();
        Iterator<ServiceInfo> servicesItr = services.iterator();
        while (expectedServicesItr.hasNext() && servicesItr.hasNext()) {
            ServiceInfoVerifyHelper verifier = new ServiceInfoVerifyHelper(
                expectedServicesItr.next());
            verifier.verify(servicesItr.next());
        }
    }

    private void verifyServiceListXML(String xml, List<Integer> tags) {
        for (int tag : tags) {
            verifyServiceXML(xml, tag);
        }
    }

    private void verifyServiceXML(String xml, int tag) {
        Assert.assertNotNull(xml);
        Assert.assertTrue(xml, xml.contains(contentId + tag));
        Assert.assertTrue(xml, xml.contains(displayName + tag));
        Assert.assertTrue(xml, xml.contains(serviceVersion + tag));
        Assert.assertTrue(xml, xml.contains(userConfigVersion + tag));
        Assert.assertTrue(xml, xml.contains(description + tag));
        Assert.assertTrue(xml, xml.contains(systemConfigName + tag));
        Assert.assertTrue(xml, xml.contains(systemConfigValue + tag));
        Assert.assertTrue(xml, xml.contains(systemConfigDefaultName + tag));
        Assert.assertTrue(xml, xml.contains(userConfigName + tag));
        Assert.assertTrue(xml, xml.contains(userConfigDisplayName + tag));
        // +2 due to switch/case test option creation (see: createUserConfig())
        Assert.assertTrue(xml, xml.contains(userConfigValue + (tag + 2)));
        // +1 due to switch/case test option creation (see: createUserConfig())
        Assert.assertTrue(xml, xml.contains(optionDisplayName + (tag + 1)));
        Assert.assertTrue(xml, xml.contains(optionValue + (tag + 1)));
        Assert.assertTrue(xml, xml.contains(deploymentOptionDisplayName + tag));
        Assert.assertTrue(xml, xml.contains(deploymentOptionHostname + tag));
        Assert.assertTrue(xml, xml.contains(deploymentHostname + tag));

    }

}
