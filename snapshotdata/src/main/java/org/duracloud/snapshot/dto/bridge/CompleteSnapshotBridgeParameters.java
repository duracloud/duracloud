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
 *         Date: 8/3/2015
 */
public class CompleteSnapshotBridgeParameters {

    /**
     * Identifiers to associate with this snapshot other than the snapshot-id
     */
    @XmlValue
    private List<String> alternateIds;

    public CompleteSnapshotBridgeParameters() {}

    public CompleteSnapshotBridgeParameters(List<String> alternateIds) {
        this.alternateIds = alternateIds;
    }

    public List<String> getAlternateIds() {
        return alternateIds;
    }

    public void setAlternateIds(List<String> alternateIds) {
        this.alternateIds = alternateIds;
    }

    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<CompleteSnapshotBridgeParameters> serializer =
            new JaxbJsonSerializer<>(CompleteSnapshotBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
