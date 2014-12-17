/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.dto.RestoreStatus;

import java.util.Date;

public class RestoreDetail extends BaseDTO {

    private Long id;
    private String snapshotId;
    private RestoreStatus status;
    private Date startDate;
    private Date endDate;
    private String statusText;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSnapshotId() {
        return snapshotId;
    }
    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }
    public RestoreStatus getStatus() {
        return status;
    }
    public void setStatus(RestoreStatus status) {
        this.status = status;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public String getStatusText() {
        return statusText;
    }
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

}
