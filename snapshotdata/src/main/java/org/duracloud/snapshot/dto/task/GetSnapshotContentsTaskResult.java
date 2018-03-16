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
import org.duracloud.snapshot.dto.bridge.GetSnapshotContentBridgeResult;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Bill Branan
 * Date: 8/11/14
 */
public class GetSnapshotContentsTaskResult extends GetSnapshotContentBridgeResult {

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static GetSnapshotContentsTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetSnapshotContentsTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotContentsTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch (IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
