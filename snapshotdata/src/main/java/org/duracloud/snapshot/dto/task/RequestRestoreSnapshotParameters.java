/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import java.io.IOException;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.bridge.RequestRestoreBridgeParameters;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Daniel Bernstein
 *         Date: 11/04/15
 */
public class RequestRestoreSnapshotParameters  extends RequestRestoreBridgeParameters {


    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<RequestRestoreSnapshotParameters> serializer =
            new JaxbJsonSerializer<>(RequestRestoreSnapshotParameters.class);
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
    public static RequestRestoreSnapshotParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<RequestRestoreSnapshotParameters> serializer =
            new JaxbJsonSerializer<>(RequestRestoreSnapshotParameters.class);
        try {
            RequestRestoreSnapshotParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSnapshotId() || params.getSnapshotId().isEmpty()) {
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
