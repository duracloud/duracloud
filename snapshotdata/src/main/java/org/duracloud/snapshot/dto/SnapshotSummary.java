/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Daniel Bernstein Date: 7/28/14
 */
public class SnapshotSummary extends BaseDTO {

    @XmlValue
    private String snapshotId;
    @XmlValue
    private String description;

    @XmlValue
    private String sourceSpaceId;

    @XmlValue
    private String sourceStoreId;

    
    @XmlValue
    private SnapshotStatus status;
    
    public SnapshotSummary() {}
    
    public SnapshotSummary(String snapshotId,
                           SnapshotStatus status,
                           String description,
                           String sourceStoreId,
                           String sourceSpaceId) {
        super();
        this.snapshotId = snapshotId;
        this.description = description;
        this.status = status;
        this.sourceStoreId = sourceStoreId;
        this.sourceSpaceId = sourceSpaceId;
    } 

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SnapshotStatus getStatus() {
        return status;
    }

    public void setStatus(SnapshotStatus status) {
        this.status = status;
    }

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public void setSourceSpaceId(String sourceSpaceId) {
        this.sourceSpaceId = sourceSpaceId;
    }

    public String getSourceStoreId() {
        return sourceStoreId;
    }

    public void setSourceStoreId(String sourceStoreId) {
        this.sourceStoreId = sourceStoreId;
    }

}
