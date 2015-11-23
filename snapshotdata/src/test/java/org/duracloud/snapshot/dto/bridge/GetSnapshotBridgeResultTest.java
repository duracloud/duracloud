/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.duracloud.snapshot.dto.SnapshotStatus;
import org.junit.Test;


/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetSnapshotBridgeResultTest {

    private SnapshotStatus status = SnapshotStatus.INITIALIZED;
    private String description = "description";
    private String sourceHost = "source-host";
    private String sourceSpaceId = "source-space-id";
    private String sourceStoreId = "source-store-id";
    private Date snapshotDate = new Date();
    private String snapshotId = "snapshot-id";
    private Long contentItemCount = 424242l;
    private Long totalSizeInBytes = 111111l;
    private String alternateId1 = "alternate-1", alternateId2 = "alternate-2";
    private String memberId = "member-id";
    private List<String> alternateIds = new ArrayList<String>();
    {
        alternateIds.add(alternateId1);
        alternateIds.add(alternateId2);
    }
    
    @Test
    public void testSerialize() {

        GetSnapshotBridgeResult params =
            new GetSnapshotBridgeResult();
        params.setStatus(status);
        params.setDescription(description);
        params.setSourceHost(sourceHost);
        params.setSourceSpaceId(sourceSpaceId);
        params.setSourceStoreId(sourceStoreId);
        params.setSnapshotId(snapshotId);
        params.setSnapshotDate(snapshotDate);
        params.setContentItemCount(contentItemCount);
        params.setTotalSizeInBytes(totalSizeInBytes);
        params.setAlternateIds(alternateIds);
        params.setMemberId(memberId);
        
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"status\":\""+status.name()+"\""));
        assertThat(cleanResult,
                   containsString("\"description\":\""+description+"\""));
        assertThat(cleanResult,
                   containsString("\"sourceHost\":\""+sourceHost+"\""));
        assertThat(cleanResult,
                   containsString("\"sourceSpaceId\":\""+sourceSpaceId+"\""));
        assertThat(cleanResult,
                   containsString("\"sourceStoreId\":\""+sourceStoreId+"\""));
        assertThat(cleanResult,
                   containsString("\"snapshotDate\":"+snapshotDate.getTime()));
        assertThat(cleanResult,
                   containsString("\"description\":\""+description+"\""));
        assertThat(cleanResult,
                   containsString("\"contentItemCount\":"+contentItemCount));
        assertThat(cleanResult,
                   containsString("\"totalSizeInBytes\":"+totalSizeInBytes));
        assertThat(cleanResult,
                   containsString("\"memberId\":\""+memberId+"\""));

        assertThat(cleanResult,
                containsString("\"alternateIds\":[\"" + alternateId1 + "\",\"" + alternateId2 + "\"]"));

        GetSnapshotBridgeResult params2 =
            GetSnapshotBridgeResult.deserialize(result);
        assertEquals(description, params2.getDescription());
        assertEquals(sourceHost, params2.getSourceHost());
        assertEquals(sourceSpaceId, params2.getSourceSpaceId());
        assertEquals(sourceStoreId, params2.getSourceStoreId());
        assertEquals(snapshotDate, params2.getSnapshotDate());
        assertEquals(snapshotId, params2.getSnapshotId());
        assertEquals(contentItemCount, params2.getContentItemCount());
        assertEquals(totalSizeInBytes, params2.getTotalSizeInBytes());
        assertEquals(alternateIds, params2.getAlternateIds());
    }
}
