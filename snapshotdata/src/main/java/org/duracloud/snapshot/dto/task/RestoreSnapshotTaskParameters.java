/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import java.io.IOException;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Bill Branan
 *         Date: 7/30/14
 */
public class RestoreSnapshotTaskParameters extends BaseDTO {

    /**
     * The ID of the snapshot to be restored
     */
    @XmlValue
    private String snapshotId;

    /**
     * The email address of the user, will be used for snapshot notifications
     */
    @XmlValue
    private String userEmail;

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
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<RestoreSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(RestoreSnapshotTaskParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task parameters due to: " + e.getMessage());
        }
    }

    /**
     * Parses properties from task parameter string
     *
     * @param taskParameters - JSON formatted set of parameters
     */
    public static RestoreSnapshotTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<RestoreSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(RestoreSnapshotTaskParameters.class);
        try {
            RestoreSnapshotTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSnapshotId() || params.getSnapshotId().isEmpty() ||
               null == params.getUserEmail() || params.getUserEmail().isEmpty()) {
                throw new SnapshotDataException("Task parameter values may not be empty");
            }
            return params;
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }

}
