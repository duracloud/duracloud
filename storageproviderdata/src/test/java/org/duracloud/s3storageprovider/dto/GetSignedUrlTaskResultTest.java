/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 3/5/15
 */
public class GetSignedUrlTaskResultTest {

    private final String signedUrl = "signed-url";

    @Test
    public void testSerialize() {
        GetSignedUrlTaskResult taskResult = new GetSignedUrlTaskResult();
        taskResult.setSignedUrl(signedUrl);

        String result = taskResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"signedUrl\":\""+signedUrl+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"signedUrl\" : \""+signedUrl+"\"}";

        GetSignedUrlTaskResult taskResult =
            GetSignedUrlTaskResult.deserialize(resultSerialized);
        assertEquals(signedUrl, taskResult.getSignedUrl());
    }


}
