/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Random;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.junit.BeforeClass;

/**
 * @author: Bill Branan
 * Date: Feb 10, 2010
 */
public abstract class BaseRestTester {

    protected static RestHttpHelper restHelper = RestTestHelper.getAuthorizedRestHelper();    

    protected static String baseUrl;

    protected static String spaceId;

    @BeforeClass
    public static void beforeClass() throws Exception {
        baseUrl = RestTestHelper.getBaseUrl();
    }

    protected void setNewSpaceId() {
        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "durastore-test-rest-" + random;
    }

    /*
     * Checks the response for the expected code, returns the body text
     */
    protected String checkResponse(HttpResponse response,
                                   int expectedCode)
        throws IOException {
        assertNotNull(response);
        String responseText = response.getResponseBody();
        assertEquals(responseText, expectedCode, response.getStatusCode());
        return responseText;
    }

}
