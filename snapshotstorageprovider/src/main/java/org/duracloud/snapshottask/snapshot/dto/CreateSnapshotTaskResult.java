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
 *         Date: 1/30/14
 */
public class CreateSnapshotTaskResult {

    public CreateSnapshotTaskResult() {
    }

    @XmlValue
    private String snapshotId;

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

}
