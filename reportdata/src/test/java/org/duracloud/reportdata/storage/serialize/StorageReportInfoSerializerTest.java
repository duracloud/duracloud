/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.reportdata.storage.StorageReportInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportInfoSerializerTest {

    private final String status = "status";
    private final long startTime = 100;
    private final long currentCount = 34;
    private final long finalCount = 39;
    private final long completionTime = 200;
    private final long estimatedCompletionTime = 202;
    private final long nextScheduledStartTime = 1492;

    @Test
    public void testStorageReportInfoSerializer() {
        StorageReportInfoSerializer serializer =
            new StorageReportInfoSerializer();

        StorageReportInfo info = new StorageReportInfo();
        info.setStatus(status);
        info.setStartTime(100);
        info.setCurrentCount(currentCount);
        info.setFinalCount(finalCount);
        info.setCompletionTime(completionTime);
        info.setEstimatedCompletionTime(estimatedCompletionTime);
        info.setNextScheduledStartTime(nextScheduledStartTime);

        String xml = serializer.serialize(info);
        assertNotNull(xml);

        StorageReportInfo infoDeserialized = serializer.deserialize(xml);

        assertEquals(status, infoDeserialized.getStatus());
        assertEquals(startTime, infoDeserialized.getStartTime());
        assertEquals(currentCount, infoDeserialized.getCurrentCount());
        assertEquals(finalCount, infoDeserialized.getFinalCount());
        assertEquals(completionTime, infoDeserialized.getCompletionTime());
        assertEquals(estimatedCompletionTime,
                     infoDeserialized.getEstimatedCompletionTime());
        assertEquals(nextScheduledStartTime,
                     infoDeserialized.getNextScheduledStartTime());

        assertEquals(info, infoDeserialized);
        assertEquals(xml, serializer.serialize(infoDeserialized));
    }
}
