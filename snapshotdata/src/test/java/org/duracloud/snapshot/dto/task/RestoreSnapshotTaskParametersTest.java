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
 *         Date: 7/30/14
 */
public class RestoreSnapshotTaskParametersTest {

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
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            RestoreSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
