/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Nov 19, 2010
 */
public class UserConfigModeSetTest {

    private UserConfigModeSet modeSet;

    @Test
    public void testDefaultConstructor() throws Exception {
        modeSet = new UserConfigModeSet();
        Assert.assertNotNull(modeSet.getId());
        Assert.assertNull(modeSet.getModes());
        Assert.assertNull(modeSet.getDisplayName());
        Assert.assertNull(modeSet.getName());
        Assert.assertNull(modeSet.getValue());
    }

    @Test
    public void testConstructor() throws Exception {
        UserConfig userConfig = new TextUserConfig("not-used", "not-used");
        List<UserConfig> userConfigs = Arrays.asList(new UserConfig[]{userConfig});

        modeSet = new UserConfigModeSet(userConfigs);

        int id = modeSet.getId();
        String displayName = modeSet.getDisplayName();
        String name = modeSet.getName();
        String value = modeSet.getValue();
        List<UserConfigMode> modes = modeSet.getModes();

        Assert.assertNotNull(id);
        Assert.assertNotNull(displayName);
        Assert.assertNotNull(name);
        Assert.assertNotNull(value);
        Assert.assertNotNull(modes);

        Assert.assertEquals(-1, id);
        Assert.assertEquals("Default Mode Set", displayName);
        Assert.assertEquals("defaultModeSet", name);
        Assert.assertEquals("defaultMode", value);
        Assert.assertEquals(1, modes.size());

        UserConfigMode mode = modes.get(0);
        Assert.assertEquals("defaultMode", mode.getName());
        Assert.assertEquals("Default Mode", mode.getDisplayName());

        List<UserConfig> testUserConfigs = mode.getUserConfigs();
        Assert.assertNotNull(testUserConfigs);

        Assert.assertEquals(1, testUserConfigs.size());
        UserConfig testUserConfig = testUserConfigs.get(0);
        Assert.assertEquals(userConfig, testUserConfig);
    }

    @Test
    public void testHasOnlyUserConfigs() {
        modeSet = new UserConfigModeSet();
        Assert.assertFalse(modeSet.hasOnlyUserConfigs());

        List<UserConfig> userConfigs = null;
        modeSet = new UserConfigModeSet(userConfigs);
        Assert.assertFalse(modeSet.hasOnlyUserConfigs());

        userConfigs = new ArrayList<UserConfig>();
        modeSet = new UserConfigModeSet(userConfigs);
        Assert.assertFalse(modeSet.hasOnlyUserConfigs());

        userConfigs = Arrays.asList(new UserConfig[]{new TextUserConfig("",
                                                                        "")});
        modeSet = new UserConfigModeSet(userConfigs);
        Assert.assertTrue(modeSet.hasOnlyUserConfigs());
    }

    @Test
    public void testWrappedUserConfigs() {
        modeSet = new UserConfigModeSet();
        doTest(modeSet, false);

        List<UserConfig> userConfigs = null;
        modeSet = new UserConfigModeSet(userConfigs);
        doTest(modeSet, false);

        userConfigs = new ArrayList<UserConfig>();
        modeSet = new UserConfigModeSet(userConfigs);
        doTest(modeSet, false);

        UserConfig config = new TextUserConfig("not-used", "not-used");
        userConfigs = Arrays.asList(new UserConfig[]{config});
        modeSet = new UserConfigModeSet(userConfigs);
        List<UserConfig> testUserConfigs = doTest(modeSet, true);

        Assert.assertNotNull(testUserConfigs);
        Assert.assertEquals(userConfigs.size(), testUserConfigs.size());
        Assert.assertTrue(userConfigs.size() > 0);
        for (int i = 0; i < userConfigs.size(); ++i) {
            Assert.assertEquals(userConfigs.get(i), testUserConfigs.get(i));
        }
    }

    private List<UserConfig> doTest(UserConfigModeSet modeSet, boolean valid) {
        List<UserConfig> userConfigs = null;
        boolean isValid = true;
        try {
            userConfigs = modeSet.wrappedUserConfigs();

        } catch (Exception e) {
            isValid = false;
        }
        Assert.assertEquals(valid, isValid);

        return userConfigs;
    }

}
