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
import org.duracloud.snapshot.dto.SnapshotHistoryItem;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Gad Krumholz
 *         Date: 6/23/15
 */
public class GetSnapshotHistoryBridgeResult extends BaseDTO {

    @XmlValue
    private Long totalCount;
    /**
     * The details of the current status
     */
    @XmlValue
    private List<SnapshotHistoryItem> historyItems;

    public List<SnapshotHistoryItem> getHistoryItems() {
        return historyItems;
    }

    public void setHistoryItems(List<SnapshotHistoryItem> historyItems) {
        this.historyItems = historyItems;
    }
    
    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotHistoryBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotHistoryBridgeResult.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException("Unable to create bridge result due to: " +
                                            e.getMessage());
        }
    }

    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static GetSnapshotHistoryBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetSnapshotHistoryBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotHistoryBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }

}