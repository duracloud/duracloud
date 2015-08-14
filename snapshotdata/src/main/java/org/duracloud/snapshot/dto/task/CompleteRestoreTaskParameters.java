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
 *         Date: 7/29/15
 */
public class CompleteRestoreTaskParameters extends BaseDTO {

    /**
     * The ID of the space in which the restored content resides
     */
    @XmlValue
    private String spaceId;

    /**
     * The number of days that restored content will remain in place before
     * it expires and is removed
     */
    @XmlValue
    private int daysToExpire;

    // Required by JAXB
    public CompleteRestoreTaskParameters() {
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public int getDaysToExpire() {
        return daysToExpire;
    }

    public void setDaysToExpire(int daysToExpire) {
        this.daysToExpire = daysToExpire;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<CompleteRestoreTaskParameters> serializer =
            new JaxbJsonSerializer<>(CompleteRestoreTaskParameters.class);
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
    public static CompleteRestoreTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<CompleteRestoreTaskParameters> serializer =
            new JaxbJsonSerializer<>(CompleteRestoreTaskParameters.class);
        try {
            CompleteRestoreTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSpaceId() || params.getSpaceId().isEmpty()) {
                throw new SnapshotDataException(
                    "Task parameter values may not be empty");
            }
            if(params.getDaysToExpire() < 0) {
                throw new SnapshotDataException(
                    "Task parameter value must be a positive integer");
            }
            return params;
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }

}
