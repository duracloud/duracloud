/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;


/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetSnapshotListBridgeResultTest {

    @Test
    public void testSerialize() {
        SnapshotSummary summary1 =
            new SnapshotSummary("id-1", SnapshotStatus.SNAPSHOT_COMPLETE, "desc-1");
        List<SnapshotSummary> summaries = new ArrayList<>();
        summaries.add(summary1);

        String result = new GetSnapshotListBridgeResult(summaries).serialize();
        verifySummary(result, "id-1", SnapshotStatus.SNAPSHOT_COMPLETE.name(), "desc-1");

        SnapshotSummary summary2 =
            new SnapshotSummary("id-2", SnapshotStatus.WAITING_FOR_DPN, "desc-2");
        summaries.add(summary2);

        result = new GetSnapshotListBridgeResult(summaries).serialize();
        verifySummary(result, "id-1", SnapshotStatus.SNAPSHOT_COMPLETE.name(), "desc-1");
        verifySummary(result, "id-2", SnapshotStatus.SNAPSHOT_COMPLETE.name(), "desc-2");
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
        String snapshotId = "snapshotId";
        SnapshotStatus status = SnapshotStatus.INITIALIZED;
        String description = "description";
        String str = "{ \"snapshots\" : " +
                     "[ { \"status\" : \"" + status + "\","  
                        + " \"description\" : \"" + description + "\"," 
                        + " \"snapshotId\" : \"" + snapshotId + "\"}]}";
                GetSnapshotListBridgeResult params =
                    GetSnapshotListBridgeResult.deserialize(str);
        List<SnapshotSummary> snapshots = params.getSnapshots();

        Assert.assertNotNull(snapshots);
        Assert.assertEquals(1,snapshots.size());
        
        SnapshotSummary snapshot = snapshots.get(0);
        Assert.assertEquals(snapshotId, snapshot.getSnapshotId());
        Assert.assertEquals(status, snapshot.getStatus());
        Assert.assertEquals(description, snapshot.getDescription());
    }

}
