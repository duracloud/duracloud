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
 *         Date: 7/29/14
 */
public class GetSnapshotTaskParametersTest {

    @Test
    public void testSerialize() {
        String snapshotId = "snapshot-id";

        GetSnapshotTaskParameters taskParams =
            new GetSnapshotTaskParameters();
        taskParams.setSnapshotId(snapshotId);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"snapshotId\":\""+snapshotId +"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"snapshotId\" : \"snapshot-id\"}";

        GetSnapshotTaskParameters taskParams =
            GetSnapshotTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("snapshot-id", taskParams.getSnapshotId());

        // Verify that empty params throw
        taskParamsSerialized = "{\"snapshotId\" : \"\"}";

        try {
            GetSnapshotTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            GetSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
