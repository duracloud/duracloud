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
 * @author Daniel Bernstein
 *         Date: 9/22/15
 */
public class CompleteCancelSnapshotTaskParametersTest {

    @Test
    public void testSerialize() {
        String spaceId = "space-id";

        CompleteCancelSnapshotTaskParameters taskParams =
            new CompleteCancelSnapshotTaskParameters(spaceId);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId +"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"spaceId\" : \"test-space\"}";

        CompleteCancelSnapshotTaskParameters taskParams =
            CompleteCancelSnapshotTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("test-space", taskParams.getSpaceId());

        // Verify that empty params throw
        taskParamsSerialized = "{\"spaceId\" : \"\"}";

        try {
            CompleteSnapshotTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            CompleteSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
