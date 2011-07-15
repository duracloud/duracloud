/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.reportdata.storage.ReportTestHelper;
import org.duracloud.reportdata.storage.StorageReportInfo;
import org.junit.Before;
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
    private final String error = "none";

    private ReportTestHelper<StorageReportInfo> testHelper;

    @Before
    public void setup() {
        this.testHelper = new ReportTestHelper<StorageReportInfo>();
    }

    @Test
    public void testStorageReportInfoSerializer() {
        StorageReportInfoSerializer serializer =
            new StorageReportInfoSerializer();

        StorageReportInfo info = createStorageReportInfo();

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
        assertEquals(error, infoDeserialized.getError());

        assertEquals(info, infoDeserialized);
        assertEquals(xml, serializer.serialize(infoDeserialized));
    }

    @Test
    public void testSchemaVersionCheck() {
        String schemaVersion = "42";
        StorageReportInfo reportInfo = createStorageReportInfo();
        reportInfo.setSchemaVersion(schemaVersion);
        testHelper.schemaVersionCheck(reportInfo,
                                      schemaVersion,
                                      new StorageReportInfoSerializer());
    }

    @Test
    public void testValidationCheck() {
        StorageReportInfo reportInfo = createStorageReportInfo();
        testHelper.validationCheck(reportInfo,
                                   new StorageReportInfoSerializer());
    }

    private StorageReportInfo createStorageReportInfo() {
        StorageReportInfo info = new StorageReportInfo();
        info.setStatus(status);
        info.setStartTime(100);
        info.setCurrentCount(currentCount);
        info.setFinalCount(finalCount);
        info.setCompletionTime(completionTime);
        info.setEstimatedCompletionTime(estimatedCompletionTime);
        info.setNextScheduledStartTime(nextScheduledStartTime);
        info.setError(error);
        return info;
    }
}
