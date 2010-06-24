/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Random;

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

        // Initialize the Instance
        RestHttpHelper.HttpResponse response = RestTestHelper.initialize();
        int statusCode = response.getStatusCode();
        assertEquals(HttpStatus.SC_OK, statusCode);
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
