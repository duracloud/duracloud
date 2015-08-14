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

/**
 * @author Bill Branan
 *         Date: 8/3/2015
 */
public class UpdateSnapshotHistoryBridgeParameters {

    /**
     * Specifies whether the provided ID is the snapshot-id or an alternate
     */
    @XmlValue
    private Boolean alternate;

    /**
     * New history value to attach
     */
    @XmlValue
    private String history;

    public UpdateSnapshotHistoryBridgeParameters() {}

    public UpdateSnapshotHistoryBridgeParameters(Boolean alternate, String history) {
        this.alternate = alternate;
        this.history = history;
    }

    public Boolean getAlternate() {
        return alternate;
    }

    public void setAlternate(Boolean alternate) {
        this.alternate = alternate;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }


    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<UpdateSnapshotHistoryBridgeParameters> serializer =
            new JaxbJsonSerializer<>(UpdateSnapshotHistoryBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
