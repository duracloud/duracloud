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
 * @author: Bill Branan
 * Date: 5/12/11
 */
public abstract class MetricsCollector {

    private long totalItems;
    private long totalSize;

    private Map<String, MimetypeMetricsCollector> mimetypeMetrics;

    public MetricsCollector() {
        this.totalItems = 0;
        this.totalSize = 0;
        this.mimetypeMetrics = new HashMap<String, MimetypeMetricsCollector>();
    }

    public void update(String mimetype, long size) {
        ++totalItems;
        totalSize += size;

        MimetypeMetricsCollector mimeMet = mimetypeMetrics.get(mimetype);
        if(null == mimeMet) {
            mimeMet = new MimetypeMetricsCollector(mimetype);
            mimetypeMetrics.put(mimetype, mimeMet);
        }
        mimeMet.update(size);
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public Map<String, MimetypeMetricsCollector> getMimetypeMetrics() {
        return mimetypeMetrics;
    }

}
