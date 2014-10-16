/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class ContentIdUtilTest {

    @Test
    public void testGetContentId() throws Exception {

        File watchDir = new File("a");
        File file = new File("a/b/c", "file.txt");

        // Get Content ID with a watch dir
        String contentId = ContentIdUtil.getContentId(file, watchDir, null);
        Assert.assertEquals("b/c/file.txt", contentId);

        // Get Content ID with now watch dir
        contentId = ContentIdUtil.getContentId(file, null, null);
        Assert.assertEquals("file.txt", contentId);
    }

    @Test
    public void testGetContentIdPrefix() throws Exception {
        String prefix = "prefix/";

        File watchDir = new File("a");
        File file = new File("a/b/c", "file.txt");

        // Get Content ID with a watch dir
        String contentId = ContentIdUtil.getContentId(file, watchDir, prefix);
        Assert.assertEquals(prefix + "b/c/file.txt", contentId);

        // Get Content ID with now watch dir
        contentId = ContentIdUtil.getContentId(file, null, prefix);
        Assert.assertEquals(prefix + "file.txt", contentId);
    }

}
