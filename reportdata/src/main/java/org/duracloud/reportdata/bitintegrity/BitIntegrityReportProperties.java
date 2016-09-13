/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.bitintegrity;

import java.util.Date;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class BitIntegrityReportProperties {
    private Date completionDate;
    private BitIntegrityReportResult result;
    private long size; 
    
    public Date getCompletionDate() {
        return completionDate;
    }
    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }
    public BitIntegrityReportResult getResult() {
        return result;
    }
    public void setResult(BitIntegrityReportResult result) {
        this.result = result;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
}
