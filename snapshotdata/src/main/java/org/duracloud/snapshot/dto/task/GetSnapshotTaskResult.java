/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.bridge.GetSnapshotBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 7/29/14
 */
public class GetSnapshotTaskResult extends GetSnapshotBridgeResult {

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static GetSnapshotTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
