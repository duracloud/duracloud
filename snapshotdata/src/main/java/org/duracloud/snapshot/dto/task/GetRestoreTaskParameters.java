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

import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * This class is used for passing either a snapshotId or a restoreId to the
 * GetRestoreTaskRunner.
 * 
 * @author Daniel Bernstein 
 *         Date: 8/7/14
 */
public class GetRestoreTaskParameters extends BaseDTO {

    @XmlValue
    private String snapshotId;

    @XmlValue
    private String restoreId;

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        check(snapshotId, restoreId);
        this.snapshotId = snapshotId;
    }

    private void check(String snapshotId, String restoreId) {
        if(!StringUtils.isBlank(snapshotId) && !StringUtils.isBlank(restoreId)){
            throw new IllegalArgumentException(
                "EITHER a snapshotId or a restoreId may be set, but not both");
        }
    }

    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String restoreId) {
        check(snapshotId, restoreId);
        this.restoreId = restoreId;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetRestoreTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetRestoreTaskParameters.class);
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
    public static GetRestoreTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<GetRestoreTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetRestoreTaskParameters.class);
        try {
            GetRestoreTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(StringUtils.isBlank(params.getSnapshotId()) && StringUtils.isBlank(params.getRestoreId())) {
                throw new SnapshotDataException("Task parameter values may not be empty");
            }
            return params;
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }
    
}
