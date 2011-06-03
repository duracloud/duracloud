/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.metrics;

import java.util.List;

/**
 * Top level metrics storage data structure for DuraStore. Contains all
 * metrics information for all storage providers.
 *
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class StorageMetrics extends Metrics {

    private List<StorageProviderMetrics> storageProviderMetrics;

    public StorageMetrics(List<StorageProviderMetrics> storageProviderMetrics,
                          long totalItems,
                          long totalSize,
                          List<MimetypeMetrics> mimetypeMetrics) {
        super(totalItems, totalSize, mimetypeMetrics);
        this.storageProviderMetrics = storageProviderMetrics;
    }

    public List<StorageProviderMetrics> getStorageProviderMetrics() {
        return storageProviderMetrics;
    }
}
