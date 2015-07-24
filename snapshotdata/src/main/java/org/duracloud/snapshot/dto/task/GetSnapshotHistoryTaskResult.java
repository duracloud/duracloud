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
import org.duracloud.snapshot.dto.bridge.GetSnapshotHistoryBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Gad Krumholz
 *         Date: 6/24/15
 */
public class GetSnapshotHistoryTaskResult extends GetSnapshotHistoryBridgeResult {

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static GetSnapshotHistoryTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetSnapshotHistoryTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotHistoryTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
