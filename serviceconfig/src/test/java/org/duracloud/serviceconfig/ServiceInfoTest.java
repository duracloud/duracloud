/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Andrew Woods
 *         Date: Dec 4, 2010
 */
public class ServiceInfoTest {

    private ServiceInfo serviceInfo;

    private String contentId = "contentId";
    private List<DeploymentOption> deploymentOptions = createDeploymentOptions();
    private List<Deployment> deployments = createDeployments();
    private String description = "description";
    private String displayName = "displayName";
    private int id = 17;
    private int maxDeploymentsAllowed = 18;
    private String serviceVersion = "1.2.3";
    private List<SystemConfig> systemConfigs = createSystemConfigs();
    private List<UserConfigModeSet> userConfigModeSets = createUserConfigModeSets();
    private String userConfigVersion = "4.5.6";

    @Before
    public void setUp() {
        serviceInfo = new ServiceInfo();
        serviceInfo.setContentId(contentId);
        serviceInfo.setDeploymentOptions(deploymentOptions);
        serviceInfo.setDeployments(deployments);
        serviceInfo.setDescription(description);
        serviceInfo.setDisplayName(displayName);
        serviceInfo.setId(id);
        serviceInfo.setMaxDeploymentsAllowed(maxDeploymentsAllowed);
        serviceInfo.setServiceVersion(serviceVersion);
        serviceInfo.setSystemConfigs(systemConfigs);
        serviceInfo.setUserConfigModeSets(userConfigModeSets);
        serviceInfo.setUserConfigVersion(userConfigVersion);

    }


    private List<DeploymentOption> createDeploymentOptions() {
        DeploymentOption option = new DeploymentOption();
        option.setDisplayName("display-name:" + new Random().nextInt());

        List<DeploymentOption> options = new ArrayList<DeploymentOption>();
        options.add(option);
        return options;
    }

    private List<Deployment> createDeployments() {
        Deployment deployment = new Deployment();
        deployment.setId(new Random().nextInt());

        List<Deployment> deployments = new ArrayList<Deployment>();
        deployments.add(deployment);
        return deployments;
    }

    private List<SystemConfig> createSystemConfigs() {
        int num = new Random().nextInt();
        SystemConfig config = new SystemConfig("name:" + num,
                                               "value:" + num,
                                               "defaultValue:" + num);

        List<SystemConfig> configs = new ArrayList<SystemConfig>();
        configs.add(config);
        return configs;
    }

    private List<UserConfigModeSet> createUserConfigModeSets() {
        int num = new Random().nextInt();
        UserConfig userConfig = new TextUserConfig("name:" + num,
                                                   "display-name:" + num);
        List<UserConfig> userConfigs = Arrays.asList(userConfig);
        return Arrays.asList(new UserConfigModeSet(userConfigs));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {

        verify(serviceInfo, serviceInfo, true);

        ServiceInfo clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setContentId("new-content-id");
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setDeploymentOptions(createDeploymentOptions());
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setDeployments(createDeployments());
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setDescription("new-description");
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setDisplayName("new-display-name");
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setId(34);
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setMaxDeploymentsAllowed(34);
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setServiceVersion("6.5.4");
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setSystemConfigs(createSystemConfigs());
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setUserConfigModeSets(createUserConfigModeSets());
        verify(serviceInfo, clone, false);

        clone = serviceInfo.clone();
        verify(serviceInfo, clone, true);
        clone.setUserConfigVersion("9.8.7");
        verify(serviceInfo, clone, false);
    }

    private void verify(ServiceInfo source, ServiceInfo clone, boolean valid) {
        boolean isValid = false;
        try {
            Assert.assertEquals(source.getContentId(), clone.getContentId());
            Assert.assertEquals(source.getDeploymentOptions(),
                                clone.getDeploymentOptions());
            Assert.assertEquals(source.getDeploymentCount(),
                                clone.getDeploymentCount());
            Assert.assertEquals(source.getDeployments(),
                                clone.getDeployments());
            Assert.assertEquals(source.getDescription(),
                                clone.getDescription());
            Assert.assertEquals(source.getDisplayName(),
                                clone.getDisplayName());
            Assert.assertEquals(source.getId(), clone.getId());
            Assert.assertEquals(source.getMaxDeploymentsAllowed(),
                                clone.getMaxDeploymentsAllowed());
            Assert.assertEquals(source.getServiceVersion(),
                                clone.getServiceVersion());
            Assert.assertEquals(source.getSystemConfigs(),
                                clone.getSystemConfigs());
            Assert.assertEquals(source.getUserConfigModeSets(),
                                clone.getUserConfigModeSets());
            Assert.assertEquals(source.getUserConfigVersion(),
                                clone.getUserConfigVersion());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(t.getMessage(), valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }

}
