/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.junit.Before;
import org.junit.Test;


public class ServiceInfoUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testApplyValuesTextUserConfig() {
        TextUserConfig tuc = new TextUserConfig("test","Test");
        tuc.setValue("testValue1");
        assertEquals("testValue1", tuc.getValue());
        Map<String,String> params = new HashMap<String,String>();
        params.put("test", "testValue1");
        assertFalse(ServiceInfoUtil.applyValues(tuc, params));
        params.put("test", "testValue2");
        assertTrue(ServiceInfoUtil.applyValues(tuc, params));
    }

    @Test
    public void testApplyValuesSingleSelectUserConfig() {
        List<Option> options = new LinkedList<Option>();
        options.add(new Option("Value 1", "1", false));
        options.add(new Option("Value 2", "2", false));
        options.add(new Option("Value 3", "3", false));
        SingleSelectUserConfig uc = new SingleSelectUserConfig("test","Test", options);

        assertNull(uc.getSelectedValue());
        uc.select("2");
        assertEquals("2", uc.getSelectedValue());
        
        Map<String,String> params = new HashMap<String,String>();
        params.put("test", "1");
        assertTrue(ServiceInfoUtil.applyValues(uc, params));
        assertFalse(ServiceInfoUtil.applyValues(uc, params));
    }

    @Test
    public void testApplyValueMultiSelectUserConfig() {
        List<Option> options = new LinkedList<Option>();
        options.add(new Option("Value 1", "1", false));
        options.add(new Option("Value 2", "2", false));
        options.add(new Option("Value 3", "3", false));
        MultiSelectUserConfig uc = new MultiSelectUserConfig("test","Test", options);

        Map<String,String> params = new HashMap<String,String>();
        params.put("test-checkbox-0", "checked");
        params.put("test-checkbox-1", "checked");
        params.put("test-checkbox-2", "checked");
        assertTrue(ServiceInfoUtil.applyValues(uc, params));

        params.remove("test-checkbox-2");
        assertTrue(ServiceInfoUtil.applyValues(uc, params));
        assertFalse(ServiceInfoUtil.applyValues(uc, params));
    }

}
