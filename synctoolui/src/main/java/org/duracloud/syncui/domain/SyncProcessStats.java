/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import java.util.Date;

/**
 * A Read-only data object deCribing the state of the sync tool from the user's
 * perspective.
 * 
 * @author Daniel Bernstein
 * 
 */

public class SyncProcessStats {
    private Date startDate;
    private Date estimatedCompletionDate;
    private int errorCount;
    private long currentUpBytesPerSecond;
    private long averageUpBytesPerSecond;
    private int queueSize;
    public SyncProcessStats() {
        this.startDate = new Date();
        this.estimatedCompletionDate = new Date();
        this.errorCount = 0;
        this.currentUpBytesPerSecond = 2 * 1000 * 1000;
        this.averageUpBytesPerSecond = 2 * 1000 * 1000;
        this.queueSize = 0;
    }

    public SyncProcessStats(Date startDate, Date estimatedCompletionDate,
        int errorCount, long currentUpBytesPerSecond,
        long averageUpBytesPerSecond, int queueSize) {
        this.startDate = startDate;
        this.estimatedCompletionDate = estimatedCompletionDate;
        this.errorCount = errorCount;
        this.currentUpBytesPerSecond = currentUpBytesPerSecond;
        this.averageUpBytesPerSecond = averageUpBytesPerSecond;
        this.queueSize = queueSize;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public long getCurrentUpBytesPerSecond() {
        return currentUpBytesPerSecond;
    }

    public long getAverageUpBytesPerSecond() {
        return averageUpBytesPerSecond;
    }
    
    public int getQueueSize(){
        return this.queueSize;
    }
}
