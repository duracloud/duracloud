/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import org.duracloud.durareport.storage.metrics.DuraStoreMetricsCollector;
import org.duracloud.durareport.storage.metrics.MimetypeMetricsCollector;
import org.duracloud.durareport.storage.metrics.SpaceMetricsCollector;
import org.duracloud.durareport.storage.metrics.StorageProviderMetricsCollector;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.MimetypeMetrics;
import org.duracloud.reportdata.storage.metrics.SpaceMetrics;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 6/1/11
 */
public class StorageReportConverter {

    public StorageReport createStorageReport(String contentId,
                                             DuraStoreMetricsCollector metrics,
                                             long completionTime,
                                             long elapsedTime) {
        List<StorageProviderMetrics> spMetrics =
            new LinkedList<StorageProviderMetrics>();
        for(StorageProviderMetricsCollector provider :
            metrics.getStorageProviderMetrics().values()) {

            List<SpaceMetrics> spaceMetrics = new LinkedList<SpaceMetrics>();
            for(SpaceMetricsCollector space :
                provider.getSpaceMetrics().values()) {
                spaceMetrics.add(
                    new SpaceMetrics(space.getSpaceName(),
                                     space.getTotalItems(),
                                     space.getTotalSize(),
                                     convertMime(space.getMimetypeMetrics())));
            }

            spMetrics.add(
                new StorageProviderMetrics(provider.getStorageProviderId(),
                                           provider.getStorageProviderType(),
                                           spaceMetrics,
                                           provider.getTotalItems(),
                                           provider.getTotalSize(),
                                           convertMime(
                                               provider.getMimetypeMetrics())));
        }

        StorageMetrics storageMetrics =
            new StorageMetrics(spMetrics,
                               metrics.getTotalItems(),
                               metrics.getTotalSize(),
                               convertMime(metrics.getMimetypeMetrics()));

        return new StorageReport(contentId,
                                 storageMetrics,
                                 completionTime,
                                 elapsedTime);
    }

    private List<MimetypeMetrics> convertMime(
        Map<String, MimetypeMetricsCollector> mimeMetrics) {
        List<MimetypeMetrics> converted = new LinkedList<MimetypeMetrics>();
        for(MimetypeMetricsCollector mime :
            mimeMetrics.values()) {
            converted.add(new MimetypeMetrics(mime.getMimetype(),
                                              mime.getTotalItems(),
                                              mime.getTotalSize()));
        }
        return converted;
    }

}
