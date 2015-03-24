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
 *         Date: 3/23/15
 */
public class GetUrlTaskResultTest {

    private final String streamUrl = "stream-url";

    @Test
    public void testSerialize() {
        GetUrlTaskResult taskResult = new GetUrlTaskResult();
        taskResult.setStreamUrl(streamUrl);

        String result = taskResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"streamUrl\":\""+streamUrl+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"streamUrl\" : \""+streamUrl+"\"}";

        GetUrlTaskResult taskResult =
            GetUrlTaskResult.deserialize(resultSerialized);
        assertEquals(streamUrl, taskResult.getStreamUrl());
    }


}
