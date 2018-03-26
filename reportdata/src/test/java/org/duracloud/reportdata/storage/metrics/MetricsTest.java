/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.duracloud.reportdata.storage.ReportTestHelper;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class MetricsTest {

    @Test
    public void testCreateMetrics() {
        ReportTestHelper testHelper = new ReportTestHelper();
        StorageMetrics storageMetrics = testHelper.createMetrics();

        assertNotNull(storageMetrics);
        assertEquals(24, storageMetrics.getTotalItems());
        assertEquals(2400, storageMetrics.getTotalSize());

        List<StorageProviderMetrics> providerMetrics =
            storageMetrics.getStorageProviderMetrics();
        assertNotNull(providerMetrics);
        assertEquals(2, providerMetrics.size());

        for (StorageProviderMetrics provider : providerMetrics) {
            assertEquals(12, provider.getTotalItems());
            assertEquals(1200, provider.getTotalSize());

            List<SpaceMetrics> spaceMetrics = provider.getSpaceMetrics();
            assertEquals(2, spaceMetrics.size());

            for (SpaceMetrics space : spaceMetrics) {
                assertEquals(6, space.getTotalItems());
                assertEquals(600, space.getTotalSize());
                assertEquals(3, space.getMimetypeMetrics().size());
            }

            assertEquals(3, provider.getMimetypeMetrics().size());
        }

        assertEquals(3, storageMetrics.getMimetypeMetrics().size());
    }

}
