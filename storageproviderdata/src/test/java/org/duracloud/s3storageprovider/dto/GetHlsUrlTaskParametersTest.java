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
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.duracloud.error.TaskDataException;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: Aug 16, 2018
 */
public class GetHlsUrlTaskParametersTest {

    private final String spaceId = "space-id";
    private final String contentId = "content-id";

    @Test
    public void testSerialize() {
        GetHlsUrlTaskParameters taskParams = new GetHlsUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\"" + spaceId + "\""));
        assertThat(cleanResult, containsString("\"contentId\":\"" + contentId + "\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \"" + spaceId + "\"," +
            " \"contentId\" : \"" + contentId + "\"}";

        GetHlsUrlTaskParameters taskParams =
            GetHlsUrlTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(contentId, taskParams.getContentId());

        // Verify that empty spaceId will throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"contentId\" : \"" + contentId + "\"}";

        try {
            GetHlsUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }

        // Verify that empty contentId will throw
        taskParamsSerialized =
            "{\"spaceId\" : \"" + spaceId + "\"," +
            " \"contentId\" : \"\"}";

        try {
            GetHlsUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"contentId\" : \"\"}";

        try {
            GetHlsUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }

        // Verify that empty params throw
        try {
            GetHlsUrlTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }
    }

}
