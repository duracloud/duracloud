/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage.metrics;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class MimetypeMetrics {

    private String mimetype;
    private long totalItems;
    private long totalSize;

    public MimetypeMetrics(String mimetype) {
        this.mimetype = mimetype;
        this.totalItems = 0;
        this.totalSize = 0;
    }

    public void update(long size) {
        ++totalItems;
        totalSize += size;
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
