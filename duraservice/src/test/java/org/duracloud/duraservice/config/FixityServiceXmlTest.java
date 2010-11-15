package org.duracloud.duraservice.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class FixityServiceXmlTest {

    private int index = 5;
    private String version = "1.2.3";

    private static Map<FixityServiceInfo.ModeType, Integer> modeToUserConfig;
    private static Map<FixityServiceInfo.ModeType, Integer> modeToSpaceConfigs;

    @BeforeClass
    public static void beforeClass() {
        modeToUserConfig = new HashMap<FixityServiceInfo.ModeType, Integer>();
        modeToUserConfig.put(FixityServiceInfo.ModeType.ALL_IN_ONE_LIST, 4);
        modeToUserConfig.put(FixityServiceInfo.ModeType.ALL_IN_ONE_SPACE, 4);
        modeToUserConfig.put(FixityServiceInfo.ModeType.GENERATE_LIST, 3);
        modeToUserConfig.put(FixityServiceInfo.ModeType.GENERATE_SPACE, 2);
        modeToUserConfig.put(FixityServiceInfo.ModeType.COMPARE, 3);

        modeToSpaceConfigs = new HashMap<FixityServiceInfo.ModeType, Integer>();
        modeToSpaceConfigs.put(FixityServiceInfo.ModeType.ALL_IN_ONE_LIST, 2);
        modeToSpaceConfigs.put(FixityServiceInfo.ModeType.ALL_IN_ONE_SPACE, 3);
        modeToSpaceConfigs.put(FixityServiceInfo.ModeType.GENERATE_LIST, 2);
        modeToSpaceConfigs.put(FixityServiceInfo.ModeType.GENERATE_SPACE, 2);
        modeToSpaceConfigs.put(FixityServiceInfo.ModeType.COMPARE, 3);
    }

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
        List<UserConfigModeSet> modeSets = serviceInfo.getModeSets();
        List<Deployment> deployments = serviceInfo.getDeployments();

        Assert.assertNotNull(deploymentOptions);
        Assert.assertNotNull(systemConfigs);
        Assert.assertNotNull(userConfigs);
        Assert.assertNotNull(modeSets);
        Assert.assertNull(deployments);

        Assert.assertEquals(3, deploymentOptions.size());
        Assert.assertEquals(5, systemConfigs.size());
        Assert.assertEquals(1, modeSets.size());
        Assert.assertEquals(0, userConfigs.size());


        List<UserConfigMode> modes = modeSets.get(0).getModes();
        verifyModes(modes);
    }

    private void verifyModes(List<UserConfigMode> modes) {
        Assert.assertNotNull(modes);
        Assert.assertEquals(5, modes.size());

        verifyMode(modes, FixityServiceInfo.ModeType.ALL_IN_ONE_LIST);
        verifyMode(modes, FixityServiceInfo.ModeType.ALL_IN_ONE_SPACE);
        verifyMode(modes, FixityServiceInfo.ModeType.GENERATE_LIST);
        verifyMode(modes, FixityServiceInfo.ModeType.GENERATE_SPACE);
        verifyMode(modes, FixityServiceInfo.ModeType.COMPARE);
    }

    private void verifyMode(List<UserConfigMode> modes,
                            FixityServiceInfo.ModeType modeType) {
        UserConfigMode mode = getMode(modeType, modes);
        Assert.assertNotNull(mode);

        Assert.assertEquals(modeType.getKey(), mode.getName());
        Assert.assertEquals(modeType.getDesc(), mode.getDisplayName());

        List<UserConfig> userConfigs = mode.getUserConfigs();
        Assert.assertNotNull(userConfigs);
        int numUserConfig = modeToUserConfig.get(modeType);
        Assert.assertEquals(modeType.name(), numUserConfig, userConfigs.size());

        List<UserConfigModeSet> modeSets = mode.getUserConfigModeSets();
        List<UserConfigMode> spaceModes = modeSets.get(0).getModes();
        Assert.assertNotNull(spaceModes);

        Assert.assertEquals(1, spaceModes.size());
        List<UserConfig> spaceConfigs = spaceModes.get(0).getUserConfigs();
        Assert.assertNotNull(spaceConfigs);

        int numSpaceCfgs = modeToSpaceConfigs.get(modeType);
        Assert.assertEquals(modeType.name(), numSpaceCfgs, spaceConfigs.size());
    }

    private UserConfigMode getMode(FixityServiceInfo.ModeType modeType,
                                   List<UserConfigMode> modes) {
        for (UserConfigMode mode : modes) {
            if (mode.getName().equals(modeType.getKey())) {
                return mode;
            }
        }
        Assert.fail("Mode not found: " + modeType);
        return null;
    }
}
