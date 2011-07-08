/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.reportdata.storage.ReportTestHelper;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 5/17/11
 */
public class StorageReportSerializerTest {

    @Test
    public void testStorageReportSerializer() {
        ReportTestHelper testHelper = new ReportTestHelper();
        StorageMetrics metrics = testHelper.createMetrics();

        String reportId = "reportId";
        long completionTime = 1000;
        long elapsedTime = 100;
        StorageReport report =
            new StorageReport(reportId, metrics, completionTime, elapsedTime);

        StorageReportSerializer serializer = new StorageReportSerializer();
        String xml = serializer.serialize(report);
        assertNotNull(xml);

        StorageReport reportDeserialized = serializer.deserialize(xml);
        assertEquals(reportId, reportDeserialized.getReportId());
        assertEquals(completionTime, reportDeserialized.getCompletionTime());
        assertEquals(elapsedTime, reportDeserialized.getElapsedTime());

        StorageMetrics metricsDeserialized =
            reportDeserialized.getStorageMetrics();
        assertEquals(metrics.getTotalItems(),
                     metricsDeserialized.getTotalItems());
        assertEquals(metrics.getTotalSize(),
                     metricsDeserialized.getTotalSize());
        assertEquals(metrics.getStorageProviderMetrics().size(),
                     metricsDeserialized.getStorageProviderMetrics().size());

        assertEquals(report, reportDeserialized);
        assertEquals(xml, serializer.serialize(reportDeserialized));
    }
}
