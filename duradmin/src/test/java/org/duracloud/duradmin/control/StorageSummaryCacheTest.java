/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.util.Arrays;
import java.util.List;

import org.duracloud.client.report.StorageReportManager;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.SpaceMetrics;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class StorageSummaryCacheTest {

    @Test
    public void testGetSummaries() throws Exception {
        StorageReportManager srm =
            EasyMock.createMock("StorageReportManager",
                                StorageReportManager.class);

        String storeId = "test-storeId";
        String spaceId = "test-space";

        List<SpaceMetrics> spaceMetrics =
            Arrays.asList(new SpaceMetrics[] { new SpaceMetrics(spaceId,
                                                                10,
                                                                10,
                                                                null) });
        StorageProviderMetrics spm =
            new StorageProviderMetrics(storeId,
                                       "type",
                                       spaceMetrics,
                                       0,
                                       0,
                                       null);
        StorageMetrics storageMetrics =
            EasyMock.createMock("StorageMetrics", StorageMetrics.class);
        EasyMock.expect(storageMetrics.getStorageProviderMetrics())
                .andReturn(Arrays.asList(new StorageProviderMetrics[]{spm}));

        StorageReport storageReport =
            EasyMock.createMock("StorageReport", StorageReport.class);
        EasyMock.expect(storageReport.getStorageMetrics())
                .andReturn(storageMetrics);

        EasyMock.expect(srm.getStorageReportList())
                .andReturn(Arrays.asList(new String[] { "report/storage-report-2012-03-26T23:54:58.xml" }));

        EasyMock.expect(srm.getStorageReport(EasyMock.isA(String.class)))
                .andReturn(storageReport);

        EasyMock.replay(srm, storageReport, storageMetrics);

        StorageSummaryCache cache = new StorageSummaryCacheImpl(srm);

        cache.init();
        // wait for cache to start building.
        Thread.sleep(1000);

        // test null parameters
        try {
            cache.getSummaries(null, null);
            Assert.assertTrue(false);
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }

        List<StorageSummary> list = cache.getSummaries(storeId, null);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        list = cache.getSummaries(storeId, spaceId);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        EasyMock.verify(srm, storageReport,storageMetrics);
    }

}
