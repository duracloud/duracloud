/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the Serialization Utilities.
 *
 * @author Bill Branan
 */
public class SerializationUtilTest {

    protected static final Logger log =
        LoggerFactory.getLogger(SerializationUtilTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSerializeDeserializeMap() throws Exception {
        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put("testName", "testValue");
        testMap.put("foo", "bar");
        testMap.put("dura", "cloud.org");

        String serialized =
            SerializationUtil.serializeMap(testMap);
        Map<String, String> resultMap =
            SerializationUtil.deserializeMap(serialized);

        assertTrue(testMap.equals(resultMap));

        // Test empty/null values
        String serNull = SerializationUtil.serializeMap(null);
        String serEmpty =
            SerializationUtil.serializeMap(new HashMap<String, String>());
        assertNotNull(serNull);
        assertNotNull(serEmpty);
        assertEquals(serNull, serEmpty);

        resultMap = SerializationUtil.deserializeMap(serNull);
        assertNotNull(resultMap);
        assertEquals(0, resultMap.size());

        resultMap = SerializationUtil.deserializeMap(serEmpty);
        assertNotNull(resultMap);
        assertEquals(0, resultMap.size());

        resultMap = SerializationUtil.deserializeMap("");
        assertNotNull(resultMap);
        assertEquals(0, resultMap.size());

        resultMap = SerializationUtil.deserializeMap(null);
        assertNotNull(resultMap);
        assertEquals(0, resultMap.size());
    }

    @Test
    public void testSerializeDeserializeList() throws Exception {
        List<String> testList = new ArrayList<String>();
        testList.add("testName");
        testList.add("foo");
        testList.add("dura");

        String serialized =
            SerializationUtil.serializeList(testList);
        List<String> resultList =
            SerializationUtil.deserializeList(serialized);

        assertTrue(testList.equals(resultList));

        // Test empty/null values
        String serNull = SerializationUtil.serializeList(null);
        String serEmpty =
            SerializationUtil.serializeList(new ArrayList<String>());
        assertNotNull(serNull);
        assertNotNull(serEmpty);
        assertEquals(serNull, serEmpty);

        resultList = SerializationUtil.deserializeList(serNull);
        assertNotNull(resultList);
        assertEquals(0, resultList.size());

        resultList = SerializationUtil.deserializeList(serEmpty);
        assertNotNull(resultList);
        assertEquals(0, resultList.size());

        resultList = SerializationUtil.deserializeList("");
        assertNotNull(resultList);
        assertEquals(0, resultList.size());

        resultList = SerializationUtil.deserializeList(null);
        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testSerializeDeserializeSet() throws Exception {
        Set<String> testSet = new HashSet<String>();
        testSet.add("testName");
        testSet.add("foo");
        testSet.add("dura");

        String serialized = SerializationUtil.serializeSet(testSet);
        Set<String> resultSet = SerializationUtil.deserializeSet(serialized);

        assertTrue(testSet.equals(resultSet));

        // Test empty/null values
        String serNull = SerializationUtil.serializeSet(null);
        String serEmpty =
            SerializationUtil.serializeSet(new HashSet<Object>());
        assertNotNull(serNull);
        assertNotNull(serEmpty);
        assertEquals(serNull, serEmpty);

        resultSet = SerializationUtil.deserializeSet(serNull);
        assertNotNull(resultSet);
        assertEquals(0, resultSet.size());

        resultSet = SerializationUtil.deserializeSet(serEmpty);
        assertNotNull(resultSet);
        assertEquals(0, resultSet.size());

        resultSet = SerializationUtil.deserializeSet("");
        assertNotNull(resultSet);
        assertEquals(0, resultSet.size());

        resultSet = SerializationUtil.deserializeSet(null);
        assertNotNull(resultSet);
        assertEquals(0, resultSet.size());
    }

}
