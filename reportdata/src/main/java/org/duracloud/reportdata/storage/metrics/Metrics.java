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
 * @author: Bill Branan
 * Date: 5/12/11
 */
public abstract class Metrics {

    private long totalItems;
    private long totalSize;

    private List<MimetypeMetrics> mimetypeMetrics;

    public Metrics(long totalItems,
                   long totalSize,
                   List<MimetypeMetrics> mimetypeMetrics) {
        this.totalItems = totalItems;
        this.totalSize = totalSize;
        this.mimetypeMetrics = mimetypeMetrics;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public List<MimetypeMetrics> getMimetypeMetrics() {
        return mimetypeMetrics;
    }

}
