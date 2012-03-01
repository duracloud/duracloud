/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reporter.storage.metrics;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class MetricsCollectorTest {

    private String mimetype1 = "text/plain";
    private String mimetype2 = "text/xml";
    private String mimetype3 = "application/xml";
    private String spaceName1 = "space1";
    private String spaceName2 = "space2";
    private String providerId1 = "provider1";
    private String providerType1 = "AMAZON";
    private String providerId2 = "provider2";
    private String providerType2 = "RACKSPACE";

    @Test
    public void testMimetypeMetrics() {
        MimetypeMetricsCollector metrics = new MimetypeMetricsCollector(mimetype1);

        // Add data
        for(int i=1; i<=100; i++) {
            metrics.update(i);
        }

        // Verify totals
        assertEquals(mimetype1, metrics.getMimetype());
        assertEquals(100, metrics.getTotalItems());
        assertEquals(5050, metrics.getTotalSize());
    }

    @Test
    public void testSpaceMetrics() {
        SpaceMetricsCollector metrics = new SpaceMetricsCollector(spaceName1);

        // Add data
        for(int i=1; i<=10; i++) {
            metrics.update(mimetype1, i);
        }
        for(int i=1; i<=5; i++) {
            metrics.update(mimetype2, i);
        }
        for(int i=1; i<=3; i++) {
            metrics.update(mimetype3, i);
        }

        verifySpaceTotals(spaceName1, metrics);
    }

    private void verifySpaceTotals(String spaceName, SpaceMetricsCollector metrics) {
        // Verify space totals
        assertEquals(spaceName, metrics.getSpaceName());
        assertEquals(18, metrics.getTotalItems());
        assertEquals(76, metrics.getTotalSize());

        // Verify mimetype totals
        Map<String, MimetypeMetricsCollector> mimetypeMetricsMap =
            metrics.getMimetypeMetrics();
        assertEquals(3, mimetypeMetricsMap.size());

        MimetypeMetricsCollector mimetype1Metrics = mimetypeMetricsMap.get(mimetype1);
        assertNotNull(mimetype1Metrics);
        assertEquals(mimetype1, mimetype1Metrics.getMimetype());
        assertEquals(10, mimetype1Metrics.getTotalItems());
        assertEquals(55, mimetype1Metrics.getTotalSize());

        MimetypeMetricsCollector mimetype2Metrics = mimetypeMetricsMap.get(mimetype2);
        assertNotNull(mimetype2Metrics);
        assertEquals(mimetype2, mimetype2Metrics.getMimetype());
        assertEquals(5, mimetype2Metrics.getTotalItems());
        assertEquals(15, mimetype2Metrics.getTotalSize());

        MimetypeMetricsCollector mimetype3Metrics = mimetypeMetricsMap.get(mimetype3);
        assertNotNull(mimetype3Metrics);
        assertEquals(mimetype3, mimetype3Metrics.getMimetype());
        assertEquals(3, mimetype3Metrics.getTotalItems());
        assertEquals(6, mimetype3Metrics.getTotalSize());
    }

    @Test
    public void testStorageProviderMetricsError() {
        StorageProviderMetricsCollector metrics =
            new StorageProviderMetricsCollector(providerId1, providerType1);

        try {
            metrics.update(mimetype1, 1);
            fail("Exception expected");
        } catch(UnsupportedOperationException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testStorageProviderMetrics() {
        StorageProviderMetricsCollector metrics =
            new StorageProviderMetricsCollector(providerId1, providerType1);

        // Add data for space 1
        for(int i=1; i<=10; i++) {
            metrics.update(spaceName1, mimetype1, i);
        }
        for(int i=1; i<=5; i++) {
            metrics.update(spaceName1, mimetype2, i);
        }
        for(int i=1; i<=3; i++) {
            metrics.update(spaceName1, mimetype3, i);
        }

        // Add data for space 2
        for(int i=1; i<=10; i++) {
            metrics.update(spaceName2, mimetype1, i);
        }
        for(int i=1; i<=5; i++) {
            metrics.update(spaceName2, mimetype2, i);
        }
        for(int i=1; i<=3; i++) {
            metrics.update(spaceName2, mimetype3, i);
        }
        
        verifyStorageProviderMetrics(providerId1, metrics);
    }

    private void verifyStorageProviderMetrics(String providerId,
                                              StorageProviderMetricsCollector metrics) {
        // Verify storage provider totals
        assertEquals(providerId, metrics.getStorageProviderId());
        assertEquals(36, metrics.getTotalItems());
        assertEquals(152, metrics.getTotalSize());

        // Verify storage provider mimetype totals
        Map<String, MimetypeMetricsCollector> mimetypeMetricsMap =
            metrics.getMimetypeMetrics();
        assertEquals(3, mimetypeMetricsMap.size());

        MimetypeMetricsCollector mimetype1Metrics = mimetypeMetricsMap.get(mimetype1);
        assertNotNull(mimetype1Metrics);
        assertEquals(mimetype1, mimetype1Metrics.getMimetype());
        assertEquals(20, mimetype1Metrics.getTotalItems());
        assertEquals(110, mimetype1Metrics.getTotalSize());

        MimetypeMetricsCollector mimetype2Metrics = mimetypeMetricsMap.get(mimetype2);
        assertNotNull(mimetype2Metrics);
        assertEquals(mimetype2, mimetype2Metrics.getMimetype());
        assertEquals(10, mimetype2Metrics.getTotalItems());
        assertEquals(30, mimetype2Metrics.getTotalSize());

        MimetypeMetricsCollector mimetype3Metrics = mimetypeMetricsMap.get(mimetype3);
        assertNotNull(mimetype3Metrics);
        assertEquals(mimetype3, mimetype3Metrics.getMimetype());
        assertEquals(6, mimetype3Metrics.getTotalItems());
        assertEquals(12, mimetype3Metrics.getTotalSize());

        // Verify space metrics map
        Map<String, SpaceMetricsCollector> spaceMetricsMap = metrics.getSpaceMetrics();
        assertNotNull(spaceMetricsMap);
        assertEquals(2, spaceMetricsMap.size());

        // Verify space 1 totals
        SpaceMetricsCollector space1Metrics = spaceMetricsMap.get(spaceName1);
        assertNotNull(space1Metrics);
        verifySpaceTotals(spaceName1, space1Metrics);

        // Verify space 2 totals
        SpaceMetricsCollector space2Metrics = spaceMetricsMap.get(spaceName2);
        assertNotNull(space2Metrics);
        verifySpaceTotals(spaceName2, space2Metrics);
    }

    @Test
    public void testDuraStoreMetricsError() {
        DuraStoreMetricsCollector metrics = new DuraStoreMetricsCollector();

        try {
            metrics.update(mimetype1, 1);
            fail("Exception expected");
        } catch(UnsupportedOperationException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testDuraStoreMetrics() {
        DuraStoreMetricsCollector metrics = new DuraStoreMetricsCollector();

        // Add data
        
        // Provider 1, Space 1
        for(int i=1; i<=10; i++) {
            metrics.update(providerId1, providerType1, spaceName1, mimetype1, i);
        }
        for(int i=1; i<=5; i++) {
            metrics.update(providerId1, providerType1, spaceName1, mimetype2, i);
        }
        for(int i=1; i<=3; i++) {
            metrics.update(providerId1, providerType1, spaceName1, mimetype3, i);
        }

        // Provider 1, Space 2
        for(int i=1; i<=10; i++) {
            metrics.update(providerId1, providerType1, spaceName2, mimetype1, i);
        }
        for(int i=1; i<=5; i++) {
            metrics.update(providerId1, providerType1, spaceName2, mimetype2, i);
        }
        for(int i=1; i<=3; i++) {
            metrics.update(providerId1, providerType1, spaceName2, mimetype3, i);
        }

        // Provider 2, Space 1
        for(int i=1; i<=10; i++) {
            metrics.update(providerId2, providerType2, spaceName1, mimetype1, i);
        }
        for(int i=1; i<=5; i++) {
            metrics.update(providerId2, providerType2, spaceName1, mimetype2, i);
        }
        for(int i=1; i<=3; i++) {
            metrics.update(providerId2, providerType2, spaceName1, mimetype3, i);
        }

        // Provider 2, Space 2
        for(int i=1; i<=10; i++) {
            metrics.update(providerId2, providerType2, spaceName2, mimetype1, i);
        }
        for(int i=1; i<=5; i++) {
            metrics.update(providerId2, providerType2, spaceName2, mimetype2, i);
        }
        for(int i=1; i<=3; i++) {
            metrics.update(providerId2, providerType2, spaceName2, mimetype3, i);
        }

        // Verify durastore totals
        assertEquals(72, metrics.getTotalItems());
        assertEquals(304, metrics.getTotalSize());

        // Verify durastore mimetype totals
        Map<String, MimetypeMetricsCollector> mimetypeMetricsMap =
            metrics.getMimetypeMetrics();
        assertEquals(3, mimetypeMetricsMap.size());

        MimetypeMetricsCollector mimetype1Metrics = mimetypeMetricsMap.get(mimetype1);
        assertNotNull(mimetype1Metrics);
        assertEquals(mimetype1, mimetype1Metrics.getMimetype());
        assertEquals(40, mimetype1Metrics.getTotalItems());
        assertEquals(220, mimetype1Metrics.getTotalSize());

        MimetypeMetricsCollector mimetype2Metrics = mimetypeMetricsMap.get(mimetype2);
        assertNotNull(mimetype2Metrics);
        assertEquals(mimetype2, mimetype2Metrics.getMimetype());
        assertEquals(20, mimetype2Metrics.getTotalItems());
        assertEquals(60, mimetype2Metrics.getTotalSize());

        MimetypeMetricsCollector mimetype3Metrics = mimetypeMetricsMap.get(mimetype3);
        assertNotNull(mimetype3Metrics);
        assertEquals(mimetype3, mimetype3Metrics.getMimetype());
        assertEquals(12, mimetype3Metrics.getTotalItems());
        assertEquals(24, mimetype3Metrics.getTotalSize());

        // Verify storage provider metrics map
        Map<String, StorageProviderMetricsCollector> storageProviderMetricsMap =
            metrics.getStorageProviderMetrics();
        assertNotNull(storageProviderMetricsMap);
        assertEquals(2, storageProviderMetricsMap.size());

        // Verify storage provider 1 totals
        StorageProviderMetricsCollector spMetrics1 =
            storageProviderMetricsMap.get(providerId1);
        assertNotNull(spMetrics1);
        verifyStorageProviderMetrics(providerId1, spMetrics1);

        // Verify storage provider 2 totals
        StorageProviderMetricsCollector spMetrics2 =
            storageProviderMetricsMap.get(providerId2);
        assertNotNull(spMetrics2);
        verifyStorageProviderMetrics(providerId2, spMetrics2);
    }
}
