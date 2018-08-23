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
public class GetSignedCookiesUrlTaskParametersTest {

    private final String spaceId = "space-id";
    private final int minutesToExpire = 60;
    private final String ipAddress = "ip-address";
    private final String redirectUrl = "redirect.url";

    @Test
    public void testSerialize() {
        GetSignedCookiesUrlTaskParameters taskParams =
            new GetSignedCookiesUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setMinutesToExpire(minutesToExpire);
        taskParams.setIpAddress(ipAddress);
        taskParams.setRedirectUrl(redirectUrl);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\"" + spaceId + "\""));
        assertThat(cleanResult, containsString("\"minutesToExpire\":" + minutesToExpire));
        assertThat(cleanResult, containsString("\"ipAddress\":\"" + ipAddress + "\""));
        assertThat(cleanResult, containsString("\"redirectUrl\":\"" + redirectUrl + "\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \"" + spaceId + "\"," +
            " \"minutesToExpire\" : " + minutesToExpire + "," +
            " \"ipAddress\" : \"" + ipAddress + "\"," +
            " \"redirectUrl\" : \"" + redirectUrl + "\"}";

        GetSignedCookiesUrlTaskParameters taskParams =
            GetSignedCookiesUrlTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(minutesToExpire, taskParams.getMinutesToExpire());
        assertEquals(ipAddress, taskParams.getIpAddress());
        assertEquals(redirectUrl, taskParams.getRedirectUrl());

        // Verify that empty spaceId will throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"minutesToExpire\" : " + minutesToExpire + "," +
            " \"ipAddress\" : \"" + ipAddress + "\"," +
            " \"redirectUrl\" : \"" + redirectUrl + "\"}";

        try {
            GetSignedCookiesUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }

        // Verify that empty redirectUrl will throw
        taskParamsSerialized =
            "{\"spaceId\" : \"" + spaceId + "\"," +
            " \"minutesToExpire\" : " + minutesToExpire + "," +
            " \"ipAddress\" : \"" + ipAddress + "\"," +
            " \"redirectUrl\" : \"\"}";

        try {
            GetSignedCookiesUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"minutesToExpire\" : \"\"," +
            " \"ipAddress\" : \"\"," +
            " \"redirectUrl\" : \"\"}";

        try {
            GetSignedCookiesUrlTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }

        // Verify that empty params throw
        try {
            GetSignedCookiesUrlTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch (TaskDataException e) {
            // Expected exception
        }
    }

}
