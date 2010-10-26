package org.duracloud.duraservice.config;

import java.util.List;

import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class FixityServiceXmlTest {

    private int index = 5;
    private String version = "1.2.3";

    @Test
    public void testGetServiceXml() throws Exception {
        FixityServiceInfo xml = new FixityServiceInfo();
        ServiceInfo serviceInfo = xml.getServiceXml(index, version);
        Assert.assertNotNull(serviceInfo);

        int deploymentCount = serviceInfo.getDeploymentCount();
        int id = serviceInfo.getId();
        int maxDeploymentsAllowed = serviceInfo.getMaxDeploymentsAllowed();

        Assert.assertEquals(0, deploymentCount);
        Assert.assertEquals(index, id);
        Assert.assertEquals(1, maxDeploymentsAllowed);


        String contentId = serviceInfo.getContentId();
        String displayName = serviceInfo.getDisplayName();
        String serviceVersion = serviceInfo.getServiceVersion();
        String userConfigVersion = serviceInfo.getUserConfigVersion();
        String description = serviceInfo.getDescription();

        Assert.assertNotNull(contentId);
        Assert.assertNotNull(displayName);
        Assert.assertNotNull(serviceVersion);
        Assert.assertNotNull(userConfigVersion);
        Assert.assertNotNull(description);

        Assert.assertEquals("fixityservice-" + version + ".zip", contentId);
        Assert.assertEquals("Bit Integrity Checker", displayName);
        Assert.assertEquals(version, serviceVersion);
        Assert.assertEquals("1.0", userConfigVersion);
        Assert.assertTrue(description.length() > 20);


        List<DeploymentOption> deploymentOptions = serviceInfo.getDeploymentOptions();
        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        List<UserConfig> userConfigs = serviceInfo.getUserConfigs();
        List<Deployment> deployments = serviceInfo.getDeployments();

        Assert.assertNotNull(deploymentOptions);
        Assert.assertNotNull(systemConfigs);
        Assert.assertNotNull(userConfigs);
        Assert.assertNull(deployments);

        Assert.assertEquals(3, deploymentOptions.size());
        Assert.assertEquals(5, systemConfigs.size());
        Assert.assertEquals(12, userConfigs.size());

    }
}
