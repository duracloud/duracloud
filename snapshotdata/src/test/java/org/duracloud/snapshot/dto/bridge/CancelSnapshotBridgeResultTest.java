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


/**
 * @author Daniel Bernstein
 *         Date: 9/22/15
 */
public class CancelSnapshotBridgeResultTest {

    @Test
    public void testDeserialize(){
        SnapshotStatus status = SnapshotStatus.CANCELLED;
        String details = "details";
        String str = "{ \"status\" : \"" + status + "\","  
                        + " \"details\" : \"" + details + "\"}";
        
        CancelSnapshotBridgeResult params =
            CancelSnapshotBridgeResult.deserialize(str);
        Assert.assertEquals(status, params.getStatus());
        Assert.assertEquals(details, params.getDetails());
    }

}
