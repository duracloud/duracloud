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
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;
import java.util.List;

/**
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class GetSnapshotContentBridgeResult extends BaseDTO {

    @XmlValue
    private Long totalCount;
    /**
     * The details of the current status
     */
    @XmlValue
    private List<SnapshotContentItem> contentItems;

    public List<SnapshotContentItem> getContentItems() {
        return contentItems;
    }

    public void setContentItems(List<SnapshotContentItem> contentItems) {
        this.contentItems = contentItems;
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
        JaxbJsonSerializer<GetSnapshotContentBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotContentBridgeResult.class);
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
    public static GetSnapshotContentBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetSnapshotContentBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotContentBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }

}
