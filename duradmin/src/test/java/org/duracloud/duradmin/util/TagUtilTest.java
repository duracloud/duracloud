/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TagUtilTest {

    private Map<String, String> metadata;

    @Before
    public void setup() {
        metadata = new HashMap<String, String>();
        metadata.put(TagUtil.TAGS, "test1" + TagUtil.DELIMITER + "test2");

    }

    @Test
    public void testRemoveTag() {

        Assert.assertTrue(TagUtil.removeTag("test1", metadata));
        Assert.assertFalse(metadata.get(TagUtil.TAGS).contains("test1"));

    }

    @Test
    public void testAddTag() {
        Assert.assertTrue(TagUtil.addTag("test3", metadata));
        Assert.assertFalse(TagUtil.addTag("test3", metadata));
        Assert.assertTrue(metadata.get(TagUtil.TAGS).contains("test3"));
        Assert.assertTrue(metadata.get(TagUtil.TAGS).contains("test2"));

    }

}
