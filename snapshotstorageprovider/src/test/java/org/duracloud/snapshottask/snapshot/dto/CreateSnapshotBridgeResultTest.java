/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 7/28/14
 */
public class CreateSnapshotBridgeResultTest {

    @Test
    public void testSerialize() {
        String snapshotId = "snapshot-id";
        String status = "status";

        CreateSnapshotBridgeResult params =
            new CreateSnapshotBridgeResult(snapshotId, status);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"snapshotId\":\""+snapshotId+"\""));
        assertThat(cleanResult, containsString("\"status\":\""+status+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized =
            "{\"snapshotId\" : \"snapshot-id\",\"status\":\"snapshot-status\"}";

        CreateSnapshotBridgeResult bridgeResult =
            CreateSnapshotBridgeResult.deserialize(resultSerialized);
        assertEquals("snapshot-id", bridgeResult.getSnapshotId());
        assertEquals("snapshot-status", bridgeResult.getStatus());
    }

}
