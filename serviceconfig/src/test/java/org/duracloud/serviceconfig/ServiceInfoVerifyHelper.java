/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import java.util.List;

import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.junit.Assert;

/**
 * This class helps other unit tests check an expected ServiceInfo object
 * against a provided one.
 *
 * @author Andrew Woods
 *         Date: Aug 26, 2010
 */
public class ServiceInfoVerifyHelper {

    private ServiceInfo expected;

    public ServiceInfoVerifyHelper(ServiceInfo expected) {
        this.expected = expected;
    }

    public void verify(ServiceInfo serviceInfo) {
        Assert.assertNotNull(expected);
        Assert.assertNotNull(serviceInfo);

        Assert.assertTrue(expected.getId() >= 0);
        if (null != expected.getContentId() &&
            null != serviceInfo.getContentId()) {
            Assert.assertEquals(expected.getContentId(),
                                serviceInfo.getContentId());
        }

        if (null != expected.getDisplayName() &&
            null != serviceInfo.getDisplayName()) {
            Assert.assertEquals(expected.getDisplayName(),
                                serviceInfo.getDisplayName());
        }

        if (null != expected.getServiceVersion() &&
            null != serviceInfo.getServiceVersion()) {
            Assert.assertEquals(expected.getServiceVersion(),
                                serviceInfo.getServiceVersion());
        }

        if (null != expected.getUserConfigVersion() &&
            null != serviceInfo.getUserConfigVersion()) {
            Assert.assertEquals(expected.getUserConfigVersion(),
                                serviceInfo.getUserConfigVersion());
        }

        if (null != expected.getDescription() &&
            null != serviceInfo.getDescription()) {
            Assert.assertEquals(expected.getDescription(),
                                serviceInfo.getDescription());
        }

        Assert.assertTrue(expected.getMaxDeploymentsAllowed() >= -1);

        Assert.assertEquals(expected.getId(), serviceInfo.getId());
        Assert.assertEquals(expected.getContentId(),
                            serviceInfo.getContentId());
        Assert.assertEquals(expected.getDisplayName(),
                            serviceInfo.getDisplayName());
        Assert.assertEquals(expected.getServiceVersion(),
                            serviceInfo.getServiceVersion());
        Assert.assertEquals(expected.getUserConfigVersion(),
                            serviceInfo.getUserConfigVersion());
        Assert.assertEquals(expected.getDescription(),
                            serviceInfo.getDescription());
        Assert.assertEquals(expected.getMaxDeploymentsAllowed(),
                            serviceInfo.getMaxDeploymentsAllowed());

        verifyEqual(expected.getSystemConfigs(),
                    serviceInfo.getSystemConfigs());
        verifyEqual(expected.getUserConfigModeSets(), serviceInfo.getUserConfigModeSets());
        verifyUserConfigModeSetsEqual(expected.getUserConfigModeSets(),
                                      serviceInfo.getUserConfigModeSets());
        verifyEqual(expected.getDeploymentOptions(),
                    serviceInfo.getDeploymentOptions());
        verifyDeploymentsEqual(expected.getDeployments(),
                               serviceInfo.getDeployments());

    }

    private void verifyEqual(List expected, List list) {
        if (null == expected && null == list) {
            return;
        }

        Assert.assertNotNull(expected);
        Assert.assertNotNull(list);
        Assert.assertEquals(expected.size(), list.size());

        for (Object expectedElem : expected) {
            boolean found = false;
            for (Object obj : list) {
                if (expectedElem.equals(obj)) {
                    found = true;
                }
            }
            Assert.assertTrue(expectedElem.toString(), found);
        }
    }

