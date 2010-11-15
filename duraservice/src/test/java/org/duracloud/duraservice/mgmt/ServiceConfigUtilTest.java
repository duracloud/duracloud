/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.duracloud.duraservice.domain.ServiceComputeInstance;
import org.duracloud.duraservice.domain.UserStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Apr 2, 2010
 */
public class ServiceConfigUtilTest {

    private ServiceConfigUtil util;
    private ContentStoreManagerUtil contentStoreManagerUtil;
    private List<ServiceComputeInstance> serviceComputeInstances;

    private Map<String, ContentStore> contentStores;
    private final int NUM_CONTENT_STORES = 2;

    private List<String> spaces;
    private final String space0 = "space0";
    private final String space1 = "space1";
    private final String space2 = "space2";

    private ServiceInfo service;
    private List<UserConfigModeSet> modeSets;
    private final String modeSetNameTop = "modeset.name.top";
    private final String modeSetNameIn = "modeset.name.in";
    private final String modeSetNameOut = "modeset.name.out";

    private final String modeNameOn = "mode.name.on";
    private final String modeNameOff = "mode.name.off";

    private final String optName0 = "option.name.0";
    private final String optName1 = "option.name.1";

    private final String configName0 = "config.name.0";
    private final String configName1 = "config.name.1";

    private UserStore userStore;
    private List<SystemConfig> systemConfig;

    private final String computeHostName = "compute.host.name";
    private final String computeDisplayName = "compute.display.name";

    private final String hostName = "test.host.name";
    private final String portName = "test.port.name";
    private final String contextName = "test.context.name";
    private final String msgBrokerUrlName = "test.msgBrokerUrl.name";
    private final String usernameName = "test.username.name";
    private final String passwordName = "test.password.name";

    private final String hostVal = ServiceConfigUtil.STORE_HOST_VAR;
    private final String portVal = ServiceConfigUtil.STORE_PORT_VAR;
    private final String contextVal = ServiceConfigUtil.STORE_CONTEXT_VAR;
    private final String msgBrokerUrlVal = ServiceConfigUtil.STORE_MSG_BROKER_VAR;
    private final String usernameVal = ServiceConfigUtil.STORE_USER_VAR;
    private final String passwordVal = ServiceConfigUtil.STORE_PWORD_VAR;

    private final String hostDefault = "test.host.default";
    private final String portDefault = "8888";
    private final String contextDefault = "test.context.default";
    private final String msgBrokerUrlDefault = "test.msgBrokerUrl.default";
    private final String usernameDefault = "test.username.default";
    private final String passwordDefault = "test.password.default";

    private final String host = "test.host";
    private final String port = "9999";
    private final String context = "test.context";
    private final String msgBrokerUrl = "test.msgBrokerUrl";
    private final String username = "test.username";
    private final String password = "test.password";


    @Before
    public void setUp() throws Exception {
        contentStores = createContentStores();
        contentStoreManagerUtil = createMockContentStoreManagerUtil();
        util = new ServiceConfigUtil(contentStoreManagerUtil);

        serviceComputeInstances = new ArrayList<ServiceComputeInstance>();
        serviceComputeInstances.add(new ServiceComputeInstance(computeHostName,
                                                               computeDisplayName,
                                                               null));

        service = new ServiceInfo();
        service.setDeploymentOptions(new ArrayList<DeploymentOption>());
        service.setModeSets(createModeSets());

        userStore = new UserStore();
        userStore.setHost(host);
        userStore.setPort(port);
        userStore.setContext(context);
        userStore.setMsgBrokerUrl(msgBrokerUrl);

        systemConfig = new ArrayList<SystemConfig>();
        SystemConfig config0 = new SystemConfig(hostName, hostVal, hostDefault);
        SystemConfig config1 = new SystemConfig(portName, portVal, portDefault);
        SystemConfig config2 = new SystemConfig(contextName,
                                                contextVal,
                                                contextDefault);
        SystemConfig config3 = new SystemConfig(msgBrokerUrlName,
                                                msgBrokerUrlVal,
                                                msgBrokerUrlDefault);
        SystemConfig config4 = new SystemConfig(usernameName,
                                                usernameVal,
                                                usernameDefault);
        SystemConfig config5 = new SystemConfig(passwordName,
                                                passwordVal,
                                                passwordDefault);
        systemConfig.add(config0);
        systemConfig.add(config1);
        systemConfig.add(config2);
        systemConfig.add(config3);
        systemConfig.add(config4);
        systemConfig.add(config5);
    }

