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
 *         Date: 7/29/14
 */
public class CompleteSnapshotBridgeResultTest {

    @Test
    public void testDeSerialize(){
        SnapshotStatus status = SnapshotStatus.INITIALIZED;
        String details = "details";
        String str = "{ \"status\" : \"" + status + "\","  
                        + " \"details\" : \"" + details + "\"}";
        
        CompleteSnapshotBridgeResult params =
            CompleteSnapshotBridgeResult.deserialize(str);
        Assert.assertEquals(status, params.getStatus());
        Assert.assertEquals(details, params.getDetails());
    }

}
