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
import org.duracloud.snapshot.dto.bridge.RequestRestoreBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * Result of calling restart snapshot task.
 *
 * Note: The task result currently mirrors the bridge result
 *
 * @author Daniel Bernstein
 *         Date: 11/04/2015
 */
public class RequestRestoreSnapshotTaskResult extends RequestRestoreBridgeResult {

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static RequestRestoreSnapshotTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<RequestRestoreSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(RequestRestoreSnapshotTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
