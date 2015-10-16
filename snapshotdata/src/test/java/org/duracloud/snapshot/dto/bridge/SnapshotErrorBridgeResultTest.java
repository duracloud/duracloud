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
 * @author Bill Branan
 *         Date: 9/17/2015
 */
public class SnapshotErrorBridgeResultTest {

    @Test
    public void testDeSerialize(){
        SnapshotStatus status = SnapshotStatus.ERROR;
        String details = "details";
        String str = "{ \"status\" : \"" + status + "\"," +
                      " \"details\" : \"" + details + "\"}";

        SnapshotErrorBridgeResult params =
            SnapshotErrorBridgeResult.deserialize(str);
        Assert.assertEquals(status, params.getStatus());
        Assert.assertEquals(details, params.getDetails());
    }

}
