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
 *         Date: 7/25/14
 */
public class CompleteSnapshotTaskParameters extends BaseDTO {

    /**
     * The ID of the space in which the content to snapshot resides
     */
    @XmlValue
    private String spaceId;

    // Required by JAXB
    public CompleteSnapshotTaskParameters() {
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<CompleteSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(CompleteSnapshotTaskParameters.class);
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
    public static CompleteSnapshotTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<CompleteSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(CompleteSnapshotTaskParameters.class);
        try {
            CompleteSnapshotTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSpaceId() || params.getSpaceId().isEmpty()) {
                throw new SnapshotDataException(
                    "Task parameter values may not be empty");
            }
            return params;
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }

}
