/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class CompleteRestoreBridgeResult {

    /**
     * The restore status 
     */
    @XmlValue
    private String status;

    /**
     * The details of the current status
     */
    @XmlValue
    private String details;

    public CompleteRestoreBridgeResult(){}

    public CompleteRestoreBridgeResult(String status,
                                          String details) {
        this.status = status;
        this.details = details;
    }

    
    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<CompleteRestoreBridgeResult> serializer =
            new JaxbJsonSerializer<>(CompleteRestoreBridgeResult.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create result due to: " + e.getMessage());
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}
