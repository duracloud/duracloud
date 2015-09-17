/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;
import java.util.List;

/**
 * @author Bill Branan
 *         Date: 9/17/2015
 */
public class SnapshotErrorBridgeParameters {

    /**
     * Indicates the error which has stopped the snapshot process
     */
    @XmlValue
    private String error;

    public SnapshotErrorBridgeParameters() {}

    public SnapshotErrorBridgeParameters(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<SnapshotErrorBridgeParameters> serializer =
            new JaxbJsonSerializer<>(SnapshotErrorBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
