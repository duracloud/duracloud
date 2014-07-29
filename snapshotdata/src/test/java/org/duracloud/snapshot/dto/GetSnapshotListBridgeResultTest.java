/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetSnapshotListBridgeResultTest {


    
    @Test
    public void testDeSerialize(){
        String snapshotId = "snapshotId";
        SnapshotStatus status = SnapshotStatus.INITIALIZED;
        String description = "description";
        String str = "{ \"snapshots\" : " +
                     "[ { \"status\" : \"" + status + "\","  
                        + " \"description\" : \"" + description + "\"," 
                        + " \"snapshotId\" : \"" + snapshotId + "\"}]}";
                GetSnapshotListBridgeResult params = GetSnapshotListBridgeResult.deserialize(str);
        List<SnapshotSummary> snapshots = params.getSnapshots();

        Assert.assertNotNull(snapshots);
        Assert.assertEquals(1,snapshots.size());
        
        SnapshotSummary snapshot = snapshots.get(0);
        Assert.assertEquals(snapshotId, snapshot.getSnapshotId());
        Assert.assertEquals(status, snapshot.getStatus());
        Assert.assertEquals(description, snapshot.getDescription());
    }


}
