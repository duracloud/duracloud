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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.easymock.EasyMock;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import junit.framework.Assert;

public class ConfigAdminImplTest {

    @SuppressWarnings("unchecked")
    private Dictionary dictionary;

    private DuraConfigAdminImpl configAdmin;

    private final String PID = "dummyConfigPID";

    @Before
    public void setUp() throws Exception {
        dictionary = new Properties();
        configAdmin = new DuraConfigAdminImpl();
        configAdmin.setOsgiConfigAdmin(createMockConfigurationAdmin());
    }

    @After
    public void tearDown() throws Exception {
    }

    private ConfigurationAdmin createMockConfigurationAdmin()
            throws IOException {
        Configuration mockConfiguration =
                EasyMock.createMock(Configuration.class);
        EasyMock.expect(mockConfiguration.getProperties())
                .andReturn(dictionary).anyTimes();
        mockConfiguration.update(dictionary);
        EasyMock.expect(EasyMock.expectLastCall()).anyTimes();

        ConfigurationAdmin ca = EasyMock.createMock(ConfigurationAdmin.class);
        EasyMock.expect(ca.getConfiguration(EasyMock.isA(String.class)))
                .andReturn(mockConfiguration).anyTimes();

        EasyMock.replay(mockConfiguration);
        EasyMock.replay(ca);
        return ca;
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
        int numAdditionalProps = additionalProps.size();

        configAdmin.updateConfiguration(PID, additionalProps);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, numPropsV1 + numAdditionalProps);

        verifyFirstFoundInSecond(newPropsV1, props, true);
        verifyFirstFoundInSecond(additionalProps, props, true);

        // Modify subset of existing props and verify.
        Map<String, String> newPropsV2 = new HashMap<String, String>();
        newPropsV2.put("key0", "newValFinal0");
        newPropsV2.put("key2", "newValFinal2");
        newPropsV2.put("keyY", "newValFinalY");
        int numPropsV2 = newPropsV2.size();

        configAdmin.updateConfiguration(PID, newPropsV2);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, numPropsV2 + numAdditionalProps);

        verifyFirstFoundInSecond(newPropsV2, props, true);
    }

    @Test
    public void testRemoveConfigurationElements() throws Exception {
        Map<String, String> props = configAdmin.getConfiguration(PID);
        verifySize(props, 0);

        Map<String, String> newProps = new HashMap<String, String>();
        newProps.put("key0", "newVal0");
        newProps.put("key1", "newVal1");
        newProps.put("key2", "newVal2");
        int numProps = newProps.size();

        configAdmin.updateConfiguration(PID, newProps);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, numProps);

        verifyFirstFoundInSecond(newProps, props, true);

        Map<String, String> propsToDelete = new HashMap<String, String>();
        propsToDelete.put("key0", "x");
        propsToDelete.put("key2", "x");
        int numRemainingProps = numProps - propsToDelete.size();

        configAdmin.removeConfigurationElements(PID, propsToDelete);
        props = configAdmin.getConfiguration(PID);
        verifySize(props, numRemainingProps);

        verifyFirstFoundInSecond(props, newProps, true);
        verifyFirstFoundInSecond(props, propsToDelete, false);
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

}
