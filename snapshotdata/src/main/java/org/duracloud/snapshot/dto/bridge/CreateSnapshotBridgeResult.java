/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 7/24/14
 */
public class CreateSnapshotBridgeResult extends BaseDTO {

    /**
     * The ID which has been assigned to the snapshot
     */
    @XmlValue
    private String snapshotId;

    /**
     * The current status of the snapshot action
     */
    @XmlValue
    private SnapshotStatus status;

    // Required by JAXB
    public CreateSnapshotBridgeResult() {
    }

    public CreateSnapshotBridgeResult(String snapshotId, SnapshotStatus status) {
        this.snapshotId = snapshotId;
        this.status = status;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public SnapshotStatus getStatus() {
        return status;
    }

    public void setStatus(SnapshotStatus status) {
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
            throw new SnapshotDataException("Unable to create task result due to: " +
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
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
