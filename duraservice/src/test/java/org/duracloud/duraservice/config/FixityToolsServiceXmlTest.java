package org.duracloud.duraservice.config;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Andrew Woods
 *         Date: Dec 3, 2010
 */
public class FixityToolsServiceXmlTest {

    private int index = 5;
    private String version = "1.2.3";

    private static Map<FixityToolsServiceInfo.ModeType, Integer> modeToUserConfig;
    private static Map<FixityToolsServiceInfo.ModeType, Integer> modeToSpaceConfigs;

    @BeforeClass
    public static void beforeClass() {
        modeToUserConfig = new HashMap<FixityToolsServiceInfo.ModeType, Integer>();
        modeToUserConfig.put(FixityToolsServiceInfo.ModeType.GENERATE_SPACE, 1);
        modeToUserConfig.put(FixityToolsServiceInfo.ModeType.GENERATE_LIST, 2);
        modeToUserConfig.put(FixityToolsServiceInfo.ModeType.COMPARE, 2);

        modeToSpaceConfigs = new HashMap<FixityToolsServiceInfo.ModeType, Integer>();
        modeToSpaceConfigs.put(FixityToolsServiceInfo.ModeType.GENERATE_SPACE,
                               1);
        modeToSpaceConfigs.put(FixityToolsServiceInfo.ModeType.GENERATE_LIST,
                               1);
        modeToSpaceConfigs.put(FixityToolsServiceInfo.ModeType.COMPARE, 2);
    }

    @Test
    public void testGetServiceXml() throws Exception {
        FixityToolsServiceInfo xml = new FixityToolsServiceInfo();
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

        Assert.assertEquals("bitintegritytoolsservice-" + version + ".zip",
                            contentId);
        Assert.assertEquals("Bit Integrity Checker - Tools", displayName);
        Assert.assertEquals(version, serviceVersion);
        Assert.assertEquals("1.0", userConfigVersion);
        Assert.assertTrue(description.length() > 20);


        List<DeploymentOption> deploymentOptions = serviceInfo.getDeploymentOptions();
        List<SystemConfig> systemConfigs = serviceInfo.getSystemConfigs();
        List<UserConfigModeSet> modeSets = serviceInfo.getUserConfigModeSets();
        List<Deployment> deployments = serviceInfo.getDeployments();

        Assert.assertNotNull(deploymentOptions);
        Assert.assertNotNull(systemConfigs);
        Assert.assertNotNull(modeSets);
        Assert.assertNull(deployments);

        Assert.assertEquals(3, deploymentOptions.size());
        Assert.assertEquals(6, systemConfigs.size());
        Assert.assertEquals(1, modeSets.size());


        List<UserConfigMode> modes = modeSets.get(0).getModes();
        verifyModes(modes);
    }

    private void verifyModes(List<UserConfigMode> modes) {
        Assert.assertNotNull(modes);
        Assert.assertEquals(3, modes.size());

        verifyMode(modes, FixityToolsServiceInfo.ModeType.GENERATE_LIST);
        verifyMode(modes, FixityToolsServiceInfo.ModeType.GENERATE_SPACE);
        verifyMode(modes, FixityToolsServiceInfo.ModeType.COMPARE);
    }

    private void verifyMode(List<UserConfigMode> modes,
                            FixityToolsServiceInfo.ModeType modeType) {
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

    private UserConfigMode getMode(FixityToolsServiceInfo.ModeType modeType,
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
