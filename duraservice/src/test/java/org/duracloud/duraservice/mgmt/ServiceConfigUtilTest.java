/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.duracloud.duraservice.domain.UserStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.common.model.Credential;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Apr 2, 2010
 */
public class ServiceConfigUtilTest {

    private ServiceConfigUtil util;
    private ContentStoreManagerUtil contentStoreManagerUtil;

    private UserStore userStore;
    private List<SystemConfig> systemConfig;

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
        contentStoreManagerUtil = createMockContentStoreManagerUtil();

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

    @After
    public void tearDown() {
        contentStoreManagerUtil = null;
        userStore = null;
        systemConfig = null;
    }

    @Test
    public void testPopulateService() {
        Assert.assertTrue("Implementation needed", true);
    }

    @Test
    public void testResolveSystemConfigVars() throws ContentStoreException {
        util = new ServiceConfigUtil(contentStoreManagerUtil);
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
            ContentStoreManagerUtil.class);
        EasyMock.expect(util.getCurrentUser())
            .andReturn(new Credential(username, password))
            .anyTimes();
        EasyMock.replay(util);
        return util;
    }
}
