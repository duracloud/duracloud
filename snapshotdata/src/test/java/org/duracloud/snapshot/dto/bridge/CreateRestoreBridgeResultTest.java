/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.snapshot.dto.RestoreStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Bill Branan
 *         Date: 7/28/14
 */
public class CreateRestoreBridgeResultTest {



    @Test
    public void testDeserialize() {
        RestoreStatus status = RestoreStatus.INITIALIZED;
        String restoreId = "restore-id";
        String str = "{ \"status\" : \"" + status + "\","  
                        + " \"restoreId\" : \"" + restoreId + "\"}";
        
        CreateRestoreBridgeResult params = CreateRestoreBridgeResult
            .deserialize(str);
        Assert.assertEquals(status, params.getStatus());
        Assert.assertEquals(restoreId, params.getRestoreId());
        
    }

}
