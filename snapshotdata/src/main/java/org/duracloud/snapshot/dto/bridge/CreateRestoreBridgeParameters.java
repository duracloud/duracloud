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
 * @author Bill Branan
 *         Date: 7/24/14
 */
public class CreateRestoreBridgeParameters extends BaseDTO {

    /**
     * The host name of the DuraCloud instance
     */
    @XmlValue
    private String host;

    /**
     * The port on which DuraCloud is available
     */
    @XmlValue
    private String port;

    /**
     * The ID of the storage provider where the snapshot will be restored
     */
    @XmlValue
    private String storeId;

    /**
     * The ID of the space where the snapshot will be restored.
     */
    @XmlValue
    private String spaceId;

    /**
     * User-supplied Snapshot id of the snapshot
     */
    @XmlValue
    private String snapshotId;

    /**
     * The email address of the user, will be used for restoration notifications
     */
    @XmlValue
    private String userEmail;

    public CreateRestoreBridgeParameters(){}

    public CreateRestoreBridgeParameters(String host,
                                          String port,
                                          String storeId,
                                          String spaceId,
                                          String snapshotId,
                                          String userEmail) {
        this.host = host;
        this.port = port;
        this.storeId = storeId;
        this.spaceId = spaceId;
        this.snapshotId = snapshotId;
        this.userEmail = userEmail;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<CreateRestoreBridgeParameters> serializer =
            new JaxbJsonSerializer<>(CreateRestoreBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create  result due to: " + e.getMessage());
        }
    }

}
