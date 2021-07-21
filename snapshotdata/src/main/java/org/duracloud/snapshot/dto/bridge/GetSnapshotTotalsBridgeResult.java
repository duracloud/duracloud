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
public class GetSnapshotTotalsBridgeResult extends BaseDTO {

    /**
     * The details of the current status
     */
    @XmlValue
    private Long totalCount;

    @XmlValue
    private Long totalSize;

    @XmlValue
    private Long totalFiles;

    public GetSnapshotTotalsBridgeResult() {
    }

    public GetSnapshotTotalsBridgeResult(Long totalCount, Long totalSize, Long totalFiles) {
        this.totalCount = totalCount;
        this.totalSize = totalSize;
        this.totalFiles = totalFiles;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(Long totalFiles) {
        this.totalFiles = totalFiles;
    }

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotTotalsBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotTotalsBridgeResult.class);
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
    public static GetSnapshotTotalsBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetSnapshotTotalsBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotTotalsBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch (IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }
}
