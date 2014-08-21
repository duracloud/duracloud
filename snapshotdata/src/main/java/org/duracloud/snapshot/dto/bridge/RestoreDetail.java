package org.duracloud.snapshot.dto.bridge;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.duracloud.snapshot.dto.RestoreStatus;

public class RestoreDetail {
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
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).build();
    }

}
