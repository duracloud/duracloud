/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.metrics;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class MimetypeMetrics {

    private String mimetype;
    private long totalItems;
    private long totalSize;

    public MimetypeMetrics(String mimetype, long totalItems, long totalSize) {
        this.mimetype = mimetype;
        this.totalItems = totalItems;
        this.totalSize = totalSize;
    }

    public String getMimetype() {
        return mimetype;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalSize() {
        return totalSize;
    }

}
