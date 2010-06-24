/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.junit.Before;
import org.junit.Test;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.durastore.rest.RestTestHelper;
import org.apache.commons.httpclient.HttpStatus;

import junit.framework.TestCase;

/**
 * Tests the AOP to verify creation storage provider calls
 *
 * @author Bill Branan
 */
public class TestVerifyCreationAdvice extends TestCase {

    private static RestHttpHelper restHelper = RestTestHelper.getAuthorizedRestHelper();
    private static String baseUrl;

    // NOTE: The store-id maps to a provider from StorageProviderTypes, and its
    //       position in the order of that enumeration (minus one for providers
    //       over the position of UNKNOWN).
    private static final String STORE_ID = "5"; // TEST_VERIFY_CREATE

    // Should be set to the same value as maxRetries in Verify*Advice
    private static final int MAX_RETRIES = 3;

    @Override
    @Before
    public void setUp() throws Exception {
        baseUrl = RestTestHelper.getBaseUrl();

        // Initialize the Instance
        HttpResponse response = RestTestHelper.initialize();
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void testVerifySpaceCreation() throws Exception {
        // Tests for verification checks up to the maximum number of failures
        for(int i=0; i<MAX_RETRIES; i++) {
            String url = baseUrl + "/00" + i + "?storeID=" + STORE_ID;
            HttpResponse response = restHelper.put(url, null, null);
            assertEquals(HttpStatus.SC_CREATED, response.getStatusCode());
        }

        // Not testing at max retries because that begins to overlap with retry advice
    }

}