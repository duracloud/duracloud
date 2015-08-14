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
 * @author Daniel Bernstein 
 *         Date: 08/05/15
 */
public class RestartSnapshotBridgeResult extends BaseDTO {

    /**
     * Describes in human parseable terms what happened.
     */
    @XmlValue
    private String description;

    /**
     * The current status of the snapshot action
     */
    @XmlValue
    private SnapshotStatus status;

    // Required by JAXB
    public RestartSnapshotBridgeResult() {
    }

    public RestartSnapshotBridgeResult(String description, SnapshotStatus status) {
        this.description = description;
        this.status = status;
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

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<RestartSnapshotBridgeResult> serializer =
            new JaxbJsonSerializer<>(RestartSnapshotBridgeResult.class);
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
    public static RestartSnapshotBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<RestartSnapshotBridgeResult> serializer =
            new JaxbJsonSerializer<>(RestartSnapshotBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
