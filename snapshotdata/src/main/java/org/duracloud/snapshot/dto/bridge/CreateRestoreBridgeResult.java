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

/**
 * @author Bill Branan
 *         Date: 7/24/14
 */
public class CreateRestoreBridgeResult extends BaseDTO {

    /**
     * The ID which has been assigned to the restoration
     */
    @XmlValue
    private String restoreId;

    /**
     * The current status of the restore action
     */
    @XmlValue
    private RestoreStatus status;

    // Required by JAXB
    public CreateRestoreBridgeResult() {
    }

    public CreateRestoreBridgeResult(String restoreId, RestoreStatus status) {
        this.restoreId = restoreId;
        this.status = status;
    }

    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String restoreId) {
        this.restoreId = restoreId;
    }

    public RestoreStatus getStatus() {
        return status;
    }

    public void setStatus(RestoreStatus status) {
        this.status = status;
    }

    /**
     * Creates a serialized version of bridge result
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<CreateRestoreBridgeResult> serializer =
            new JaxbJsonSerializer<>(CreateRestoreBridgeResult.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException("Unable to create result due to: " +
                                            e.getMessage());
        }
    }

    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static CreateRestoreBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<CreateRestoreBridgeResult> serializer =
            new JaxbJsonSerializer<>(CreateRestoreBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create result due to: " + e.getMessage());
        }
    }

}
