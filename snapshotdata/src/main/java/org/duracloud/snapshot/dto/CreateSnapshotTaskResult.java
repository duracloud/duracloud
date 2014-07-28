/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 1/30/14
 */
public class CreateSnapshotTaskResult {

    /**
     * The ID which has been assigned to the snapshot
     */
    @XmlValue
    private String snapshotId;

    public CreateSnapshotTaskResult(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<CreateSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotTaskResult.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
