/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.runner;

import org.duracloud.client.ContentStore;
import org.duracloud.exec.handler.BitIntegrityHandler;
import org.duracloud.exec.handler.HandlerTestBase;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.services.ComputeService;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 3/15/12
 */
public class BitIntegrityRunnerTest extends HandlerTestBase {

    private ServiceInfo service;
    private BitIntegrityHandler handler;
    private BitIntegrityRunner runner;

    private String storeId = "store-id";
    private String space1 = "space-1-id";
    private String space2 = "space-2-id";
    private int serviceId = 34;
    private String host = "host";
    private String configVersion = "1.1";
    private int depId1 = 11;
    private int depId2 = 12;
    private int depId3 = 13;
    private int depId4 = 14;

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        service = EasyMock.createMock("ServiceInfo", ServiceInfo.class);
        handler = EasyMock.createMock("BitIntegrityHandler",
                                      BitIntegrityHandler.class);
        runner = new BitIntegrityRunner(storeMgr, servicesMgr, service, handler);
    }

    @Override
    protected void replayMocks() {
        super.replayMocks();
        EasyMock.replay(service, handler);
    }

    @After
    @Override
    public void teardown() {
        super.teardown();
        EasyMock.verify(service, handler);
    }

    /**
     * Tests the bit integrity runner using a single (primary) provider
     * with 2 spaces. Each space should be verified against both the
     * provider checksum and a generated checksum, meaning that the
     * bit integrity service should be run 4 times.
     */
    @Test
    public void testRun() throws Exception {
        Map<String, ContentStore> stores = new HashMap<String, ContentStore>();
        stores.put("", contentStore);
        EasyMock.expect(storeMgr.getContentStores())
                .andReturn(stores);

        EasyMock.expect(contentStore.getStoreId())
                .andReturn(storeId).anyTimes();

        List<String> spaces = new ArrayList<String>();
        spaces.add(space1);
        spaces.add(space2);
        EasyMock.expect(contentStore.getSpaces())
                .andReturn(spaces).times(2);

        EasyMock.expect(contentStore.getStorageProviderType())
                .andReturn("provider-type").times(4);

        EasyMock.expect(service.getUserConfigModeSets())
                .andReturn(createConfig()).times(4);

        EasyMock.expect(service.getId())
                .andReturn(serviceId).anyTimes();

        EasyMock.expect(handler.getDeploymentHost(service))
                .andReturn(host).times(4);

        EasyMock.expect(service.getUserConfigVersion())
                .andReturn(configVersion).times(4);

        // Space 1 - generated checksum
        FixedCapture<List<UserConfigModeSet>> configCapture1 =
            setUpDeploy(depId1);

        // Space 2 - generated checksum
        FixedCapture<List<UserConfigModeSet>> configCapture2 =
            setUpDeploy(depId2);

        // Space 1 - files checksum
        FixedCapture<List<UserConfigModeSet>> configCapture3 =
            setUpDeploy(depId3);

        // Space 2 - files checksum
        FixedCapture<List<UserConfigModeSet>> configCapture4 =
            setUpDeploy(depId4);

        replayMocks();

        runner.run();

        verifyCapture(configCapture1,
                      BitIntegrityRunner.HASH_APPROACH_FILES,
                      space1);
        verifyCapture(configCapture2,
                      BitIntegrityRunner.HASH_APPROACH_FILES,
                      space2);
        verifyCapture(configCapture3,
                      BitIntegrityRunner.HASH_APPROACH_PROVIDER,
                      space1);
        verifyCapture(configCapture4,
                      BitIntegrityRunner.HASH_APPROACH_PROVIDER,
                      space2);
    }

    private void verifyCapture(FixedCapture<List<UserConfigModeSet>> capture,
                               String expHashApproach,
                               String expSpaceId)
        throws Exception {
        List<UserConfigModeSet> modeSets = capture.getValue();
        UserConfigMode capMode = modeSets.get(0).getModes().get(0);
        assertEquals(BitIntegrityRunner.MODE_NAME, capMode.getName());
        assertTrue(capMode.isSelected());

        // Check hash approach setting
        SingleSelectUserConfig hashApproach =
            (SingleSelectUserConfig)capMode.getUserConfigs().get(0);
        for(Option option : hashApproach.getOptions()) {
            verifyOption(option, expHashApproach);
        }

        // Check space setting
        SingleSelectUserConfig targetSpaceId =
            (SingleSelectUserConfig)capMode.getUserConfigModeSets().get(0)
                                    .getModes().get(0).getUserConfigs().get(0);
        for(Option option : targetSpaceId.getOptions()) {
            verifyOption(option, expSpaceId);
        }
    }

    private void verifyOption(Option op, String expValue) {
        if(op.getValue().equals(expValue)) {
            assertTrue(op.isSelected());
        } else {
            assertFalse(op.isSelected());
        }
    }

    private FixedCapture<List<UserConfigModeSet>> setUpDeploy(int depId) throws Exception {
        FixedCapture<List<UserConfigModeSet>> configCapture =
            new FixedCapture<List<UserConfigModeSet>>();
        EasyMock.expect(
            servicesMgr.deployService(EasyMock.eq(serviceId),
                                      EasyMock.eq(host),
                                      EasyMock.eq(configVersion),
                                      EasyMock.capture(configCapture)))
                .andReturn(depId);

        Map<String, String> depProps = new HashMap<String, String>();
        depProps.put(ComputeService.STATUS_KEY,
                     ComputeService.ServiceStatus.SUCCESS.name());
        depProps.put(ComputeService.REPORT_KEY, "report");
        EasyMock.expect(servicesMgr.getDeployedServiceProps(serviceId, depId))
                .andReturn(depProps).times(2);

        servicesMgr.undeployService(serviceId, depId);
        EasyMock.expectLastCall();

        return configCapture;
    }

    private List<UserConfigModeSet> createConfig() {
        List<UserConfigModeSet> userConfig = new ArrayList<UserConfigModeSet>();
        UserConfigModeSet modeSet = new UserConfigModeSet();
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        UserConfigMode mode = new UserConfigMode();
        mode.setName(BitIntegrityRunner.MODE_NAME);

        // Hash Approach
        List<UserConfig> configs = new ArrayList<UserConfig>();
        List<Option> hashOptions = new ArrayList<Option>();
        Option filesOption =
            new Option("", BitIntegrityRunner.HASH_APPROACH_FILES, false);
        Option providerOption =
            new Option("", BitIntegrityRunner.HASH_APPROACH_PROVIDER, false);
        hashOptions.add(filesOption);
        hashOptions.add(providerOption);
        UserConfig hashConfig =
            new SingleSelectUserConfig("hashApproach", "", hashOptions);
        configs.add(hashConfig);
        mode.setUserConfigs(configs);

        // Stores and spaces
        List<UserConfigModeSet> storesModeSets =
            new ArrayList<UserConfigModeSet>();
        UserConfigModeSet storesModeSet = new UserConfigModeSet();
        List<UserConfigMode> storeModes = new ArrayList<UserConfigMode>();
        UserConfigMode storeMode = new UserConfigMode();
        storeMode.setName(storeId);

        List<UserConfig> spaceConfigs = new ArrayList<UserConfig>();
        List<Option> spaceOptions = new ArrayList<Option>();
        Option space1Option = new Option("", space1, false);
        Option space2Option = new Option("", space2, false);
        spaceOptions.add(space1Option);
        spaceOptions.add(space2Option);
        UserConfig spaceConfig =
            new SingleSelectUserConfig("targetSpaceId", "", spaceOptions);
        spaceConfigs.add(spaceConfig);
        storeMode.setUserConfigs(spaceConfigs);

        // Putting it all together
        storeModes.add(storeMode);
        storesModeSet.setModes(storeModes);
        storesModeSets.add(storesModeSet);
        mode.setUserConfigModeSets(storesModeSets);

        modes.add(mode);
        modeSet.setModes(modes);
        userConfig.add(modeSet);

        return userConfig;
    }

    /*
     * Fixes a bug in EasyMock which is seen when using multiple Captures.
     * http://sourceforge.net/tracker/?func=detail&atid=567837&aid=2617107&group_id=82958
     */
    private class FixedCapture<T> extends Capture<T> {
        @Override
        public void setValue(T value) {
            if (!hasCaptured()) {
                super.setValue(value);
            }
        }
    }

}
