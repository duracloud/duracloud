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
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Nicholas Woodward
 * Date: 7/15/21
 */
public class GetSnapshotTotalSizeBridgeResult extends BaseDTO {

    /**
     * The details of the current status
     */
    @XmlValue
    private Long totalSize;

    public GetSnapshotTotalSizeBridgeResult() {
    }

    public GetSnapshotTotalSizeBridgeResult(Long totalSize) {
        this.totalSize = totalSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotTotalSizeBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotTotalSizeBridgeResult.class);
        try {
            return serializer.serialize(this);
        } catch (IOException e) {
            throw new SnapshotDataException("Unable to create task result due to: " +
                                            e.getMessage());
        }
    }

    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static GetSnapshotTotalSizeBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetSnapshotTotalSizeBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotTotalSizeBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch (IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }
}
