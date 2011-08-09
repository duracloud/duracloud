/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TagUtilTest {

    private Map<String, String> properties;

    @Before
    public void setup() {
        properties = new HashMap<String, String>();
        properties.put(TagUtil.TAGS, "test1" + TagUtil.DELIMITER + "test2");

    }

    @Test
    public void testRemoveTag() {

        Assert.assertTrue(TagUtil.removeTag("test1", properties));
        Assert.assertFalse(properties.get(TagUtil.TAGS).contains("test1"));

    }

    @Test
    public void testAddTag() {
        Assert.assertTrue(TagUtil.addTag("test3", properties));
        Assert.assertFalse(TagUtil.addTag("test3", properties));
        Assert.assertTrue(properties.get(TagUtil.TAGS).contains("test3"));
        Assert.assertTrue(properties.get(TagUtil.TAGS).contains("test2"));

    }

}
