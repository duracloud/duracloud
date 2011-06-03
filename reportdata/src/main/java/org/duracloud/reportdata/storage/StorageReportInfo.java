/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage;

/**
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportInfo {

    private String status;
    private long startTime;
    private long currentCount;
    private long finalCount;
    private long completionTime;
    private long estimatedCompletionTime;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(long currentCount) {
        this.currentCount = currentCount;
    }

    public long getFinalCount() {
        return finalCount;
    }

    public void setFinalCount(long finalCount) {
        this.finalCount = finalCount;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public long getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }

    public void setEstimatedCompletionTime(long estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

}