    private Map<String, ContentStore> createContentStores()
        throws ContentStoreException {
        spaces = new ArrayList<String>();
        spaces.add(space0);
        spaces.add(space1);
        spaces.add(space2);

        contentStores = new HashMap<String, ContentStore>();
        for (int i = 0; i < NUM_CONTENT_STORES; ++i) {
            contentStores.put("id_" + i, createMockContentStore(i));
        }

        return contentStores;
    }

    private ContentStore createMockContentStore(int i)
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock("ContentStore",
                                                        ContentStore.class);
        EasyMock.expect(contentStore.getStorageProviderType()).andReturn(
            StorageProviderType.AMAZON_S3.name()).anyTimes();
        EasyMock.expect(contentStore.getSpaces()).andReturn(spaces).anyTimes();
        EasyMock.expect(contentStore.getStoreId()).andReturn("stId").anyTimes();

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private List<UserConfigModeSet> createModeSets() {

        // bottom-level modes
        List<Option> options0 = new ArrayList<Option>();
        List<Option> options1 = new ArrayList<Option>();
        options0.add(new Option(optName0, ServiceConfigUtil.SPACES_VAR, false));
        options1.add(new Option(optName1, ServiceConfigUtil.SPACES_VAR, false));

        List<UserConfig> userConfigs0 = new ArrayList<UserConfig>();
        userConfigs0.add(new SingleSelectUserConfig(configName0,
                                                   configName0,
                                                   options0));

        List<UserConfig> userConfigs1 = new ArrayList<UserConfig>();
        userConfigs1.add(new SingleSelectUserConfig(configName1,
                                                   configName1,
                                                   options1));

        UserConfigMode modeSrc = new UserConfigMode();
        modeSrc.setName("not-used");
        modeSrc.setDisplayName("not-used");
        modeSrc.setUserConfigs(userConfigs0);

        UserConfigMode modeDest = new UserConfigMode();
        modeDest.setName("not-used");
        modeDest.setDisplayName("not-used");
        modeDest.setUserConfigs(userConfigs1);

        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        modes.add(modeSrc);
        modes.add(modeDest);

        // second-level modesets
        UserConfigModeSet modeSetIn = new UserConfigModeSet();
        modeSetIn.setValue(ServiceConfigUtil.ALL_STORE_SPACES_VAR);
        modeSetIn.setDisplayName(modeSetNameIn);
        modeSetIn.setName(modeSetNameIn);
        modeSetIn.setModes(modes);

        UserConfigModeSet modeSetOut = new UserConfigModeSet();
        modeSetOut.setValue(ServiceConfigUtil.ALL_STORE_SPACES_VAR);
        modeSetOut.setDisplayName(modeSetNameOut);
        modeSetOut.setName(modeSetNameOut);
        modeSetOut.setModes(modes);

        List<UserConfigModeSet> modeSetsB = new ArrayList<UserConfigModeSet>();
        modeSetsB.add(modeSetIn);
        modeSetsB.add(modeSetOut);


        // top-level modeset
        UserConfigMode modeOn = new UserConfigMode();
        modeOn.setName(modeNameOn);
        modeOn.setDisplayName(modeNameOn);

        UserConfigMode modeOff = new UserConfigMode();
        modeOff.setName(modeNameOff);
        modeOff.setDisplayName(modeNameOff);

        List<UserConfigMode> modesOnOff = new ArrayList<UserConfigMode>();
        modesOnOff.add(modeOn);
        modesOnOff.add(modeOff);

        UserConfigModeSet modeSetTop = new UserConfigModeSet();
        modeSetTop.setDisplayName(modeSetNameTop);
        modeSetTop.setName(modeSetNameTop);
        modeSetTop.setModes(modesOnOff);

        modeOn.setUserConfigModeSets(modeSetsB);
        modeOff.setUserConfigModeSets(modeSetsB);

        modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(modeSetTop);

        return modeSets;
    }

