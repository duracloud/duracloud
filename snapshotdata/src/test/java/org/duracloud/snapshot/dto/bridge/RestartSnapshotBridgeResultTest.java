/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.snapshot.dto.SnapshotStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Daniel Bernstein 
 *         Date: 08/07/15
 */
public class RestartSnapshotBridgeResultTest {

    @Test
    public void testSerialize() {
        String snapshotId = "snapshot-id";
        SnapshotStatus status = SnapshotStatus.INITIALIZED;

        RestartSnapshotBridgeResult params =
            new RestartSnapshotBridgeResult(snapshotId, status);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"snapshotId\":\""+snapshotId+"\""));
        assertThat(cleanResult, containsString("\"status\":\""+status.name()+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"snapshotId\" : \"snapshot-id\"," +
                                    "\"status\":\"" + SnapshotStatus.INITIALIZED +"\"}";

        RestartSnapshotBridgeResult bridgeResult =
            RestartSnapshotBridgeResult.deserialize(resultSerialized);
        assertEquals("snapshot-id", bridgeResult.getSnapshotId());
        assertEquals(SnapshotStatus.INITIALIZED, bridgeResult.getStatus());
    }

}
