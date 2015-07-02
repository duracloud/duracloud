/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.io.IOException;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Gad Krumholz
 *         Date: 6/03/15
 */
public class UpdateSnapshotHistoryBridgeResult extends BaseDTO {

    /**
     * The snapshot status 
     */
    @XmlValue
    private SnapshotSummary snapshot;

    /**
     * The latest history of the current snapshot
     */
    @XmlValue
    private String history;

    public UpdateSnapshotHistoryBridgeResult(){}

    public UpdateSnapshotHistoryBridgeResult(SnapshotSummary snapshot,
                                          String history) {
        this.snapshot = snapshot;
        this.history = history;
    }

    public SnapshotSummary getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(SnapshotSummary snapshot) {
        this.snapshot = snapshot;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
    
    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<UpdateSnapshotHistoryBridgeResult> serializer =
            new JaxbJsonSerializer<>(UpdateSnapshotHistoryBridgeResult.class);
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
    public static UpdateSnapshotHistoryBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<UpdateSnapshotHistoryBridgeResult> serializer =
            new JaxbJsonSerializer<>(UpdateSnapshotHistoryBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }

}