    @After
    public void tearDown() {
        contentStoreManagerUtil = null;
        userStore = null;
        systemConfig = null;
    }

    @Test
    public void testPopulateServiceNull() {
        boolean success = false;

        service.setModeSets(null);
        try {
            util.populateService(service,
                                 serviceComputeInstances,
                                 userStore,
                                 computeHostName);
            success = true;
            
        } catch (Exception e) {
            Assert.fail("Exception not expected: " + e.getMessage());
        }

        Assert.assertTrue(success);
    }

    @Test
    public void testPopulateService() throws ContentStoreException {
        ServiceInfo serviceInfo = util.populateService(service,
                                                       serviceComputeInstances,
                                                       userStore,
                                                       computeHostName);

        List<UserConfigModeSet> testModeSets = serviceInfo.getModeSets();
        Assert.assertNotNull("modeSets is null", testModeSets);
        Assert.assertEquals(1, testModeSets.size());

        UserConfigModeSet testModeSet = testModeSets.get(0);
        Assert.assertEquals(modeSetNameTop, testModeSet.getName());
        Assert.assertEquals(modeSetNameTop, testModeSet.getDisplayName());
        Assert.assertNull(testModeSet.getValue());

        List<UserConfigMode> testModes = testModeSet.getModes();
        Assert.assertNotNull(testModes);
        Assert.assertEquals(2, testModes.size());

        for (UserConfigMode testMode : testModes) {
            Assert.assertEquals(testMode.getDisplayName(), testMode.getName());
            if (modeNameOn.equals(testMode.getDisplayName())) {
                Assert.assertEquals(modeNameOn, testMode.getName());

            } else if (modeNameOff.equals(testMode.getDisplayName())) {
                Assert.assertEquals(modeNameOff, testMode.getName());

            } else {
                Assert.fail("Unexpected modeName:'" + testMode.getName() + "'");
            }

            Assert.assertNull(testMode.getUserConfigs());
            verifyModeSets(testMode.getUserConfigModeSets());
        }
    }

    private void verifyModeSets(List<UserConfigModeSet> testModeSets) {
        Assert.assertNotNull(testModeSets);
        Assert.assertEquals(2, testModeSets.size());

        for (UserConfigModeSet testModeSet : testModeSets) {
            String name = testModeSet.getName();
            String displayName = testModeSet.getDisplayName();
            Assert.assertNotNull("mode set name is null", name);
            Assert.assertNotNull("mode set displayName is null", displayName);

            Assert.assertEquals(name, displayName);
            if (modeSetNameIn.equals(name)) {
                Assert.assertEquals(modeSetNameIn, displayName);

            } else if (modeSetNameOut.equals(name)) {
                Assert.assertEquals(modeSetNameOut, displayName);

            } else {
                Assert.fail("Unexpected name: " + name);
            }

            List<UserConfigMode> testModes = testModeSet.getModes();
            Assert.assertNotNull("mode list is null", testModes);

            Assert.assertEquals(2, testModes.size());
            for (UserConfigMode testMode : testModes) {
                String modeName = testMode.getName();
                String modeDisplayName = testMode.getDisplayName();
                Assert.assertNotNull("mode name is null", modeName);
                Assert.assertNotNull("mode display is null", modeDisplayName);

                Assert.assertEquals(modeName, modeDisplayName);
                Assert.assertEquals(StorageProviderType.AMAZON_S3.name(),
                                    modeName);

                List<UserConfig> userConfigs = testMode.getUserConfigs();
                Assert.assertNotNull(userConfigs);

                Assert.assertEquals(1, userConfigs.size());
                UserConfig userConfig = userConfigs.get(0);

                String configName = userConfig.getName();
                String configDisplayName = userConfig.getDisplayName();
                Assert.assertNotNull(configName);
                Assert.assertNotNull(configDisplayName);

                Assert.assertEquals(configName, configDisplayName);

                if (configName0.equals(configName)) {
                    Assert.assertEquals(configName0, configDisplayName);

                } else if (configName1.equals(configName)) {
                    Assert.assertEquals(configName1, configDisplayName);

                } else {
                    Assert.fail("Unexpected name: " + configName);
                }

                Assert.assertTrue(userConfig instanceof SingleSelectUserConfig);
                SingleSelectUserConfig singleSelectUserConfig = (SingleSelectUserConfig) userConfig;
                List<Option> options = singleSelectUserConfig.getOptions();
                Assert.assertNotNull(options);

                Assert.assertEquals(spaces.size(), options.size());
                boolean foundSpace0 = false;
                boolean foundSpace1 = false;
                boolean foundSpace2 = false;
                for (Option option : options) {
                    if (space0.equals(option.getDisplayName())) {
                        foundSpace0 = true;
                    } else if (space1.equals(option.getDisplayName())) {
                        foundSpace1 = true;
                    } else if (space2.equals(option.getDisplayName())) {
                        foundSpace2 = true;
                    }
                }
                Assert.assertTrue("space 0 not found", foundSpace0);
                Assert.assertTrue("space 1 not found", foundSpace1);
                Assert.assertTrue("space 2 not found", foundSpace2);

                Assert.assertNull(testMode.getUserConfigModeSets());
            }
        }
    }

