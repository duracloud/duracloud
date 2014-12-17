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
 *         Date: 7/29/14
 */
public class GetSnapshotTaskParameters extends BaseDTO {

    @XmlValue
    private String snapshotId;

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshotTaskParameters.class);
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
    public static GetSnapshotTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<GetSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshotTaskParameters.class);
        try {
            GetSnapshotTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSnapshotId() || params.getSnapshotId().isEmpty()) {
                throw new SnapshotDataException("Task parameter values may not be empty");
            }
            return params;
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }

}
