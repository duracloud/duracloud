/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reporter.storage.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Top level metrics storage data structure for DuraStore. Contains all
 * metrics information for all storage providers.
 *
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class DuraStoreMetricsCollector extends MetricsCollector {

    private Map<String, StorageProviderMetricsCollector> storageProviderMetrics;

    public DuraStoreMetricsCollector() {
        super();
        this.storageProviderMetrics =
            new HashMap<String, StorageProviderMetricsCollector>();
    }

    @Override
    public void update(String mimetype, long size) {
        String error = "Use update(String, String, String, long)";
        throw new UnsupportedOperationException(error);
    }

    public void update(String storageProviderId,
                       String storageProviderType,
                       String spaceId,
                       String mimetype,
                       long size) {
        super.update(mimetype, size);

        StorageProviderMetricsCollector providerMet =
            storageProviderMetrics.get(storageProviderId);
        if(null == providerMet) {
            providerMet = new StorageProviderMetricsCollector(storageProviderId,
                                                     storageProviderType);
            storageProviderMetrics.put(storageProviderId, providerMet);
        }
        providerMet.update(spaceId, mimetype, size);
    }

    public Map<String, StorageProviderMetricsCollector> getStorageProviderMetrics() {
        return storageProviderMetrics;
    }
}
