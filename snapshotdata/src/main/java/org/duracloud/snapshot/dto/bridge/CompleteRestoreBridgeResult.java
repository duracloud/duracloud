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
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class CompleteRestoreBridgeResult extends BaseDTO {

    /**
     * The restore status 
     */
    @XmlValue
    private RestoreStatus status;

    /**
     * The details of the current status
     */
    @XmlValue
    private String details;

    public CompleteRestoreBridgeResult(){}

    public CompleteRestoreBridgeResult(RestoreStatus status,
                                          String details) {
        this.status = status;
        this.details = details;
    }

    public RestoreStatus getStatus() {
        return status;
    }

    public void setStatus(RestoreStatus status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }


    /**
     * Creates a deserialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public static CompleteRestoreBridgeResult deserialize(String json) {
        JaxbJsonSerializer<CompleteRestoreBridgeResult> serializer =
            new JaxbJsonSerializer<>(CompleteRestoreBridgeResult.class);
        try {
            return serializer.deserialize(json);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create result due to: " + e.getMessage());
        }
    }

}
