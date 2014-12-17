/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.dto.bridge.GetSnapshotListBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

import java.io.IOException;
import java.util.List;

/**
 * @author Daniel Bernstein
 *         Date: 8/4/14
 */
public class GetSnapshotListTaskResult extends GetSnapshotListBridgeResult {

    public GetSnapshotListTaskResult(){}

    public GetSnapshotListTaskResult(List<SnapshotSummary> snapshots){
        super(snapshots);
    }
    
    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static GetSnapshotListTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetSnapshotListTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotListTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }
    
}
