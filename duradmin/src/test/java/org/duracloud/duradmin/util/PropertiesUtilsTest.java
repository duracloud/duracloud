/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;


public class PropertiesUtilsTest {
    private Map<String,String> properties;
    
    @Before
    public void setUp() throws Exception {
        properties = new HashMap<String,String>();
        properties.put("test-key", "test-value");

    }



    @Test
    public void testConvertExtendedProperties() {
        List<NameValuePair> list = PropertiesUtils
            .convertExtendedProperties(properties);
        assertEquals("test-key", list.get(0).getName());
        
    }

    @Test
    public void testRemove() {
        assertNotNull(PropertiesUtils.remove("test-key", properties));
    }

    @Test
    public void testAdd() {
        assertNull(PropertiesUtils.add("test-key-2", "test-value2", properties));
        assertNotNull(PropertiesUtils.add("test-key", "test-value-3", properties));
        assertEquals("test-value-3", PropertiesUtils
            .getValue("test-key", properties));
    }

}
