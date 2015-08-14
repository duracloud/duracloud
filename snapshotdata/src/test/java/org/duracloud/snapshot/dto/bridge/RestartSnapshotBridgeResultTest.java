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
        String description = "description";
        SnapshotStatus status = SnapshotStatus.INITIALIZED;

        RestartSnapshotBridgeResult params =
            new RestartSnapshotBridgeResult(description, status);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"description\":\""+description+"\""));
        assertThat(cleanResult, containsString("\"status\":\""+status.name()+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"description\" : \"description\"," +
                                    "\"status\":\"" + SnapshotStatus.INITIALIZED +"\"}";

        RestartSnapshotBridgeResult bridgeResult =
            RestartSnapshotBridgeResult.deserialize(resultSerialized);
        assertEquals("description", bridgeResult.getDescription());
        assertEquals(SnapshotStatus.INITIALIZED, bridgeResult.getStatus());
    }

}
