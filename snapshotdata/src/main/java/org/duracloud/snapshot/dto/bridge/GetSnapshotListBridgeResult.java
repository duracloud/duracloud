/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class GetSnapshotListBridgeResult extends BaseDTO {

    /**
     * The details of the current status
     */
    @XmlValue
    private List<SnapshotSummary> snapshots;

    public GetSnapshotListBridgeResult(){}

    public GetSnapshotListBridgeResult(List<SnapshotSummary> snapshots) {
        this.setSnapshots(snapshots);
    }

    public List<SnapshotSummary> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<SnapshotSummary> snapshots) {
        this.snapshots = snapshots;
    }

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotListBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotListBridgeResult.class);
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
    public static GetSnapshotListBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetSnapshotListBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotListBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }
    
}
