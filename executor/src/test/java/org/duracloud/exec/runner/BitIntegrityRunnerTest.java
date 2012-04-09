/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.runner;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.IOUtil;
import org.duracloud.exec.handler.BitIntegrityHandler;
import org.duracloud.exec.handler.HandlerTestBase;
import org.duracloud.execdata.bitintegrity.SpaceBitIntegrityResult;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.services.ComputeService;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    private String reportPath =
        "x-service-out/bit-integrity/report-id?storeId=0";
    private String reportContentId = "bit-integrity/report-id";

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        service = EasyMock.createMock("ServiceInfo", ServiceInfo.class);
        handler = EasyMock.createMock("BitIntegrityHandler",
                                      BitIntegrityHandler.class);
        runner = new BitIntegrityRunner(storeMgr,
                                        servicesMgr,
                                        manifestGenerator,
                                        service,
                                        handler);
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
     * bit integrity service should be run 4 times in generation mode and
     * another 4 times in comparison mode (after the manifest is retrieved).
     */
    @Test
    public void testRun() throws Exception {
        Map<String, String> state = new HashMap<String, String>();
        state.put(BitIntegrityRunner.STATE_STORE_ID, storeId);
        state.put(BitIntegrityRunner.STATE_SPACE_ID, space1);
        EasyMock.expect(handler.getState(BitIntegrityRunner.HANDLER_STATE_FILE))
                .andReturn(state);

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
                .andReturn("provider-type").times(2);

        handler.storeState(EasyMock.eq(BitIntegrityRunner.HANDLER_STATE_FILE),
                           EasyMock.isA(Map.class));
        EasyMock.expectLastCall().times(4);

        setUpStoreManifest(4);

        EasyMock.expect(service.getUserConfigModeSets())
                .andReturn(createConfig()).times(8);

        EasyMock.expect(service.getId())
                .andReturn(serviceId).anyTimes();

        EasyMock.expect(handler.getDeploymentHost(service))
                .andReturn(host).times(8);

        EasyMock.expect(service.getUserConfigVersion())
                .andReturn(configVersion).times(8);

        Map<String, String> successProps = getSuccessProps();

        // Space 1 - generated checksum
        Capture<List<UserConfigModeSet>> configCapture1 =
            setUpDeploy(depId1, successProps);
        Capture<List<UserConfigModeSet>> compCapture1 =
            setUpDeploy(depId1, successProps);
        Capture<SpaceBitIntegrityResult> resultCapture1 = setUpResult(space1);

        // Space 2 - generated checksum
        Capture<List<UserConfigModeSet>> configCapture2 =
            setUpDeploy(depId2, successProps);
        Capture<List<UserConfigModeSet>> compCapture2 =
            setUpDeploy(depId2, successProps);
        Capture<SpaceBitIntegrityResult> resultCapture2 = setUpResult(space2);

        // Space 1 - files checksum
        Capture<List<UserConfigModeSet>> configCapture3 =
            setUpDeploy(depId3, successProps);
        Capture<List<UserConfigModeSet>> compCapture3 =
            setUpDeploy(depId3, successProps);
        Capture<SpaceBitIntegrityResult> resultCapture3 = setUpResult(space1);

        // Space 2 - files checksum
        Capture<List<UserConfigModeSet>> configCapture4 =
            setUpDeploy(depId4, successProps);
        Capture<List<UserConfigModeSet>> compCapture4 =
            setUpDeploy(depId4, successProps);
        Capture<SpaceBitIntegrityResult> resultCapture4 = setUpResult(space2);

        handler.clearState(BitIntegrityRunner.HANDLER_STATE_FILE);
        EasyMock.expectLastCall().times(2);

        replayMocks();

        runner.run();

        verifyConfigCapture(configCapture1,
                            BitIntegrityRunner.HASH_APPROACH_FILES,
                            space1);
        verifyConfigCapture(configCapture2,
                            BitIntegrityRunner.HASH_APPROACH_FILES,
                            space2);
        verifyConfigCapture(configCapture3,
                            BitIntegrityRunner.HASH_APPROACH_PROVIDER,
                            space1);
        verifyConfigCapture(configCapture4,
                            BitIntegrityRunner.HASH_APPROACH_PROVIDER,
                            space2);

        verifyCompareCapture(compCapture1);
        verifyCompareCapture(compCapture2);
        verifyCompareCapture(compCapture3);
        verifyCompareCapture(compCapture4);

        verifyResultCapture(resultCapture1);
        verifyResultCapture(resultCapture2);
        verifyResultCapture(resultCapture3);
        verifyResultCapture(resultCapture4);
    }

    private void setUpStoreManifest(int times) throws Exception {
        InputStream manifest = IOUtil.writeStringToStream("manifest");
        EasyMock.expect(
            manifestGenerator.getManifest(EasyMock.eq(storeId),
                                          EasyMock.<String>anyObject(),
                                          EasyMock.eq(
                                              ManifestGenerator.FORMAT.TSV),
                                          EasyMock.<Date>isNull()))
                .andReturn(manifest)
                .times(times);

        EasyMock.expect(
            contentStore.addContent(EasyMock.eq(
                                        BitIntegrityRunner.RESULT_SPACE_ID),
                                    EasyMock.eq(
                                        BitIntegrityRunner.MANIFEST_CONTENT_ID),
                                    EasyMock.<InputStream>anyObject(),
                                    EasyMock.anyInt(),
                                    EasyMock.eq("text/tab-separated-values"),
                                    EasyMock.<String>isNull(),
                                    EasyMock.<Map<String,String>>isNull()))
                .andReturn("")
                .times(times);
    }

    private void verifyConfigCapture(Capture<List<UserConfigModeSet>> capture,
                                     String expHashApproach,
                                     String expSpaceId)
        throws Exception {
        List<UserConfigModeSet> modeSets = capture.getValue();
        UserConfigMode capMode = modeSets.get(0).getModes().get(0);
        assertEquals(BitIntegrityRunner.GENERATE_MODE_NAME, capMode.getName());
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

    private void verifyCompareCapture(Capture<List<UserConfigModeSet>> capture)
        throws Exception {
        List<UserConfigModeSet> modeSets = capture.getValue();
        UserConfigMode capMode = modeSets.get(0).getModes().get(1);
        assertEquals(BitIntegrityRunner.COMPARE_MODE_NAME, capMode.getName());
        assertTrue(capMode.isSelected());

        // Check contentId setting
        String manId = BitIntegrityRunner.MANIFEST_CONTENT_ID;
        String contentIdA =
            ((TextUserConfig)capMode.getUserConfigs().get(0)).getValue();
        assertTrue(reportContentId.equals(contentIdA) ||
                   manId.equals(contentIdA));
        String contentIdB =
            ((TextUserConfig)capMode.getUserConfigs().get(1)).getValue();
        assertTrue(reportContentId.equals(contentIdB) ||
                   manId.equals(contentIdB));

        // Check space setting
        List<UserConfig> spaceConfigs =
            capMode.getUserConfigModeSets().get(0).getModes().get(0)
                   .getUserConfigs();
        // Space A
        SingleSelectUserConfig targetSpaceIdA =
            (SingleSelectUserConfig)spaceConfigs.get(0);
        for(Option option : targetSpaceIdA.getOptions()) {
            verifyOption(option, BitIntegrityRunner.RESULT_SPACE_ID);
        }
        // Space B
        SingleSelectUserConfig targetSpaceIdB =
            (SingleSelectUserConfig)spaceConfigs.get(1);
        for(Option option : targetSpaceIdB.getOptions()) {
            verifyOption(option, BitIntegrityRunner.RESULT_SPACE_ID);
        }
    }

    private void verifyResultCapture(Capture<SpaceBitIntegrityResult> capture) {
        SpaceBitIntegrityResult result = capture.getValue();
        assertNotNull(result);
        assertEquals("success", result.getResult());
        assertEquals(reportPath, result.getReportContentId());
        long now = new Date().getTime();
        long resultTime = result.getCompletionDate().getTime();
        assertTrue(now >= resultTime);
        assertTrue(resultTime > now - 5000);
        assertTrue(result.isDisplay());
    }

    private Capture<List<UserConfigModeSet>> setUpDeploy(int depId,
                                                         Map<String, String> depProps)
        throws Exception {
        Capture<List<UserConfigModeSet>> configCapture =
            new Capture<List<UserConfigModeSet>>();
        EasyMock.expect(
            servicesMgr.deployService(EasyMock.eq(serviceId),
                                      EasyMock.eq(host),
                                      EasyMock.eq(configVersion),
                                      EasyMock.capture(configCapture)))
                .andReturn(depId);

        EasyMock.expect(servicesMgr.getDeployedServiceProps(serviceId, depId))
                .andReturn(depProps).times(2);

        servicesMgr.undeployService(serviceId, depId);
        EasyMock.expectLastCall();

        return configCapture;
    }

    private Map<String, String> getSuccessProps() {
        Map<String, String> depProps = new HashMap<String, String>();
        depProps.put(ComputeService.STATUS_KEY,
                     ComputeService.ServiceStatus.SUCCESS.name());
        depProps.put(ComputeService.REPORT_KEY, reportPath);
        depProps.put(ComputeService.FAILURE_COUNT_KEY, "0");
        return depProps;
    }

    private Map<String, String> getFailureProps() {
        Map<String, String> depProps = new HashMap<String, String>();
        depProps.put(ComputeService.STATUS_KEY,
                     ComputeService.ServiceStatus.FAILED.name());
        depProps.put(ComputeService.REPORT_KEY, reportPath);
        depProps.put(ComputeService.FAILURE_COUNT_KEY, "3");
        return depProps;
    }

    private Capture<SpaceBitIntegrityResult> setUpResult(String spaceId) {
        Capture<SpaceBitIntegrityResult> resultCapture =
            new Capture<SpaceBitIntegrityResult>();
        handler.storeResults(EasyMock.eq(storeId),
                             EasyMock.eq(spaceId),
                             EasyMock.capture(resultCapture));
        EasyMock.expectLastCall();
        return resultCapture;
    }

    private List<UserConfigModeSet> createConfig() {
        List<UserConfigModeSet> userConfig = new ArrayList<UserConfigModeSet>();
        UserConfigModeSet modeSet = new UserConfigModeSet();
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();

        UserConfigMode generateMode = createGenerateConfigMode();
        UserConfigMode compareMode = createCompareConfigMode();

        modes.add(generateMode);
        modes.add(compareMode);
        modeSet.setModes(modes);
        userConfig.add(modeSet);

        return userConfig;
    }

    private UserConfigMode createGenerateConfigMode() {
        // Generate Mode
        UserConfigMode mode = new UserConfigMode();
        mode.setName(BitIntegrityRunner.GENERATE_MODE_NAME);

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

        return mode;
    }

    private UserConfigMode createCompareConfigMode() {
        // Generate Mode
        UserConfigMode mode = new UserConfigMode();
        mode.setName(BitIntegrityRunner.COMPARE_MODE_NAME);

        // Content Ids
        List<UserConfig> configs = new ArrayList<UserConfig>();
        TextUserConfig contentA = new TextUserConfig("idA", "ID A");
        TextUserConfig contentB = new TextUserConfig("idB", "ID B");
        configs.add(contentA);
        configs.add(contentB);
        mode.setUserConfigs(configs);

        // Stores and spaces
        List<UserConfigModeSet> storesModeSets =
            new ArrayList<UserConfigModeSet>();
        UserConfigModeSet storesModeSet = new UserConfigModeSet();
        List<UserConfigMode> storeModes = new ArrayList<UserConfigMode>();
        UserConfigMode storeMode = new UserConfigMode();
        storeMode.setName(storeId);

        // Space A
        List<Option> spaceOptionsA = new ArrayList<Option>();
        Option adminSpaceOptionA =
            new Option("", handler.HANDLER_STATE_SPACE, false);
        spaceOptionsA.add(adminSpaceOptionA);
        UserConfig spaceConfigA =
            new SingleSelectUserConfig("spaceIdA", "", spaceOptionsA);
        // Space B
        List<Option> spaceOptionsB = new ArrayList<Option>();
        Option adminSpaceOptionB =
            new Option("", handler.HANDLER_STATE_SPACE, false);
        spaceOptionsB.add(adminSpaceOptionB);
        UserConfig spaceConfigB =
            new SingleSelectUserConfig("spaceIdB", "", spaceOptionsB);

        List<UserConfig> spaceConfigs = new ArrayList<UserConfig>();
        spaceConfigs.add(spaceConfigA);
        spaceConfigs.add(spaceConfigB);
        storeMode.setUserConfigs(spaceConfigs);

        // Putting it all together
        storeModes.add(storeMode);
        storesModeSet.setModes(storeModes);
        storesModeSets.add(storesModeSet);
        mode.setUserConfigModeSets(storesModeSets);

        return mode;
    }

    @Test
    public void testRunFailure() throws Exception {
        EasyMock.expect(service.getUserConfigModeSets())
                .andReturn(createConfig());

        EasyMock.expect(service.getId())
                .andReturn(serviceId);

        EasyMock.expect(handler.getDeploymentHost(service))
                .andReturn(host);

        EasyMock.expect(service.getUserConfigVersion())
                .andReturn(configVersion);

        setUpDeploy(depId1, getFailureProps());

        EasyMock.expect(service.getId())
                .andReturn(serviceId).anyTimes();

        Capture<String> failureMsgCap = new Capture<String>();
        handler.notify(EasyMock.capture(failureMsgCap));
        EasyMock.expectLastCall();

        EasyMock.expect(contentStore.getStoreId())
                .andReturn(storeId).anyTimes();
        EasyMock.expect(contentStore.getStorageProviderType())
                .andReturn(StorageProviderType.AMAZON_S3.getName()).anyTimes();

        Capture<SpaceBitIntegrityResult> storeResultCap =  setUpResult(space1);

        replayMocks();
        runner.runBitIntegrityOnSpace(contentStore, true, space1);

        String failureMsg = failureMsgCap.getValue();
        assertNotNull(failureMsg);

        SpaceBitIntegrityResult storeResult = storeResultCap.getValue();
        assertNotNull(storeResult);
        assertEquals("failure", storeResult.getResult());
    }

}