    private void verifyUserConfigModeSetsEqual(List<UserConfigModeSet> expectedModeSets,
                                               List<UserConfigModeSet> modeSets) {
        if (null == expectedModeSets && null == modeSets) {
            return;
        }

        Assert.assertNotNull(expectedModeSets);
        Assert.assertNotNull(modeSets);

        Assert.assertTrue(expectedModeSets.size() > 0);
        Assert.assertEquals(expectedModeSets.size(), modeSets.size());

        int numFound = 0;
        for (UserConfigModeSet expectedModeSet : expectedModeSets) {
            boolean found = false;
            int expectedId = expectedModeSet.getId();
            for (UserConfigModeSet modeSet : modeSets) {
                if (expectedId == modeSet.getId()) {
                    verifyUserConfigModeSetEqual(expectedModeSet, modeSet);
                    numFound++;
                    found = true;
                }
            }
            Assert.assertTrue("id: " + expectedModeSet.getId(), found);
        }

        Assert.assertEquals(expectedModeSets.size(), numFound);
    }

    private void verifyUserConfigModeSetEqual(UserConfigModeSet expectedModeSet,
                                              UserConfigModeSet modeSet) {
        Assert.assertNotNull(expectedModeSet);
        Assert.assertNotNull(modeSet);

        Assert.assertEquals(expectedModeSet.getId(), modeSet.getId());

        List<UserConfigMode> expectedModes = expectedModeSet.getModes();
        List<UserConfigMode> modes = modeSet.getModes();

        Assert.assertNotNull(expectedModes);
        Assert.assertNotNull(modes);

        Assert.assertEquals(expectedModes.size(), modes.size());

        int numFound = 0;
        for (UserConfigMode expectedMode : expectedModeSet.getModes()) {
            List<UserConfigModeSet> expectedSubModeSets = expectedMode.getUserConfigModeSets();
            List<UserConfig> expectedSubUserConfigs = expectedMode.getUserConfigs();

            for (UserConfigMode mode : modeSet.getModes()) {
                List<UserConfigModeSet> subModeSets = mode.getUserConfigModeSets();
                List<UserConfig> subUserConfigs = mode.getUserConfigs();

                try {
                    verifyUserConfigModeSetsEqual(expectedSubModeSets,
                                                  subModeSets);
                    verifyUserConfigsEqual(expectedSubUserConfigs,
                                           subUserConfigs);
                    numFound++;
                } catch (Throwable t) {
                    // do nothing
                }
            }
        }
        Assert.assertEquals(expectedModes.size(), numFound);
    }

    private void verifyUserConfigsEqual(List<UserConfig> expectedUserConfigs,
                                        List<UserConfig> userConfigs) {
        if (null == expectedUserConfigs && null == userConfigs) {
            return;
        }

        Assert.assertNotNull(expectedUserConfigs);
        Assert.assertNotNull(userConfigs);
        Assert.assertEquals(expectedUserConfigs.size(), userConfigs.size());

        int numFound = 0;
        for (UserConfig expectedConfig : expectedUserConfigs) {
            boolean found = false;
            for (UserConfig config : userConfigs) {
                if (expectedConfig.equals(config)) {
                    numFound++;
                    found = true;
                }
            }
            Assert.assertTrue(expectedConfig.toString(), found);
        }
        Assert.assertEquals(expectedUserConfigs.size(), numFound);
    }

    private void verifyDeploymentsEqual(List<Deployment> expected,
                                        List<Deployment> deployments) {
        if (null == expected && null == deployments) {
            return;
        }

        Assert.assertNotNull(expected);
        Assert.assertNotNull(deployments);
        Assert.assertEquals(expected.size(), deployments.size());

        for (Deployment expectedElem : expected) {
            boolean found = false;
            int expectedId = expectedElem.getId();
            for (Deployment deployment : deployments) {
                if (expectedId == deployment.getId()) {
                    found = true;
                    Assert.assertNotNull(expectedElem.getHostname());
                    Assert.assertNotNull(expectedElem.getStatus());

                    Assert.assertEquals(expectedElem.getHostname(),
                                        deployment.getHostname());
                    Assert.assertEquals(expectedElem.getStatus(),
                                        deployment.getStatus());

                    verifyEqual(expectedElem.getSystemConfigs(),
                                deployment.getSystemConfigs());
                    verifyEqual(expectedElem.getUserConfigModeSets(),
                                deployment.getUserConfigModeSets());
                }
            }
            Assert.assertTrue("Id: " + expectedElem.getId(), found);
        }

    }
}
