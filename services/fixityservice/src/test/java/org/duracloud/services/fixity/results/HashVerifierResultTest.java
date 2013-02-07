/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

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

    @Before
    public void setUp() {
        result = new HashVerifierResult(success,
                                        text,
                                        null);
    }

    @Test
    public void testGetEntry() throws Exception {
        String entry = result.getEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(text, entry);
    }

    @Test
    public void testIsSuccess() throws Exception {
        Assert.assertEquals(success, result.isSuccess());
    }
}
