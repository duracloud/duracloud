/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot.dto;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Bill Branan
 *         Date: 7/24/14
 */
public class CreateSnapshotBridgeResult {

    @XmlValue
    private String snapshotId;

    @XmlValue
    private String status;

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
