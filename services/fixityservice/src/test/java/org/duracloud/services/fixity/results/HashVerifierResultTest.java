/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import org.duracloud.services.fixity.domain.ContentLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Aug 12, 2010
 */
public class HashVerifierResultTest {

    private HashVerifierResult result;
    private boolean success = true;
    private String text = "...result...text...";
    private ContentLocation contentLocationA = new ContentLocation("space-a",
                                                                   "content-a");
    private ContentLocation contentLocationB = new ContentLocation("space-b",
                                                                   "content-b");


    @Before
    public void setUp() {
        result = new HashVerifierResult(success,
                                        contentLocationA,
                                        contentLocationB,
                                        text);
    }

    @Test
    public void testGetEntry() throws Exception {
        String entry = result.getEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(text, entry);
    }

    @Test
    public void testGetHeader() throws Exception {
        String header = result.getHeader();
        Assert.assertNotNull(header);

        String locA = contentLocationA.getSpaceId() + "-" +
            contentLocationA.getContentId();
        String locB = contentLocationB.getSpaceId() + "-" +
            contentLocationB.getContentId();
        String h =
            "space-id,content-id,0:" + locA + "," + "1:" + locB + ",status";
        Assert.assertEquals(h, header);
    }

    @Test
    public void testIsSuccess() throws Exception {
        Assert.assertEquals(success, result.isSuccess());
    }
}
