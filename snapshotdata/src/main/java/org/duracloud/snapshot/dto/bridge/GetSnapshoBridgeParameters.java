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
 * @author Daniel Bernstein
 *         Date: 7/31/14
 */
public class GetSnapshoBridgeParameters {

    /**
     * The host name of the DuraCloud instance
     */
    @XmlValue
    private String sourceHost;


    public GetSnapshoBridgeParameters(){}

    public GetSnapshoBridgeParameters(String sourceHost) {
        this.setSourceHost(sourceHost);
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }
    
    
    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshoBridgeParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshoBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create  result due to: " + e.getMessage());
        }
    }
}
