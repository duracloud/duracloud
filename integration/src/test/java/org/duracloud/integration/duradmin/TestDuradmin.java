/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.duradmin;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Runtime test of Duradmin. The durastore web application must be deployed and
 * available in order for these tests to pass.
 *
 * @author Bill Branan
 */
public class TestDuradmin extends DuradminTestBase {

    private static RestHttpHelper restHelper = RestTestHelper.getAuthorizedRestHelper();

    @Test
    public void testSpaces() throws Exception {
        String url = getBaseUrl() + "/login";
        HttpResponse response = restHelper.get(url);
        Assert.assertEquals(200, response.getStatusCode());

        String responseText = response.getResponseBody();
        Assert.assertNotNull(responseText);
    }

}