/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot.dto;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.storage.error.TaskException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 7/24/14
 */
public class CreateSnapshotBridgeResult {

    /**
     * The ID which has been assigned to the snapshot
     */
    @XmlValue
    private String snapshotId;

    /**
     * The current status of the snapshot action
     */
    @XmlValue
    private String status;

    // Required by JAXB
    public CreateSnapshotBridgeResult() {
    }

    public CreateSnapshotBridgeResult(String snapshotId, String status) {
        this.snapshotId = snapshotId;
        this.status = status;
    }

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

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<CreateSnapshotBridgeResult> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotBridgeResult.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new TaskException("Unable to create task result due to: " +
                                    e.getMessage());
        }
    }

    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static CreateSnapshotBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<CreateSnapshotBridgeResult> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new TaskException("Unable to create task result due to: " +
                                    e.getMessage());
        }
    }

}
