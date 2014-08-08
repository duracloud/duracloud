/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.util.Date;

import org.duracloud.snapshot.dto.RestoreStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetRestoreBridgeResultTest {

    @Test
    public void testDeSerialize(){
        Long id = 10101l;
        String snapshotId = "snapshotId";
        Date startDate  = new Date();
        Date endDate  = new Date();
        RestoreStatus status = RestoreStatus.RESTORATION_COMPLETE;
        String statusText = "status text";
        
        String str = "{ \"id\": \"" + id + "\","  
                        + " \"snapshotId\" : \"" + snapshotId + "\"," 
                        + " \"startDate\" : \"" + startDate.getTime() + "\"," 
                        + " \"endDate\" : \"" + endDate.getTime() + "\"," 
                        + " \"status\" : \"" + status + "\"," 
                        + " \"statusText\" : \"" + statusText + "\"" 
                        +"}";
        
        GetRestoreBridgeResult params = GetRestoreBridgeResult
            .deserialize(str);

        Assert.assertEquals(id, params.getId());
        Assert.assertEquals(snapshotId, params.getSnapshotId());
        Assert.assertEquals(status, params.getStatus());
        Assert.assertEquals(startDate, params.getStartDate());
        Assert.assertEquals(endDate, params.getEndDate());
        Assert.assertEquals(statusText, params.getStatusText());

    }

}
