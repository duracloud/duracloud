/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.HashMap;
import java.util.Map;

import org.duracloud.error.TaskDataException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: Aug 16, 2018
 */
public class SignedCookieDataTest {

    private final String cloudfrontPolicyKey = "CloudFront-Policy";
    private final String cloudfrontPolicyVal = "policy";
    private final String cloudfrontSignatureKey = "CloudFront-Signature";
    private final String cloudfrontSignatureVal = "signature";
    private final String cloudfrontKeyPairIdKey = "CloudFront-Key-Pair-Id";
    private final String cloudfrontKeyPairIdVal = "keypairId";

    private final Map<String, String> signedCookies = new HashMap<>();
    private final String streamingHost = "streaming-host";
    private final String redirectUrl = "redirect.url";

    @Before
    public void setup() {
        signedCookies.put(cloudfrontPolicyKey, cloudfrontPolicyVal);
        signedCookies.put(cloudfrontSignatureKey, cloudfrontSignatureVal);
        signedCookies.put(cloudfrontKeyPairIdKey, cloudfrontKeyPairIdVal);
    }

    @Test
    public void testSerialize() {
        SignedCookieData taskParams = new SignedCookieData();
        taskParams.setSignedCookies(signedCookies);
        taskParams.setStreamingHost(streamingHost);
        taskParams.setRedirectUrl(redirectUrl);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"signedCookies\":{\"" +
                                  cloudfrontPolicyKey + "\":\"" + cloudfrontPolicyVal + "\",\"" +
                                  cloudfrontSignatureKey + "\":\"" + cloudfrontSignatureVal + "\",\"" +
                                  cloudfrontKeyPairIdKey + "\":\"" + cloudfrontKeyPairIdVal + "\"}"));
        assertThat(cleanResult, containsString("\"streamingHost\":\"" + streamingHost + "\""));
        assertThat(cleanResult, containsString("\"redirectUrl\":\"" + redirectUrl + "\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"signedCookies\":{\"" +
                cloudfrontPolicyKey + "\":\"" + cloudfrontPolicyVal + "\",\"" +
                cloudfrontSignatureKey + "\":\"" + cloudfrontSignatureVal + "\",\"" +
                cloudfrontKeyPairIdKey + "\":\"" + cloudfrontKeyPairIdVal + "\"}," +
            " \"streamingHost\" : \"" + streamingHost + "\"," +
            " \"redirectUrl\" : \"" + redirectUrl + "\"}";

        SignedCookieData taskParams =
            SignedCookieData.deserialize(taskParamsSerialized);
        Map<String, String> signedCookies = taskParams.getSignedCookies();
        assertNotNull(signedCookies);
        assertEquals(cloudfrontPolicyVal, signedCookies.get(cloudfrontPolicyKey));
        assertEquals(cloudfrontSignatureVal, signedCookies.get(cloudfrontSignatureKey));
        assertEquals(cloudfrontKeyPairIdVal, signedCookies.get(cloudfrontKeyPairIdKey));

        assertEquals(streamingHost, taskParams.getStreamingHost());
        assertEquals(redirectUrl, taskParams.getRedirectUrl());

        // Verify that empty params throw
        try {
            SignedCookieData.deserialize("");
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }
    }

}
