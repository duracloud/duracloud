/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;


/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetSnapshotContentBridgeResultTest {

     private String contentId = "content-id";
     private String propName = "content-prop-name";
     private String propValue = "content-prop-value";
     private Long totalCount = 1001l;

    @Test
    public void testSerialize() {
        
        GetSnapshotContentBridgeResult bridgeResult =
            new GetSnapshotContentBridgeResult();
        List<SnapshotContentItem> contentItemList = new ArrayList<>();
        SnapshotContentItem contentItem = new SnapshotContentItem();
        contentItem.setContentId(contentId);
        Map<String, String> contentProperties = new HashMap<>();
        contentProperties.put(propName, propValue);
        contentItem.setContentProperties(contentProperties);
        contentItemList.add(contentItem);
        bridgeResult.setContentItems(contentItemList);
        bridgeResult.setTotalCount(totalCount);
        String result = bridgeResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"contentId\":\""+contentId+"\""));
        assertThat(cleanResult, containsString("\""+propName+"\":\""+propValue+"\""));
        assertThat(cleanResult, containsString("\"totalCount\":" +totalCount));

    }

    @Test
    public void testDeSerialize(){
        String str = "{ \"totalCount\" : " +  totalCount+", \"contentItems\" : " +
                     "[ { \"contentId\" : \"" + contentId + "\","
                        + " \"contentProperties\" : " +
                        "{\"" + propName + "\" : \"" + propValue + "\"}}]}";

        System.out.println(str);

        GetSnapshotContentBridgeResult result =
            GetSnapshotContentBridgeResult.deserialize(str);
        List<SnapshotContentItem> contentItems = result.getContentItems();

        Assert.assertNotNull(contentItems);
        Assert.assertEquals(1,contentItems.size());
        Assert.assertEquals(totalCount, result.getTotalCount());

        Assert.assertEquals(contentId, contentItems.get(0).getContentId());
        Assert.assertEquals(propValue, contentItems.get(0).getContentProperties()
                                                   .get(propName));
    }

}
