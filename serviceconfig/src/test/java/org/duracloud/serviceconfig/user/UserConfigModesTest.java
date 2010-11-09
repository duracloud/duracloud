/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServiceInfoVerifyHelper;
import org.duracloud.serviceconfig.ServicesConfigDocument;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Aug 23, 2010
 */
public class UserConfigModesTest {

    /**
     * The structure below represents serviceInfo object created in this test.
     * <p/>
     * serviceInfo
     * --userConfigs
     * <p/>
     * --modeSet10
     * ----mode100
     * ------userConfigs
     * ------modeSets - null
     * <p/>
     * --modeSet11
     * ----mode110
     * ------userConfigs
     * ------modeSet1100
     * --------mode11000
     * ----------userConfigs
     * ----------modeSets - null
     * --------mode11001
     * ----------userConfigs
     * ----------modeSets - null
     * --------mode11002
     * ----------userConfigs
     * ----------modeSets - null
     * ------modeSet1101
     * --------mode11010
     * ----------userConfigs
     * ----------modeSets - null
     * --------mode11011
     * ----------userConfigs
     * ----------modeSets - null
     * <p/>
     * --modeSet12
     * ----mode120
     * ------userConfigs - null
     * ------modeSet1200
     * --------mode12000
     * ----------userConfigs - null
     * ----------modeSet120000
     * ------------mode1200000
     * --------------userConfigs
     * --------------modeSets - null
     */

    ServiceInfo serviceInfo;
    Map<Integer, Def> defs;

    private final String display = "display-";
    private final String val = "value-";

    @Before
    public void setUp() throws Exception {
        createDefs();
        serviceInfo = new ServiceInfo();

        Def d = getDef(1);
        serviceInfo.setUserConfigs(createUserConfigs(d.id, d.configCount));
        serviceInfo.setModeSets(createModeSets(d.id * 10, d.modeSetCount));
    }

    private void createDefs() {
        defs = new HashMap<Integer, Def>();
        Def d = new Def(1);
        d.configCount = 2;
        d.modeSetCount = 3;
        defs.put(d.id, d);

        d = new Def(10);
        d.modeCount = 1;
        defs.put(d.id, d);

        d = new Def(100);
        d.configCount = 3;
        defs.put(d.id, d);

        d = new Def(11);
        d.modeCount = 1;
        defs.put(d.id, d);

        d = new Def(110);
        d.modeSetCount = 2;
        d.configCount = 4;
        defs.put(d.id, d);

        d = new Def(1100);
        d.modeCount = 3;
        defs.put(d.id, d);

        d = new Def(11000);
        d.configCount = 5;
        defs.put(d.id, d);

        d = new Def(11001);
        d.configCount = 4;
        defs.put(d.id, d);

        d = new Def(11002);
        d.configCount = 3;
        defs.put(d.id, d);

        d = new Def(1101);
        d.modeCount = 2;
        defs.put(d.id, d);

        d = new Def(11010);
        d.configCount = 2;
        defs.put(d.id, d);

        d = new Def(11011);
        d.configCount = 4;
        defs.put(d.id, d);

        d = new Def(12);
        d.modeCount = 1;
        defs.put(d.id, d);

        d = new Def(120);
        d.modeSetCount = 1;
        defs.put(d.id, d);

        d = new Def(1200);
        d.modeCount = 1;
        defs.put(d.id, d);

        d = new Def(12000);
        d.modeSetCount = 1;
        defs.put(d.id, d);

        d = new Def(120000);
        d.modeCount = 1;
        defs.put(d.id, d);

        d = new Def(1200000);
        d.configCount = 5;
        defs.put(d.id, d);
    }

    private List<UserConfigModeSet> createModeSets(int id, int count) {
        if (count == 0) {
            return null;
        }

        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        for (int i = 0; i < count; ++i) {
            Def def = getDef(id + i);
            UserConfigModeSet modeSet = createModeSet(def.id, def.modeCount);

            modeSets.add(modeSet);
        }

        return modeSets;
    }

    private UserConfigModeSet createModeSet(int id, int count) {
        if (0 == count) {
            return null;
        }

        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setId(id);
        modeSet.setName(buildName(id));

        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        for (int i = 0; i < count; ++i) {
            Def def = getDef(id * 10 + i);
            UserConfigMode mode = createMode(def.id,
                                             def.configCount,
                                             def.modeSetCount);

            modes.add(mode);
        }
        modeSet.setModes(modes);

        return modeSet;
    }

