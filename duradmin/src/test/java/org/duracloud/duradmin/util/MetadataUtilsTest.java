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


public class MetadataUtilsTest {
    private Map<String,String> metadata;
    
    @Before
    public void setUp() throws Exception {
        metadata = new HashMap<String,String>();
        metadata.put(MetadataUtils.NAME_KEY_PREFIX+"test-key", "test-value");

    }



    @Test
    public void testConvertExtendedMetadata() {
        List<NameValuePair> list = MetadataUtils.convertExtendedMetadata(metadata);
        assertEquals("test-key", list.get(0).getName());
        
    }

    @Test
    public void testRemove() {
        assertNotNull(MetadataUtils.remove("test-key", metadata));
    }

    @Test
    public void testAdd() {
        assertNull(MetadataUtils.add("test-key-2","test-value2", metadata));
        assertNotNull(MetadataUtils.add("test-key","test-value-3", metadata));
        assertEquals("test-value-3", MetadataUtils.getValue("test-key", metadata));
    }

}
