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
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;
import java.util.Date;

/**
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class GetRestoreBridgeResult extends BaseDTO {

    @XmlValue
    private String restoreId;
    @XmlValue
    private String snapshotId;
    @XmlValue
    private RestoreStatus status;
    @XmlValue
    private Date startDate;
    @XmlValue
    private Date endDate;
    @XmlValue
    private Date expirationDate;
    @XmlValue
    private String statusText;

    @XmlValue
    private String destinationHost;
    @XmlValue
    private int destinationPort;
    @XmlValue
    private String destinationStoreId;
    @XmlValue
    private String destinationSpaceId;
    
    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String restoreId) {
        this.restoreId = restoreId;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public RestoreStatus getStatus() {
        return status;
    }

    public void setStatus(RestoreStatus status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getDestinationHost() {
        return destinationHost;
    }

    public void setDestinationHost(String destinationHost) {
        this.destinationHost = destinationHost;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getDestinationStoreId() {
        return destinationStoreId;
    }

    public void setDestinationStoreId(String destinationStoreId) {
        this.destinationStoreId = destinationStoreId;
    }

    public String getDestinationSpaceId() {
        return destinationSpaceId;
    }

    public void setDestinationSpaceId(String destinationSpaceId) {
        this.destinationSpaceId = destinationSpaceId;
    }

    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static GetRestoreBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetRestoreBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetRestoreBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }

}
