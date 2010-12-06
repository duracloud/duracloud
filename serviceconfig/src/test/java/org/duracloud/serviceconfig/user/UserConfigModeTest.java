/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.SystemConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Andrew Woods
 *         Date: Dec 5, 2010
 */
public class UserConfigModeTest {

    private UserConfigMode mode;

    @Before
    public void setUp() {
        mode = new UserConfigMode();

        mode.setDisplayName("display-name");
        mode.setName("name");
        mode.setSelected(true);
        mode.setUserConfigModeSets(createUserConfigModeSets());
        mode.setUserConfigs(createUserConfigs());
    }

    private List<UserConfigModeSet> createUserConfigModeSets() {
        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setId(new Random().nextInt());
        modeSets.add(modeSet);
        return modeSets;
    }

    private List<UserConfig> createUserConfigs() {
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        int num = new Random().nextInt();
        UserConfig userConfig = new TextUserConfig("name:" + num,
                                                   "display-name:" + num);
        userConfigs.add(userConfig);
        return userConfigs;
    }

    @Test
    public void testClone() throws Exception {
        verify(mode, mode, true);

        UserConfigMode clone = mode.clone();
        verify(mode, clone, true);
        clone.setDisplayName("new-display-name");
        verify(mode, clone, false);

        clone = mode.clone();
        verify(mode, clone, true);
        clone.setName("new-name");
        verify(mode, clone, false);

        clone = mode.clone();
        verify(mode, clone, true);
        clone.setSelected(false);
        verify(mode, clone, false);

        clone = mode.clone();
        verify(mode, clone, true);
        clone.setUserConfigModeSets(createUserConfigModeSets());
        verify(mode, clone, false);

        clone = mode.clone();
        verify(mode, clone, true);
        clone.setUserConfigs(createUserConfigs());
        verify(mode, clone, false);
    }

    private void verify(UserConfigMode source,
                        UserConfigMode clone,
                        boolean valid) {
        boolean isValid = false;
        try {
            Assert.assertEquals(source.getDisplayName(),
                                clone.getDisplayName());
            Assert.assertEquals(source.isSelected(), clone.isSelected());
            Assert.assertEquals(source.getName(), clone.getName());
            Assert.assertEquals(source.getUserConfigModeSets(),
                                clone.getUserConfigModeSets());
            Assert.assertEquals(source.getUserConfigs(),
                                clone.getUserConfigs());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(t.getMessage(), valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }

}
