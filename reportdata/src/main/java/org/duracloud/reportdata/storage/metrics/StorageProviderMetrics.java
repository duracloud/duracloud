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
 * Metrics data structure for storage providers. Contains all of the metrics
 * information about a single storage provider and its storage spaces.
 *
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class StorageProviderMetrics extends Metrics {

    private String storageProviderId;
    private String storageProviderType;
    private List<SpaceMetrics> spaceMetrics;

    public StorageProviderMetrics(String storageProviderId,
                                  String storageProviderType,
                                  List<SpaceMetrics> spaceMetrics,
                                  long totalItems,
                                  long totalSize,
                                  List<MimetypeMetrics> mimetypeMetrics) {
        super(totalItems, totalSize, mimetypeMetrics);
        this.storageProviderId = storageProviderId;
        this.storageProviderType = storageProviderType;
        this.spaceMetrics = spaceMetrics;
    }

    public String getStorageProviderId() {
        return storageProviderId;
    }

    public String getStorageProviderType() {
        return storageProviderType;
    }

    public List<SpaceMetrics> getSpaceMetrics() {
        return spaceMetrics;
    }
}
