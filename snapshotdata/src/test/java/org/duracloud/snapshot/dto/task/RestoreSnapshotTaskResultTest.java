/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.snapshot.dto.RestoreStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 7/30/14
 */
public class RestoreSnapshotTaskResultTest {

    @Test
    public void testSerialize() {
        String spaceId = "space-id";
        String restoreId = "restore-id";
        RestoreStatus status = RestoreStatus.INITIALIZED;

        RestoreSnapshotTaskResult taskResult = new RestoreSnapshotTaskResult();
        taskResult.setSpaceId(spaceId);
        taskResult.setRestoreId(restoreId);
        taskResult.setStatus(status);

        String result = taskResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId+"\""));
        assertThat(cleanResult, containsString("\"restoreId\":\""+restoreId+"\""));
        assertThat(cleanResult, containsString("\"status\":\""+status+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"spaceId\" : \"space-id\"," +
                                   "\"restoreId\" : \"restore-id\"," +
                                   "\"status\":\"" +
                                   RestoreStatus.TRANSFERRING_TO_DURACLOUD +"\"}";

        RestoreSnapshotTaskResult taskResult =
            RestoreSnapshotTaskResult.deserialize(resultSerialized);
        assertEquals("space-id", taskResult.getSpaceId());
        assertEquals("restore-id", taskResult.getRestoreId());
        assertEquals(RestoreStatus.TRANSFERRING_TO_DURACLOUD,
                     taskResult.getStatus());
    }

}
