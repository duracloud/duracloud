/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.snapshot.dto.SnapshotStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 7/25/14
 */
public class CreateSnapshotTaskResultTest {

    @Test
    public void testSerialize() {
        String snapshotId = "snapshot-id";
        SnapshotStatus status = SnapshotStatus.INITIALIZED;

        CreateSnapshotTaskResult taskResult = new CreateSnapshotTaskResult();
        taskResult.setSnapshotId(snapshotId);
        taskResult.setStatus(status);

        String result = taskResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"snapshotId\":\""+snapshotId+"\""));
        assertThat(cleanResult,
                   containsString(
                       "\"status\":\""+SnapshotStatus.INITIALIZED.name()+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"snapshotId\" : \"snapshot-id\"," +
                                    "\"status\":\"" + SnapshotStatus.INITIALIZED +"\"}";

        CreateSnapshotTaskResult taskResult =
            CreateSnapshotTaskResult.deserialize(resultSerialized);
        assertEquals("snapshot-id", taskResult.getSnapshotId());
        assertEquals(SnapshotStatus.INITIALIZED, taskResult.getStatus());
    }


}
