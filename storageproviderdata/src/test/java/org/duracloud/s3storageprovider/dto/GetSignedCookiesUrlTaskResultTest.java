/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

/**
 * @author Bill Branan
 * Date: Aug 16, 2018
 */
public class GetSignedCookiesUrlTaskResultTest {

    private final String signedCookiesUrl = "signed.cookies.url";

    @Test
    public void testSerialize() {
        GetSignedCookiesUrlTaskResult taskResult = new GetSignedCookiesUrlTaskResult();
        taskResult.setSignedCookiesUrl(signedCookiesUrl);

        String result = taskResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"signedCookiesUrl\":\"" + signedCookiesUrl + "\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"signedCookiesUrl\" : \"" + signedCookiesUrl + "\"}";

        GetSignedCookiesUrlTaskResult taskResult =
            GetSignedCookiesUrlTaskResult.deserialize(resultSerialized);
        assertEquals(signedCookiesUrl, taskResult.getSignedCookiesUrl());
    }

}
