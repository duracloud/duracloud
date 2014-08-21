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
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Daniel Bernstein
 *         Date: 7/31/14
 */
public class GetSnapshotListBridgeParameters extends BaseDTO {

    /**
     * The host name of the DuraCloud instance
     */
    @XmlValue
    private String host;


    public GetSnapshotListBridgeParameters(){}

    public GetSnapshotListBridgeParameters(String host) {
        this.setHost(host);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    
    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotListBridgeParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshotListBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create  result due to: " + e.getMessage());
        }
    }

}
