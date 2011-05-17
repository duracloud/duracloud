/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 5/17/11
 */
public class StorageReport {

    private String contentId;
    private InputStream reportStream;
    private long completionTime;
    private long elapsedTime;

    public StorageReport(String contentId,
                         InputStream reportStream,
                         long completionTime,
                         long elapsedTime) {
        this.contentId = contentId;
        this.reportStream = reportStream;
        this.completionTime = completionTime;
        this.elapsedTime = elapsedTime;
    }

    public String getContentId() {
        return contentId;
    }

    public InputStream getReportStream() {
        return reportStream;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
}
