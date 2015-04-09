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
    private final int minutesToExpire = 60;
    private final String ipAddress = "ip-address";

    @Test
    public void testSerialize() {
        GetSignedUrlTaskParameters taskParams =
            new GetSignedUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);
        taskParams.setResourcePrefix(resourcePrefix);
        taskParams.setMinutesToExpire(minutesToExpire);
        taskParams.setIpAddress(ipAddress);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId+"\""));
        assertThat(cleanResult, containsString("\"contentId\":\""+contentId+"\""));
        assertThat(cleanResult, containsString("\"resourcePrefix\":\""+resourcePrefix+"\""));
        assertThat(cleanResult, containsString("\"minutesToExpire\":"+ minutesToExpire));
        assertThat(cleanResult, containsString("\"ipAddress\":\""+ipAddress+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \""+spaceId+"\"," +
            " \"contentId\" : \""+contentId+"\"," +
            " \"resourcePrefix\" : \""+resourcePrefix+"\"," +
            " \"minutesToExpire\" : "+ minutesToExpire +"," +
            " \"ipAddress\" : \""+ipAddress+"\"}";

        GetSignedUrlTaskParameters taskParams =
            GetSignedUrlTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(contentId, taskParams.getContentId());
        assertEquals(resourcePrefix, taskParams.getResourcePrefix());
        assertEquals(minutesToExpire, taskParams.getMinutesToExpire());
        assertEquals(ipAddress, taskParams.getIpAddress());

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"contentId\" : \"\"," +
            " \"resourcePrefix\" : \"\"," +
            " \"minutesToExpire\" : \"\"," +
            " \"ipAddress\" : \"\"}";

        try {
            GetSignedUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }

        // Verify that empty params throw
        try {
            GetSignedUrlTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }
    }

}
