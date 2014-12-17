/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.snapshot.error.SnapshotDataException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 7/25/14
 */
public class CreateSnapshotTaskParametersTest {

    @Test
    public void testSerialize() {
        String spaceId = "space-id";
        String description = "descrip";
        String userEmail = "user-email";

        CreateSnapshotTaskParameters taskParams =
            new CreateSnapshotTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setDescription(description);
        taskParams.setUserEmail(userEmail);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId +"\""));
        assertThat(cleanResult,
                   containsString("\"description\":\""+description +"\""));
        assertThat(cleanResult,
                   containsString("\"userEmail\":\""+userEmail +"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \"test-space\"," +
            " \"description\" : \"test snapshot\"," +
            " \"userEmail\" : \"yo@myemail.com\"}";

        CreateSnapshotTaskParameters taskParams =
            CreateSnapshotTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("test-space", taskParams.getSpaceId());
        assertEquals("test snapshot", taskParams.getDescription());
        assertEquals("yo@myemail.com", taskParams.getUserEmail());

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"description\" : \"\"," +
            " \"userEmail\" : \"\"}";

        try {
            CreateSnapshotTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            CreateSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
