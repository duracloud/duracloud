/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.duracloud.reportdata.bitintegrity.BitIntegrityReportResult;

/**
 * @author Daniel Bernstein
 *         Date: Sep 2, 2014
 */
@Entity
@Table(name = "bit_report")
public class BitIntegrityReport extends BaseEntity {
    @Column(nullable=false)
    private String account;
    @Column(nullable=false)
    private String storeId;
    @Column(nullable=false)
    private String spaceId;
    @Column(nullable=false, length=1024)
    private String reportContentId;
    @Column(nullable=false)
    private Date completionDate;
    @Column(nullable=false)
    private String reportSpaceId;
    
    private boolean display = true;
    
    @Enumerated(EnumType.STRING)
    private BitIntegrityReportResult result;
    
    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public String getStoreId() {
        return storeId;
    }
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    public String getSpaceId() {
        return spaceId;
    }
    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
    public String getReportContentId() {
        return reportContentId;
    }
    public void setReportContentId(String reportContentId) {
        this.reportContentId = reportContentId;
    }
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
    public String getReportSpaceId() {
        return reportSpaceId;
    }
    public void setReportSpaceId(String reportSpaceId) {
        this.reportSpaceId = reportSpaceId;
    }
    public boolean isDisplay() {
        return display;
    }
    public void setDisplay(boolean display) {
        this.display = display;
    }
}
