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
 * Date: 8/14/14
 */
public class CleanupSnapshotTaskParametersTest {

    @Test
    public void testSerialize() {
        String spaceId = "space-id";

        CleanupSnapshotTaskParameters taskParams =
            new CleanupSnapshotTaskParameters();
        taskParams.setSpaceId(spaceId);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\"" + spaceId + "\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"spaceId\" : \"test-space\"}";

        CleanupSnapshotTaskParameters taskParams =
            CleanupSnapshotTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("test-space", taskParams.getSpaceId());

        // Verify that empty params throw
        taskParamsSerialized = "{\"spaceId\" : \"\"}";

        try {
            CleanupSnapshotTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch (SnapshotDataException e) {
            // Expected exception
        }

        // Verify that empty params throw
        try {
            CleanupSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch (SnapshotDataException e) {
            // Expected exception
        }
    }

}
