/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import org.duracloud.durareport.storage.metrics.DuraStoreMetrics;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Bill Branan
 * Date: 5/17/11
 */
public class MetricsSerializerTest {

    @Test
    public void testMetricsSerializer() {
        DuraStoreMetrics metrics = new DuraStoreMetrics();
        metrics.update("provider-1", "provoder-type-1", "space-1", "text/plain", 1);
        metrics.update("provider-1", "provoder-type-1", "space-1", "text/plain", 2);
        metrics.update("provider-1", "provoder-type-1", "space-2", "text/plain", 3);
        metrics.update("provider-2", "provoder-type-2", "space-A", "text/plain", 4);

        MetricsSerializer serializer = new MetricsSerializer();
        String xml = serializer.serializeMetrics(metrics);
        assertNotNull(xml);

        DuraStoreMetrics metricsDeserialized =
            serializer.deserializeMetrics(xml);
        assertEquals(metrics.getTotalItems(),
                     metricsDeserialized.getTotalItems());
        assertEquals(metrics.getTotalSize(),
                     metricsDeserialized.getTotalSize());
        assertEquals(metrics.getStorageProviderMetrics().size(),
                     metricsDeserialized.getStorageProviderMetrics().size());
    }
}
