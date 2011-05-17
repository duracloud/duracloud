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
public abstract class Metrics {

    private long totalItems;
    private long totalSize;

    private Map<String, MimetypeMetrics> mimetypeMetrics;

    public Metrics() {
        this.totalItems = 0;
        this.totalSize = 0;
        this.mimetypeMetrics = new HashMap<String, MimetypeMetrics>();
    }

    public void update(String mimetype, long size) {
        ++totalItems;
        totalSize += size;

        MimetypeMetrics mimeMet = mimetypeMetrics.get(mimetype);
        if(null == mimeMet) {
            mimeMet = new MimetypeMetrics(mimetype);
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

    public Map<String, MimetypeMetrics> getMimetypeMetrics() {
        return mimetypeMetrics;
    }

}
