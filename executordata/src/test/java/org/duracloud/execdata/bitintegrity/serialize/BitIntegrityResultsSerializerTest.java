/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.execdata.bitintegrity.serialize;

import org.duracloud.execdata.bitintegrity.BitIntegrityResults;
import org.duracloud.execdata.bitintegrity.SpaceBitIntegrityResult;
import org.duracloud.execdata.bitintegrity.StoreBitIntegrityResults;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author: Bill Branan
 * Date: 3/20/12
 */
public class BitIntegrityResultsSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        BitIntegrityResultsSerializer serializer =
            new BitIntegrityResultsSerializer();

        BitIntegrityResults startResults = buildResults();
        String json = serializer.serialize(startResults);
        
        BitIntegrityResults endResults = serializer.deserialize(json);
        assertFalse(startResults == endResults);
        assertEquals(startResults, endResults);
    }

    private BitIntegrityResults buildResults() {
        String storeId = "store-Id";
        String result = "SUCCESS";

        String spaceId1 = "space-1";
        Date date11 = new Date(System.currentTimeMillis() - 500000);
        String contentId1 = "content-Id-1-1";

        SpaceBitIntegrityResult space1Result1 =
            new SpaceBitIntegrityResult(date11, result, contentId1, true);

        Date date12 = new Date(System.currentTimeMillis() - 400000);
        String contentId12 = "content-Id-1-2";

        SpaceBitIntegrityResult space1Result2 =
            new SpaceBitIntegrityResult(date12, result, contentId12, true);

        String spaceId2 = "space-2";
        Date date2 = new Date(System.currentTimeMillis() - 300000);
        String contentId2 = "content-Id-2-1";

        SpaceBitIntegrityResult space2Result =
            new SpaceBitIntegrityResult(date2, result, contentId2, true);

        StoreBitIntegrityResults storeResults = new StoreBitIntegrityResults();
        storeResults.addSpaceResult(spaceId1, space1Result1);
        storeResults.addSpaceResult(spaceId1, space1Result2);
        storeResults.addSpaceResult(spaceId2, space2Result);

        BitIntegrityResults results = new BitIntegrityResults();
        results.addStoreResults(storeId, storeResults);

        return results;
    }

}
