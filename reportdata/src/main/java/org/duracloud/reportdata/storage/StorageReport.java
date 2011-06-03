/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage;

import org.duracloud.reportdata.storage.metrics.StorageMetrics;

/**
 * @author: Bill Branan
 * Date: 6/1/11
 */
public class StorageReport {

    private String contentId;
    private StorageMetrics storageMetrics;
    private long completionTime;
    private long elapsedTime;

    public StorageReport(String contentId,
                         StorageMetrics storageMetrics,
                         long completionTime,
                         long elapsedTime) {
        this.contentId = contentId;
        this.storageMetrics = storageMetrics;
        this.completionTime = completionTime;
        this.elapsedTime = elapsedTime;
    }

    public String getContentId() {
        return contentId;
    }

    public StorageMetrics getStorageMetrics() {
        return storageMetrics;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
}
