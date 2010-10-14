/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Oct 13, 2010
 */
public class MimeTypeUtilTest {

    private MimeTypeUtil util;

    private static final String contentId0 = "dir/dir/file.txt";
    private static final String contentId1 = "file.file.pdf";
    private static final String contentId2 = "file.jpg";
    private static final String contentId3 = "file.tiff";
    private static final String contentId4 = "file.out";
    private static final String contentId5 = "file";

    private static final String mime0 = "text/plain";
    private static final String mime1 = "application/pdf";
    private static final String mime2 = "image/jpeg";
    private static final String mime3 = "image/tiff";
    private static final String mime4 = "application/octet-stream";
    private static final String mime5 = "application/octet-stream";

    @Before
    public void setUp() throws Exception {
        util = new MimeTypeUtil();
    }

    @Test
    public void testGuessMimeType() throws Exception {
        verifyMime(util.guessMimeType(contentId0), mime0);
        verifyMime(util.guessMimeType(contentId1), mime1);
        verifyMime(util.guessMimeType(contentId2), mime2);
        verifyMime(util.guessMimeType(contentId3), mime3);
        verifyMime(util.guessMimeType(contentId4), mime4);
        verifyMime(util.guessMimeType(contentId5), mime5);

    }

    private void verifyMime(String mime, String expectedMime) {
        Assert.assertNotNull(mime);
        Assert.assertEquals(expectedMime, mime);
    }
}
