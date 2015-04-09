/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import org.duracloud.error.TaskDataException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 3/23/15
 */
public class GetUrlTaskParametersTest {

    private final String spaceId = "space-id";
    private final String contentId = "content-id";
    private final String resourcePrefix = "resource:";

    @Test
    public void testSerialize() {
        GetUrlTaskParameters taskParams =
            new GetUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);
        taskParams.setResourcePrefix(resourcePrefix);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId+"\""));
        assertThat(cleanResult, containsString("\"contentId\":\""+contentId+"\""));
        assertThat(cleanResult, containsString("\"resourcePrefix\":\""+resourcePrefix+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \""+spaceId+"\"," +
            " \"contentId\" : \""+contentId+"\"," +
            " \"resourcePrefix\" : \""+resourcePrefix+"\"}";

        GetUrlTaskParameters taskParams =
            GetUrlTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(contentId, taskParams.getContentId());
        assertEquals(resourcePrefix, taskParams.getResourcePrefix());

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"contentId\" : \"\"," +
            " \"resourcePrefix\" : \"\"}";

        try {
            GetUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }

        // Verify that empty params throw
        try {
            GetUrlTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }
    }

}
