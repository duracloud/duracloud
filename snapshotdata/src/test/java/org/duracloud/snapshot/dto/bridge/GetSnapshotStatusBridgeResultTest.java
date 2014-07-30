/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.snapshot.dto.SnapshotStatus;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;


/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetSnapshotStatusBridgeResultTest {

    @Test
    public void testSerialize() {
        SnapshotStatus status = SnapshotStatus.INITIALIZED;
        String details = "status-details";

        GetSnapshotStatusBridgeResult params =
            new GetSnapshotStatusBridgeResult(status, details);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"status\":\""+status.name()+"\""));
        assertThat(cleanResult, containsString("\"details\":\""+details+"\""));
    }

    @Test
    public void testDeSerialize(){
        SnapshotStatus status = SnapshotStatus.INITIALIZED;
        String details = "details";
        String str = "{ \"status\" : \"" + status + "\","  
                        + " \"details\" : \"" + details + "\"}";
        
        GetSnapshotStatusBridgeResult params = GetSnapshotStatusBridgeResult
            .deserialize(str);
        Assert.assertEquals(status, params.getStatus());
        Assert.assertEquals(details, params.getDetails());
    }

}
