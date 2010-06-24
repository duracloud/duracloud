/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.util;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;

/**
 * Tests the Storage Provider Utilities.
 *
 * @author Bill Branan
 */
public class StorageProviderUtilTest {

    private static final Logger log =
            LoggerFactory.getLogger(StorageProviderUtilTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLoadStore() throws Exception {
        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put("testName", "testValue");
        testMap.put("foo", "bar");
        testMap.put("dura", "cloud.org");

        InputStream is = StorageProviderUtil.storeMetadata(testMap);
        Map<String, String> resultMap = StorageProviderUtil.loadMetadata(is);

        assertTrue(testMap.equals(resultMap));
    }

    @Test
    public void testContains() throws Exception {
        List<String> testList = new ArrayList<String>();
        assertFalse(StorageProviderUtil.contains(testList.iterator(), "foo"));
        testList.add("foo");
        assertTrue(StorageProviderUtil.contains(testList.iterator(), "foo"));
    }

    @Test
    public void testCount() throws Exception {
        List<String> testList = new ArrayList<String>();
        assertEquals(0, StorageProviderUtil.count(testList.iterator()));
        testList.add("foo");
        testList.add("bar");
        testList.add("baz");
        assertEquals(3, StorageProviderUtil.count(testList.iterator()));
    }

    @Test
    public void testGetList() throws Exception {
        List<String> testList = new ArrayList<String>();
        testList.add("foo");
        testList.add("bar");
        testList.add("baz");
        assertEquals(testList, StorageProviderUtil.getList(testList.iterator()));
    }
}
