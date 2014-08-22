/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 1/30/14
 */
public class CreateSnapshotTaskParameters  extends BaseDTO {

    /**
     * The ID of the space in which the content to snapshot resides
     */
    @XmlValue
    private String spaceId;

    /**
     * User-supplied description of the snapshot
     */
    @XmlValue
    private String description;

    /**
     * The email address of the user, will be used for snapshot notifications
     */
    @XmlValue
    private String userEmail;

    // Required by JAXB
    public CreateSnapshotTaskParameters() {
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        JaxbJsonSerializer<CreateSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotTaskParameters.class);
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
    public static CreateSnapshotTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<CreateSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotTaskParameters.class);
        try {
            CreateSnapshotTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSpaceId() || params.getSpaceId().isEmpty() ||
               null == params.getDescription() || params.getDescription().isEmpty() ||
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
