/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class GetSnapshotBridgeResult extends BaseDTO {

    @XmlValue
    private String snapshotId;

    @XmlValue
    private Date snapshotDate;

    @XmlValue
    private SnapshotStatus status;

    @XmlValue
    private String sourceHost;

    @XmlValue
    private String sourceSpaceId;

    @XmlValue
    private String sourceStoreId;

    @XmlValue
    private String description;

    @XmlValue
    private Long contentItemCount;
    
    @XmlValue
    private Long totalSizeInBytes;
    
    @XmlValue
    private String memberId;
    
    @XmlValue
    private List<String> alternateIds;
        
    public GetSnapshotBridgeResult(){}

 
    public SnapshotStatus getStatus() {
        return status;
    }

    public void setStatus(SnapshotStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Date getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public void setSourceSpaceId(String sourceSpaceId) {
        this.sourceSpaceId = sourceSpaceId;
    }

    public String getSourceStoreId() {
        return sourceStoreId;
    }

    public void setSourceStoreId(String sourceStoreId) {
        this.sourceStoreId = sourceStoreId;
    }

    public Long getContentItemCount() {
        return contentItemCount;
    }

    public void setContentItemCount(Long contentItemCount) {
        this.contentItemCount = contentItemCount;
    }

    public Long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public void setTotalSizeInBytes(Long totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }
    
    public List<String> getAlternateIds() {
        return alternateIds;
    }
    
    public void setAlternateIds(List<String> alternateIds) {
        this.alternateIds = alternateIds;
    }

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotBridgeResult.class);
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
    public static GetSnapshotBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetSnapshotBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }


    public String getMemberId() {
        return memberId;
    }


    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    
}
