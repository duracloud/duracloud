/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.duracloud.reportdata.storage.ReportTestHelper;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: 5/17/11
 */
public class StorageReportSerializerTest {

    private String reportId = "reportId";
    private long completionTime = 1000;
    private long elapsedTime = 100;
    private ReportTestHelper<StorageReport> testHelper;

    @Before
    public void setup() {
        this.testHelper = new ReportTestHelper<StorageReport>();
    }

    @Test
    public void testStorageReportSerializer() {
        StorageReport report = createStorageReport();
        StorageMetrics metrics = report.getStorageMetrics();

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

    @Test
    public void testSchemaVersionCheck() {
        String schemaVersion = "42";
        StorageReport report = createStorageReport();
        report.setSchemaVersion(schemaVersion);
        testHelper.schemaVersionCheck(report,
                                      schemaVersion,
                                      new StorageReportSerializer());
    }

    @Test
    public void testValidationCheck() {
        StorageReport report = createStorageReport();
        testHelper.validationCheck(report,
                                   new StorageReportSerializer());
    }

    private StorageReport createStorageReport() {
        StorageMetrics metrics = testHelper.createMetrics();
        return new StorageReport(reportId, metrics, completionTime, elapsedTime);
    }
}
