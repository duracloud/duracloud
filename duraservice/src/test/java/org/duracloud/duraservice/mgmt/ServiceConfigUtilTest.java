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
import org.duracloud.serviceconfig.xml.ServiceDocumentBinding;
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
    private final String modeSetNameIn = "mode.name.in";
    private final String modeSetNameOut = "mode.name.out";

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

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private List<UserConfigModeSet> createModeSets() {
        UserConfigMode mode = new UserConfigMode();
        mode.setDisplayName(ServiceConfigUtil.ALL_STORE_SPACES_VAR);

        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        modes.add(mode);

        UserConfigModeSet modeSetIn = new UserConfigModeSet();
        modeSetIn.setName(modeSetNameIn);
        modeSetIn.setModes(modes);

        UserConfigModeSet modeSetOut = new UserConfigModeSet();
        modeSetOut.setName(modeSetNameOut);
        modeSetOut.setModes(modes);

        modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(modeSetIn);
        modeSets.add(modeSetOut);

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

        Assert.assertEquals(2, testModeSets.size());
        boolean foundIn = false;
        boolean foundOut = false;
        for (UserConfigModeSet testModeSet : testModeSets) {
            String name = testModeSet.getName();
            Assert.assertNotNull("mode set name is null", name);

            if (modeSetNameIn.equals(name)) {
                foundIn = true;
            } else if (modeSetNameOut.equals(name)) {
                foundOut = true;
            }

            List<UserConfigMode> testModes = testModeSet.getModes();
            Assert.assertNotNull("mode list is null", testModes);

            Assert.assertEquals(NUM_CONTENT_STORES, testModes.size());
            for (UserConfigMode testMode : testModes) {
                String modeName = testMode.getDisplayName();
                Assert.assertNotNull("mode name is null", modeName);
                Assert.assertEquals(StorageProviderType.AMAZON_S3.name(),
                                    modeName);

                List<UserConfig> userConfigs = testMode.getUserConfigs();
                Assert.assertNotNull(userConfigs);

                Assert.assertEquals(1, userConfigs.size());
                for (UserConfig userConfig : userConfigs) {
                    String configName = userConfig.getName();
                    String configDisplayName = userConfig.getDisplayName();
                    Assert.assertNotNull(configName);
                    Assert.assertNotNull(configDisplayName);

                    Assert.assertEquals(configName, configDisplayName);
                    Assert.assertTrue(configName,
                                      configName.startsWith(modeName));

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

                }

                Assert.assertNull(testMode.getUserConfigModeSets());
            }

        }

        Assert.assertTrue("mode-in not found", foundIn);
        Assert.assertTrue("mode-out not found", foundOut);
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
            ContentStoreManager.class);
        EasyMock.expect(contentStoreManager.getContentStores()).andReturn(
            contentStores).anyTimes();
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
