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
 * Date: 3/5/15
 */
public class EnableStreamingTaskResultTest {

    private final String resultValue = "task-result";
    private final String streamingHost = "streaming.host";

    @Test
    public void testSerialize() {
        EnableStreamingTaskResult taskResult = new EnableStreamingTaskResult();
        taskResult.setResult(resultValue);
        taskResult.setStreamingHost(streamingHost);

        String result = taskResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"result\":\"" + resultValue + "\""));
        assertThat(cleanResult, containsString("\"streamingHost\":\"" + streamingHost + "\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"result\" : \"" + resultValue + "\"," +
                                  "\"streamingHost\":\"" + streamingHost + "\"}";

        EnableStreamingTaskResult taskResult =
            EnableStreamingTaskResult.deserialize(resultSerialized);
        assertEquals(resultValue, taskResult.getResult());
        assertEquals(streamingHost, taskResult.getStreamingHost());
    }

}
