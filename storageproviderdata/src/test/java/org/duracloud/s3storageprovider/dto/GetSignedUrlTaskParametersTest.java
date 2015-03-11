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
 *         Date: 3/5/15
 */
public class GetSignedUrlTaskParametersTest {

    private final String spaceId = "space-id";
    private final String contentId = "content-id";
    private final String resourcePrefix = "resource:";
    private final long dateLessThan = System.currentTimeMillis() + 10000;
    private final long dateGreaterThan = 24L;
    private final String ipAddress = "ip-address";

    @Test
    public void testSerialize() {
        GetSignedUrlTaskParameters taskParams =
            new GetSignedUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);
        taskParams.setResourcePrefix(resourcePrefix);
        taskParams.setDateLessThan(dateLessThan);
        taskParams.setDateGreaterThan(dateGreaterThan);
        taskParams.setIpAddress(ipAddress);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId+"\""));
        assertThat(cleanResult, containsString("\"contentId\":\""+contentId+"\""));
        assertThat(cleanResult, containsString("\"resourcePrefix\":\""+resourcePrefix+"\""));
        assertThat(cleanResult, containsString("\"dateLessThan\":"+dateLessThan+""));
        assertThat(cleanResult, containsString("\"dateGreaterThan\":"+dateGreaterThan+""));
        assertThat(cleanResult, containsString("\"ipAddress\":\""+ipAddress+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \""+spaceId+"\"," +
            " \"contentId\" : \""+contentId+"\"," +
            " \"resourcePrefix\" : \""+resourcePrefix+"\"," +
            " \"dateLessThan\" : "+dateLessThan+"," +
            " \"dateGreaterThan\" : "+dateGreaterThan+"," +
            " \"ipAddress\" : \""+ipAddress+"\"}";

        GetSignedUrlTaskParameters taskParams =
            GetSignedUrlTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(contentId, taskParams.getContentId());
        assertEquals(resourcePrefix, taskParams.getResourcePrefix());
        assertEquals(dateLessThan, taskParams.getDateLessThan());
        assertEquals(dateGreaterThan, taskParams.getDateGreaterThan());
        assertEquals(ipAddress, taskParams.getIpAddress());

        // Verify that an incorrect dateLessThan will throw
        // Verify valid params
        taskParamsSerialized =
            "{\"spaceId\" : \""+spaceId+"\"," +
            " \"contentId\" : \""+contentId+"\"," +
            " \"resourcePrefix\" : \""+resourcePrefix+"\"," +
            " \"dateLessThan\" : "+0+"," +
            " \"dateGreaterThan\" : "+dateGreaterThan+"," +
            " \"ipAddress\" : \""+ipAddress+"\"}";

        try {
            EnableStreamingTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"contentId\" : \"\"," +
            " \"resourcePrefix\" : \"\"," +
            " \"dateLessThan\" : \"\"," +
            " \"dateGreaterThan\" : \"\"," +
            " \"ipAddress\" : \"\"}";

        try {
            EnableStreamingTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }

        // Verify that empty params throw
        try {
            EnableStreamingTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }
    }

}
