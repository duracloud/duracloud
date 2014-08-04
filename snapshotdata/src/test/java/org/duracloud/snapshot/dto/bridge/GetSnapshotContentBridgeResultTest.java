/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetSnapshotContentBridgeResultTest {


    @Test
    public void testDeSerialize(){
        String contentId = "content-id";
        String str = "{ \"contentIds\" : " +
                     "[ \""+contentId + "\"]"  + 
                        "}";
                   ;
        GetSnapshotContentBridgeResult result =  GetSnapshotContentBridgeResult.deserialize(str);
        List<String> contentIds = result.getContentIds();
        
        Assert.assertNotNull(contentIds);
        Assert.assertEquals(1,contentIds.size());
        
        Assert.assertEquals(contentId, contentIds.get(0));
    }

}
