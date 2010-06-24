/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice;

import org.junit.Test;

import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.duraservice.rest.RestTestHelper;

import junit.framework.TestCase;

/**
 * Provides a way to initialize DuraService with values
 * from the test database without needing to run the
 * full test suite.
 *
 * @author Bill Branan
 */
public class Init
       extends TestCase {

    @Test
    public void testInit() throws Exception {
        HttpResponse response = RestTestHelper.initialize();
        assertEquals(200, response.getStatusCode());
    }
}