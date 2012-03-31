/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class StorageSummary {
    protected final Long date;
    protected final Long totalSize;
    protected final Long totalItems;
    protected final String reportId;

    public StorageSummary(long date, long totalSize, long totalItems, String reportId) {
        super();
        this.date = date;
        this.totalSize = totalSize;
        this.totalItems = totalItems;
        this.reportId = reportId;
    }
    public Long getDate() {
        return date;
    }
    public Long getTotalSize() {
        return totalSize;
    }
    public Long getTotalItems() {
        return totalItems;
    }
    public String getReportId() {
        return reportId;
    }
}

