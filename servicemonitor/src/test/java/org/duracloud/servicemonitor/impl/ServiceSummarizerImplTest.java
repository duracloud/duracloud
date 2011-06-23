/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.services.ComputeService.SYSTEM_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 6/23/11
 */
public class ServiceSummarizerImplTest {

    private ServicesManager servicesMgr;
    private ServiceSummarizerImpl summarizer;

    @Before
    public void setup() {
        servicesMgr = EasyMock.createMock(ServicesManager.class);
        summarizer = new ServiceSummarizerImpl(servicesMgr);
    }

    private void replayMocks() {
        EasyMock.replay(servicesMgr);
    }

    @After
    public void teardown() {
        EasyMock.verify(servicesMgr);
    }

    @Test
    public void testGetModeSetConfig() {
        replayMocks();

        List<UserConfigModeSet> modeSets = buildModeSets();

        Map<String, String> config = new HashMap<String, String>();
        config = summarizer.getModeSetConfig(config, modeSets);
        assertNotNull(config);
        assertEquals(3, config.size());
        assertTrue("missing Mode Set 1", config.containsKey("Mode Set 1"));
        assertEquals("Mode 1", config.get("Mode Set 1"));
        assertTrue("missing Choice 1", config.containsKey("Choice 1"));
        assertEquals("value1", config.get("Choice 1"));
        assertTrue("missing Choice 2", config.containsKey("Choice 2"));
        assertEquals("option1", config.get("Choice 2"));
    }

    private List<UserConfigModeSet> buildModeSets() {
        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();

        // Mode 1
        List<UserConfig> mode1UserConfig = new ArrayList<UserConfig>();
        mode1UserConfig.add(
            new TextUserConfig("choice1", "Choice 1", "value1"));

        Option option1 = new Option("option1", "Option 1", true);
        List<Option> options = new ArrayList<Option>();
        options.add(option1);
        mode1UserConfig.add(
            new SingleSelectUserConfig("choice2", "Choice 2", options));

        UserConfigMode mode1 = new UserConfigMode();
        mode1.setUserConfigs(mode1UserConfig);
        mode1.setName("mode1");
        mode1.setDisplayName("Mode 1");
        mode1.setSelected(true);

        // Mode 2
        List<UserConfig> mode2UserConfig = new ArrayList<UserConfig>();
        mode2UserConfig.add(
            new TextUserConfig("choice3", "Choice 3", "value3"));

        UserConfigMode mode2 = new UserConfigMode();
        mode2.setUserConfigs(mode2UserConfig);
        mode2.setName("mode2");
        mode2.setDisplayName("Mode 2");
        mode2.setSelected(false);

        // Mode Set
        UserConfigModeSet modeSet1 = new UserConfigModeSet(null);
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        modes.add(mode1);
        modes.add(mode2);
        modeSet1.setModes(modes);
        modeSet1.setDisplayName("Mode Set 1");

        modeSets.add(modeSet1);
        return modeSets;
    }

    @Test
    public void testGetServiceProps() throws Exception {
        int serviceId = 20;
        int depId = 40;
        setUpMocksGetServiceProps(serviceId, depId);

        Map<String, String> serviceProps =
            summarizer.getServiceProps(serviceId, depId);
        assertNotNull(serviceProps);
        assertEquals(2, serviceProps.size());
        assertTrue("missing key one", serviceProps.containsKey("one"));
        assertTrue("missing key two", serviceProps.containsKey("two"));
    }

    private void setUpMocksGetServiceProps(int serviceId, int depId)
        throws Exception {
        Map<String, String> serviceProps = new HashMap<String, String>();
        serviceProps.put("one", "one");
        serviceProps.put("two", "two");
        serviceProps.put(SYSTEM_PREFIX + "three", "three");

        EasyMock.expect(servicesMgr.getDeployedServiceProps(serviceId, depId))
            .andReturn(serviceProps)
            .times(1);

        replayMocks();
    }

}