    private UserConfigMode createMode(int id,
                                      int configCount,
                                      int modeSetCount) {
        if (configCount == 0 && modeSetCount == 0) {
            return null;
        }

        UserConfigMode mode = new UserConfigMode();

        int nextId = id * 10;
        List<UserConfig> userConfigs = createUserConfigs(id, configCount);
        List<UserConfigModeSet> modeSets = createModeSets(nextId, modeSetCount);

        if (null != userConfigs) {
            mode.setUserConfigs(userConfigs);
        }

        if (null != modeSets) {
            mode.setUserConfigModeSets(modeSets);
        }

        mode.setDisplayName(display + id);
        if (isFirstMode(id)) {
            mode.setSelected(true);
        }

        return mode;
    }

    private List<UserConfig> createUserConfigs(int id, int count) {
        if (count == 0) {
            return null;
        }

        List<UserConfig> configs = new ArrayList<UserConfig>();
        for (int i = 0; i < count; ++i) {
            String n = new Integer(id + i).toString();
            UserConfig config = new TextUserConfig(n,
                                                   display + n,
                                                   val + n,
                                                   null);
            configs.add(config);
        }

        return configs;
    }

    @Test
    public void testGetUserConfigModes() throws Exception {
        verifyServiceInfo(serviceInfo);

        String xml = ServicesConfigDocument.getServiceAsXML(serviceInfo);
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes());

        ServiceInfo info = ServicesConfigDocument.getService(xmlStream);
        xmlStream.close();
        Assert.assertNotNull(info);

        ServiceInfoVerifyHelper verifier = new ServiceInfoVerifyHelper(
            serviceInfo);
        verifier.verify(info);

        verifyServiceInfo(info);
    }


    private void verifyServiceInfo(ServiceInfo info) {
        Def d = getDef(1);
        List<UserConfig> userConfigs = info.getUserConfigs();
        verifyUserConfigs(d, userConfigs);

        List<UserConfigModeSet> modeSets = info.getModeSets();
        verifyModeSets(d, modeSets);
    }

    private void verifyUserConfigs(Def d, List<UserConfig> userConfigs) {
        if (d.configCount == 0) {
            return;
        }

        Assert.assertNotNull(userConfigs);
        Assert.assertEquals(d.configCount, userConfigs.size());
        for (int i = 0; i < d.configCount; ++i) {
            String name = new Integer(d.id + i).toString();
            Assert.assertEquals(name, userConfigs.get(i).getName());
        }
    }

    private void verifyModeSets(Def d, List<UserConfigModeSet> modeSets) {
        if (d.modeSetCount == 0) {
            return;
        }

        Assert.assertNotNull(modeSets);
        Assert.assertEquals(d.modeSetCount, modeSets.size());

        for (int i = 0; i < d.modeSetCount; ++i) {
            UserConfigModeSet modeSet = modeSets.get(i);

            int modeSetId = d.id * 10 + i;
            Def def = getDef(modeSetId);
            verifyModeSet(def, modeSet);
        }
    }

    private void verifyModeSet(Def d, UserConfigModeSet modeSet) {
        Assert.assertNotNull(modeSet);
        Assert.assertEquals(d.id, modeSet.getId());
        Assert.assertEquals(d.name, modeSet.getName());

        List<UserConfigMode> modes = modeSet.getModes();
        for (int i = 0; i < d.modeCount; ++i) {
            UserConfigMode mode = modes.get(i);

            List<UserConfig> userConfigs = mode.getUserConfigs();
            List<UserConfigModeSet> modeSets = mode.getUserConfigModeSets();

            int modeId = d.id * 10 + i;
            Def def = getDef(modeId);
            verifyMode(modeId, mode);
            verifyUserConfigs(def, userConfigs);
            verifyModeSets(def, modeSets);
        }
    }

    private void verifyMode(int modeId, UserConfigMode mode) {
        Assert.assertNotNull(mode);
        Assert.assertEquals(display + modeId, mode.getDisplayName());
        Assert.assertEquals(isFirstMode(modeId), mode.isSelected());
    }

    private boolean isFirstMode(int modeId) {
        return modeId % 10 == 0;
    }

    private Def getDef(int id) {
        Def def = defs.get(id);
        Assert.assertNotNull("id: " + id, def);
        return def;
    }

    private class Def {
        private int id = -1;
        private String name = "";
        private int configCount = 0;
        private int modeSetCount = 0;
        private int modeCount = 0;

        public Def(int id) {
            this.id = id;
            this.name = buildName(id);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(id);
            sb.append(",c");
            sb.append(configCount);
            sb.append(",s");
            sb.append(modeSetCount);
            sb.append(",m");
            sb.append(modeCount);
            sb.append("]");
            return sb.toString();
        }
    }

    private String buildName(int id) {
        return "name_" + id;
    }
}
