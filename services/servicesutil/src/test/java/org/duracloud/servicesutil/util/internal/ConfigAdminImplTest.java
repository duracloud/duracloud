/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import java.io.IOException;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;

import org.easymock.EasyMock;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import junit.framework.Assert;

/**
 * @author Andrew Woods
 *         Date: Jan 1, 2010
 */
public class ConfigAdminImplTest {

    @SuppressWarnings("unchecked")
    private static Dictionary dictionary = new Properties();

    private DuraConfigAdminImpl configAdmin;

    private final String PID = "dummyConfigPID";

    @Before
    public void setUp() throws Exception {
        configAdmin = new DuraConfigAdminImpl();
        configAdmin.setOsgiConfigAdmin(createMockConfigurationAdmin());
    }

    @Test
    public void testUpdateConfiguration() throws Exception {
        Map<String, String> props = configAdmin.getConfiguration(PID);
        verifySize(props, 0);

        // Push in new props and verify.
        Map<String, String> newPropsV0 = new HashMap<String, String>();
        newPropsV0.put("key0", "newVal0");
        newPropsV0.put("key1", "newVal1");
        newPropsV0.put("key2", "newVal2");
        int numPropsV0 = newPropsV0.size();

        configAdmin.updateConfiguration(PID, newPropsV0);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, numPropsV0);

        verifyFirstFoundInSecond(newPropsV0, props, true);

        // Push updates to new props and verify.
        Map<String, String> newPropsV1 = new HashMap<String, String>();
        newPropsV1.put("key0", "newValA");
        newPropsV1.put("key1", "newValB");
        newPropsV1.put("key2", "newValC");
        int numPropsV1 = newPropsV1.size();

        configAdmin.updateConfiguration(PID, newPropsV1);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, numPropsV1);

        verifyFirstFoundInSecond(newPropsV1, props, true);
        verifyFirstFoundInSecond(newPropsV0, props, false);

        // Append some additional props and verify.
        Map<String, String> additionalProps = new HashMap<String, String>();
        additionalProps.put("keyX", "newValX");
        additionalProps.put("keyY", "newValY");
        additionalProps.put("keyZ", "newValZ");

        configAdmin.updateConfiguration(PID, additionalProps);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, additionalProps.size());

        verifyFirstFoundInSecond(newPropsV1, props, false);
        verifyFirstFoundInSecond(additionalProps, props, true);

        // Modify subset of existing props and verify.
        Map<String, String> newPropsV2 = new HashMap<String, String>();
        newPropsV2.put("key0", "newValFinal0");
        newPropsV2.put("key2", "newValFinal2");
        newPropsV2.put("keyY", "newValFinalY");

        configAdmin.updateConfiguration(PID, newPropsV2);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, newPropsV2.size());

        verifyFirstFoundInSecond(newPropsV2, props, true);
    }

    private void verifySize(Map<String, String> props, int num) {
        Assert.assertNotNull(props);
        Assert.assertEquals(num, props.size());
    }

    private void verifyFirstFoundInSecond(Map<String, String> firstProps,
                                          Map<String, String> secondProps,
                                          boolean expected) {
        for (String key : firstProps.keySet()) {
            String val = firstProps.get(key);
            Assert.assertEquals(expected, val.equals(secondProps.get(key)));
        }
    }

    private ConfigurationAdmin createMockConfigurationAdmin()
        throws IOException {
        Configuration mockConfiguration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(mockConfiguration.getProperties())
            .andAnswer(getProps())
            .anyTimes();
        mockConfiguration.update(isConfigUpdate());
        EasyMock.expect(EasyMock.expectLastCall()).anyTimes();

        ConfigurationAdmin ca = EasyMock.createMock(ConfigurationAdmin.class);
        EasyMock.expect(ca.getConfiguration(EasyMock.isA(String.class)))
            .andReturn(mockConfiguration)
            .anyTimes();

        EasyMock.replay(mockConfiguration);
        EasyMock.replay(ca);
        return ca;
    }

    /**
     * This class is an EasyMock helper that sets the dictionary with update calls.
     */
    private static class ConfigUpdateMatcher implements IArgumentMatcher {

        public boolean matches(Object o) {
            if (null == o || !(o instanceof Dictionary)) {
                return false;
            } else {
                dictionary = (Dictionary) o;
            }
            return true;
        }

        public void appendTo(StringBuffer stringBuffer) {
            stringBuffer.append(ConfigUpdateMatcher.class.getCanonicalName());
        }
    }

    /**
     * This method registers the EasyMock helper.
     *
     * @return
     */
    private static Dictionary isConfigUpdate() {
        EasyMock.reportMatcher(new ConfigUpdateMatcher());
        return null;
    }

    /**
     * This mock method returns the current dictionary.
     *
     * @return Dictionary
     */
    private IAnswer<? extends Dictionary> getProps() {
        return new IAnswer<Dictionary>() {
            public Dictionary answer() throws Throwable {
                return dictionary;
            }
        };
    }

}
