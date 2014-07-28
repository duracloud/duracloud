/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot.dto;

import org.duracloud.storage.error.TaskException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 7/25/14
 */
public class CompleteSnapshotTaskParametersTest {

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"spaceId\" : \"test-space\"}";

        CompleteSnapshotTaskParameters taskParams =
            CompleteSnapshotTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("test-space", taskParams.getSpaceId());

        // Verify that empty params throw
        taskParamsSerialized = "{\"spaceId\" : \"\"}";

        try {
            CompleteSnapshotTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(TaskException e) {
        }

        // Verify that empty params throw
        try {
            CompleteSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(TaskException e) {
        }
    }

}
