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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.duracloud.snapshot.dto.SnapshotHistoryItem;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Gad Krumholz
 *         Date: 7/02/15
 */
public class GetSnapshotHistoryBridgeResultTest {

     private String history = "history";
     private Date historyDate = new Date();
     private Long totalCount = 1001l;

    @Test
    public void testSerialize() {
        
        GetSnapshotHistoryBridgeResult bridgeResult =
            new GetSnapshotHistoryBridgeResult();
        List<SnapshotHistoryItem> historyItemList = new ArrayList<>();
        SnapshotHistoryItem historyItem = new SnapshotHistoryItem();
        historyItem.setHistory(history);
        historyItem.setHistoryDate(historyDate);
        
        historyItemList.add(historyItem);
        bridgeResult.setHistoryItems(historyItemList);
        bridgeResult.setTotalCount(totalCount);
        String result = bridgeResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"history\":\""+history+"\""));
        assertThat(cleanResult, containsString("\"historyDate\":"+historyDate.getTime()+""));
        assertThat(cleanResult, containsString("\"totalCount\":" +totalCount));

    }

    @Test
    public void testDeSerialize(){
        String str = "{ \"totalCount\" : " +  totalCount+", \"historyItems\" : " +
                     "[ { \"history\" : \"" + history + "\","
                        + " \"historyDate\" : " + historyDate.getTime() + " } ] }";

        System.out.println(str);

        GetSnapshotHistoryBridgeResult result =
                GetSnapshotHistoryBridgeResult.deserialize(str);
        List<SnapshotHistoryItem> historyItems = result.getHistoryItems();

        Assert.assertNotNull(historyItems);
        Assert.assertEquals(1,historyItems.size());
        Assert.assertEquals(totalCount, result.getTotalCount());

        Assert.assertEquals(history, historyItems.get(0).getHistory());
        Assert.assertEquals(historyDate, historyItems.get(0).getHistoryDate());
    }

}
