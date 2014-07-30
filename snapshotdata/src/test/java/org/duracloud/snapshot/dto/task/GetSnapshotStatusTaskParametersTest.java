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
import static org.junit.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 7/29/14
 */
public class GetSnapshotStatusTaskParametersTest {

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"snapshotId\" : \"snapshot-id\"}";

        GetSnapshotStatusTaskParameters taskParams =
            GetSnapshotStatusTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("snapshot-id", taskParams.getSnapshotId());

        // Verify that empty params throw
        taskParamsSerialized = "{\"snapshotId\" : \"\"}";

        try {
            GetSnapshotStatusTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            GetSnapshotStatusTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
