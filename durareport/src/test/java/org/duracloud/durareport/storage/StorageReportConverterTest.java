/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import org.duracloud.durareport.storage.metrics.DuraStoreMetricsCollector;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.MimetypeMetrics;
import org.duracloud.reportdata.storage.metrics.SpaceMetrics;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportConverterTest {

    private final String providerId1 = "provider1";
    private final String providerId2 = "provider2";
    private final String providerType1 = "type1";
    private final String providerType2 = "type2";
    private final String space1 = "space1";
    private final String space2 = "space2";
    private final String mimetype1 = "mimetype1";
    private final String mimetype2 = "mimetype2";

    private final String contentId = "contentId";
    private final long completionTime = 1000L;
    private final long elapsedTime = 1L;

    @Test
    public void testCreateStorageReport() throws Exception {
        DuraStoreMetricsCollector collector = new DuraStoreMetricsCollector();
        for(int i=1; i<=10; i++) {
            collector.update(providerId1, providerType1, space1, mimetype1, 10);
            collector.update(providerId1, providerType1, space1, mimetype2, 10);
            collector.update(providerId1, providerType1, space2, mimetype1, 10);
            collector.update(providerId1, providerType1, space2, mimetype2, 10);
            collector.update(providerId2, providerType2, space1, mimetype1, 10);
            collector.update(providerId2, providerType2, space1, mimetype2, 10);
            collector.update(providerId2, providerType2, space2, mimetype1, 10);
            collector.update(providerId2, providerType2, space2, mimetype2, 10);
        }

        StorageReportConverter converter = new StorageReportConverter();
        StorageReport report = converter.createStorageReport(contentId,
                                                             collector,
                                                             completionTime,
                                                             elapsedTime);
        assertNotNull(report);
        assertEquals(contentId, report.getContentId());
        assertEquals(completionTime, report.getCompletionTime());
        assertEquals(elapsedTime, report.getElapsedTime());

        StorageMetrics metrics = report.getStorageMetrics();
        assertNotNull(metrics);
        assertEquals(80, metrics.getTotalItems());
        assertEquals(800, metrics.getTotalSize());
        verifyMimeMetrics(metrics.getMimetypeMetrics(), 40, 400);

        List<StorageProviderMetrics> providerMetrics =
            metrics.getStorageProviderMetrics();
        assertNotNull(providerMetrics);
        assertEquals(2, providerMetrics.size());

        for(StorageProviderMetrics provider : providerMetrics) {
            assertEquals(40, provider.getTotalItems());
            assertEquals(400, provider.getTotalSize());

            assertTrue(provider.getStorageProviderId().equals(providerId1) ||
                       provider.getStorageProviderId().equals(providerId2));
            assertTrue(provider.getStorageProviderType().equals(providerType1) ||
                       provider.getStorageProviderType().equals(providerType2));

            verifyMimeMetrics(provider.getMimetypeMetrics(), 20, 200);

            List<SpaceMetrics> spaceMetrics = provider.getSpaceMetrics();
            assertNotNull(spaceMetrics);
            assertEquals(2, spaceMetrics.size());

            for(SpaceMetrics space : spaceMetrics) {
                assertEquals(20, space.getTotalItems());
                assertEquals(200, space.getTotalSize());

                assertTrue(space.getSpaceName().equals(space1) ||
                           space.getSpaceName().equals(space2));

                verifyMimeMetrics(space.getMimetypeMetrics(), 10, 100);
            }
        }

    }

    private void verifyMimeMetrics(List<MimetypeMetrics> mimeMetrics,
                                   long totalItems,
                                   long totalSize) {
        assertEquals(2, mimeMetrics.size());
        for(MimetypeMetrics mime : mimeMetrics) {
            assertEquals(totalItems, mime.getTotalItems());
            assertEquals(totalSize, mime.getTotalSize());

            assertTrue(mime.getMimetype().equals(mimetype1) ||
                       mime.getMimetype().equals(mimetype2));
        }
    }

}
