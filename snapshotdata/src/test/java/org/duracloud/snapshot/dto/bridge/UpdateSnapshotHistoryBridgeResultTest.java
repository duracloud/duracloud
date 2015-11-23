/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Gad Krumholz
 *         Date: 7/02/15
 */
public class UpdateSnapshotHistoryBridgeResultTest {

     private String history = "history";
     private String snapshotId = "id-1";
     private String description = "desc-1";
     private String sourceSpaceId = "sourceSpaceId";
     private String sourceStoreId = "sourceStoreId";
     
    SnapshotSummary snapshot =
        new SnapshotSummary(snapshotId,
                            SnapshotStatus.SNAPSHOT_COMPLETE,
                            description,
                            sourceStoreId,
                            sourceSpaceId);

    @Test
    public void testSerialize() {
        
        UpdateSnapshotHistoryBridgeResult bridgeResult =
            new UpdateSnapshotHistoryBridgeResult(snapshot, history);
        String result = bridgeResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"history\":\""+history+"\""));
        verifySummary(result, "id-1", SnapshotStatus.SNAPSHOT_COMPLETE.name(), "desc-1");
    }
    
    private void verifySummary(String result, String id,
                                String status, String description) {
        String cleanResult = result.replaceAll("\\s+", "");
        
        assertThat(cleanResult, containsString("\"snapshotId\":\""+id+"\""));
        assertThat(cleanResult,
        containsString("\"status\":\""+status+"\""));
        assertThat(cleanResult,
        containsString("\"description\":\""+description+"\""));
    }

    @Test
    public void testDeSerialize(){
        SnapshotStatus status = SnapshotStatus.INITIALIZED;
        String str = "{ \"snapshot\" : " +
                     "{ \"status\" : \"" + status + "\","  
                        + " \"description\" : \"" + description + "\"," 
                        + " \"snapshotId\" : \"" + snapshotId + "\"}, " +
                     "  \"history\" : \"" + history + "\"}";

        System.out.println(str);

        UpdateSnapshotHistoryBridgeResult result =
                UpdateSnapshotHistoryBridgeResult.deserialize(str);
        
        SnapshotSummary snapshotSummary = result.getSnapshot();
        
        Assert.assertEquals(snapshotId, snapshotSummary.getSnapshotId());
        Assert.assertEquals(status, snapshotSummary.getStatus());
        Assert.assertEquals(description, snapshotSummary.getDescription());
        Assert.assertEquals(history, result.getHistory());
    }

}