    @Test
    public void testResolveSystemConfigVars() throws ContentStoreException {
        List<SystemConfig> newConfig = util.resolveSystemConfigVars(userStore,
                                                                    systemConfig);
        Assert.assertNotNull(newConfig);

        boolean foundHost = false;
        boolean foundPort = false;
        boolean foundContext = false;
        boolean foundMsgBrokerUrl = false;
        boolean foundUsername = false;
        boolean foundPassword = false;

        Assert.assertEquals(systemConfig.size(), newConfig.size());
        for (SystemConfig config : newConfig) {
            String name = config.getName();
            String val = config.getValue();
            String def = config.getDefaultValue();

            Assert.assertNotNull(name);
            Assert.assertNotNull(val);
            Assert.assertNotNull(def);

            if (name.equals(hostName)) {
                foundHost = true;
                Assert.assertEquals(host, val);
                Assert.assertEquals(hostDefault, def);

            } else if (name.equals(portName)) {
                foundPort = true;
                Assert.assertEquals(port, val);
                Assert.assertEquals(portDefault, def);

            } else if (name.equals(contextName)) {
                foundContext = true;
                Assert.assertEquals(context, val);
                Assert.assertEquals(contextDefault, def);

            } else if (name.equals(msgBrokerUrlName)) {
                foundMsgBrokerUrl = true;
                Assert.assertEquals(msgBrokerUrl, val);
                Assert.assertEquals(msgBrokerUrlDefault, def);

            } else if (name.equals(usernameName)) {
                foundUsername = true;
                Assert.assertEquals(username, val);
                Assert.assertEquals(usernameDefault, def);

            } else if (name.equals(passwordName)) {
                foundPassword = true;
                Assert.assertEquals(password, val);
                Assert.assertEquals(passwordDefault, def);

            } else {
                Assert.fail("Unexpected config.name: " + name);
            }
        }

        Assert.assertTrue(foundHost);
        Assert.assertTrue(foundPort);
        Assert.assertTrue(foundContext);
        Assert.assertTrue(foundMsgBrokerUrl);
        Assert.assertTrue(foundUsername);
        Assert.assertTrue(foundPassword);

    }

    private ContentStoreManagerUtil createMockContentStoreManagerUtil()
        throws Exception {
        ContentStoreManagerUtil util = EasyMock.createMock(
            "ContentStoreManagerUtil",
            ContentStoreManagerUtil.class);

        ContentStoreManager contentStoreManager = EasyMock.createMock(
            "ContentStoreManager",
            ContentStoreManager.class);
        EasyMock.expect(contentStoreManager.getContentStores()).andReturn(
            contentStores).anyTimes();
        EasyMock.expect(contentStoreManager.getPrimaryContentStore()).andReturn(
            createMockContentStore(0)).anyTimes();
        EasyMock.replay(contentStoreManager);

        EasyMock.expect(util.getContentStoreManager(EasyMock.isA(UserStore.class)))
            .andReturn(contentStoreManager)
            .anyTimes();
        EasyMock.expect(util.getCurrentUser())
            .andReturn(new Credential(username, password))
            .anyTimes();
        EasyMock.replay(util);
        return util;
    }
}
