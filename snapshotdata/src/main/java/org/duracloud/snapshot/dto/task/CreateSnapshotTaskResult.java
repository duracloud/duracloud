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
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * Result of calling create snapshot task.
 *
 * Note: The task result currently mirrors the bridge result
 *
 * @author Bill Branan
 *         Date: 1/30/14
 */
public class CreateSnapshotTaskResult extends CreateSnapshotBridgeResult {

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static CreateSnapshotTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<CreateSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
