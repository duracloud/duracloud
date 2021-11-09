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
import org.duracloud.snapshot.dto.bridge.GetSnapshotTotalsBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Nicholas Woodward
 * Date: 7/26/21
 */
public class GetSnapshotsTotalsTaskResult extends GetSnapshotTotalsBridgeResult {

    public GetSnapshotsTotalsTaskResult() {
    }

    public GetSnapshotsTotalsTaskResult(Long totalCount, Long totalSize, Long totalFiles) {
        super(totalCount, totalSize, totalFiles);
    }

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static GetSnapshotsTotalsTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetSnapshotsTotalsTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotsTotalsTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch (IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
