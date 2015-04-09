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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 3/5/15
 */
public class EnableStreamingTaskParametersTest {

    private final String spaceId = "space-id";
    private final boolean secure = true;

    @Test
    public void testSerialize() {
        EnableStreamingTaskParameters taskParams =
            new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(secure);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId +"\""));
        assertThat(cleanResult, containsString("\"secure\":"+secure +""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \""+spaceId+"\"," +
            " \"secure\" : \""+secure+"\"}";

        EnableStreamingTaskParameters taskParams =
            EnableStreamingTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(secure, taskParams.isSecure());

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"secure\" : \"\"}";

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
