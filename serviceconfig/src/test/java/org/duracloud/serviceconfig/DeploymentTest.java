/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Andrew Woods
 *         Date: Dec 4, 2010
 */
public class DeploymentTest {

    private Deployment deployment;

    @Before
    public void setUp() {
        deployment = new Deployment();

        deployment.setHostname("hostname");
        deployment.setId(17);
        deployment.setStatus(Deployment.Status.STARTED);
        deployment.setSystemConfigs(createSystemConfigs());
        deployment.setUserConfigModeSets(createUserConfigModeSets());
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
        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setId(new Random().nextInt());

        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(modeSet);
        return modeSets;
    }

    @Test
    public void testClone() throws Exception {
        verify(deployment, deployment, true);

        Deployment clone = deployment.clone();
        verify(deployment, clone, true);
        clone.setHostname("new-host-name");
        verify(deployment, clone, false);

        clone = deployment.clone();
        verify(deployment, clone, true);
        clone.setId(34);
        verify(deployment, clone, false);

        clone = deployment.clone();
        verify(deployment, clone, true);
        clone.setStatus(Deployment.Status.STOPPED);
        verify(deployment, clone, false);

        clone = deployment.clone();
        verify(deployment, clone, true);
        clone.setSystemConfigs(createSystemConfigs());
        verify(deployment, clone, false);

        clone = deployment.clone();
        verify(deployment, clone, true);
        clone.setUserConfigModeSets(createUserConfigModeSets());
        verify(deployment, clone, false);
    }

    private void verify(Deployment source, Deployment clone, boolean valid) {
        boolean isValid = false;
        try {
            Assert.assertEquals(source.getHostname(), clone.getHostname());
            Assert.assertEquals(source.getId(), clone.getId());
            Assert.assertEquals(source.getStatus(), clone.getStatus());
            Assert.assertEquals(source.getSystemConfigs(),
                                clone.getSystemConfigs());
            Assert.assertEquals(source.getUserConfigModeSets(),
                                clone.getUserConfigModeSets());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(t.getMessage(), valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }
}
