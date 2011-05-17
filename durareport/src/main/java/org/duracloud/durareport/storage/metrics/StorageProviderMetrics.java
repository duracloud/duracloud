/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage.metrics;

import java.util.HashMap;
import java.util.Map;

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
    private Map<String, SpaceMetrics> spaceMetrics;

    public StorageProviderMetrics(String storageProviderId,
                                  String storageProviderType) {
        super();
        this.storageProviderId = storageProviderId;
        this.storageProviderType = storageProviderType;
        this.spaceMetrics = new HashMap<String, SpaceMetrics>();
    }

    @Override
    public void update(String mimetype, long size) {
        String error = "Use update(String, String, long)";
        throw new UnsupportedOperationException(error);
    }

    public void update(String spaceId, String mimetype, long size) {
        super.update(mimetype, size);

        SpaceMetrics spaceMet = spaceMetrics.get(spaceId);
        if(null == spaceMet) {
            spaceMet = new SpaceMetrics(spaceId);
            spaceMetrics.put(spaceId, spaceMet);
        }
        spaceMet.update(mimetype, size);
    }

    public String getStorageProviderId() {
        return storageProviderId;
    }

    public String getStorageProviderType() {
        return storageProviderType;
    }

    public Map<String, SpaceMetrics> getSpaceMetrics() {
        return spaceMetrics;
    }
}
