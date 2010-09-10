/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Sep 10, 2010
 */
public class ContentRestTest {

    @Test
    public void testValidMimetype() {
        ContentRest contentRest = new ContentRest(null, null);

        assertTrue(contentRest.validMimetype("text/xml"));
        assertTrue(contentRest.validMimetype("application/xml"));
        assertTrue(contentRest.validMimetype("blah/blah"));

        assertFalse(contentRest.validMimetype("text*xml"));
        assertFalse(contentRest.validMimetype("***"));        
    }

}
