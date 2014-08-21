/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.bridge.GetRestoreBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

import java.io.IOException;

/**
 * @author Daniel Bernstein
 *         Date: 8/7/14
 */
public class GetRestoreTaskResult extends GetRestoreBridgeResult {

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetRestoreTaskResult> serializer =
            new JaxbJsonSerializer<>(GetRestoreTaskResult.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static GetRestoreTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetRestoreTaskResult> serializer =
            new JaxbJsonSerializer<>(GetRestoreTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }
    
}
