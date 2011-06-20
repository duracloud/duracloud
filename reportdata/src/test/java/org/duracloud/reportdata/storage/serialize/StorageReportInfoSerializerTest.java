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

        String xml = serializer.serializeReportInfo(info);
        assertNotNull(xml);

        StorageReportInfo deserialXml = serializer.deserializeReportInfo(xml);

        assertEquals(status, deserialXml.getStatus());
        assertEquals(startTime, deserialXml.getStartTime());
        assertEquals(currentCount, deserialXml.getCurrentCount());
        assertEquals(finalCount, deserialXml.getFinalCount());
        assertEquals(completionTime, deserialXml.getCompletionTime());
        assertEquals(estimatedCompletionTime,
                     deserialXml.getEstimatedCompletionTime());
        assertEquals(nextScheduledStartTime,
                     deserialXml.getNextScheduledStartTime());
    }
}
