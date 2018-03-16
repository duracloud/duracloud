/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.duracloud.snapshot.error.SnapshotDataException;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 7/30/14
 */
public class RestoreSnapshotTaskParametersTest {

    @Test
    public void testSerialize() {
        String snapshotId = "snapshot-id";
        String userEmail = "user-email";

        RestoreSnapshotTaskParameters taskParams =
            new RestoreSnapshotTaskParameters();
        taskParams.setSnapshotId(snapshotId);
        taskParams.setUserEmail(userEmail);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"snapshotId\":\"" + snapshotId + "\""));
        assertThat(cleanResult,
                   containsString("\"userEmail\":\"" + userEmail + "\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"snapshotId\" : \"snapshot-id\"," +
            " \"userEmail\" : \"yo@myemail.com\"}";

        RestoreSnapshotTaskParameters taskParams =
            RestoreSnapshotTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("snapshot-id", taskParams.getSnapshotId());
        assertEquals("yo@myemail.com", taskParams.getUserEmail());

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"snapshotId\" : \"\"," +
            " \"userEmail\" : \"\"}";

        try {
            RestoreSnapshotTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (SnapshotDataException e) {
            // Expected exception
        }

        // Verify that empty params throw
        try {
            RestoreSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch (SnapshotDataException e) {
            // Expected exception
        }
    }

}
