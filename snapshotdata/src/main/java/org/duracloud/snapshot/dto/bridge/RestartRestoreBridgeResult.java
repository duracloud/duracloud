/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.io.IOException;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Daniel Bernstein 
 *         Date: 11/06/15
 */
public class RestartRestoreBridgeResult extends GenericRestoreBridgeResult {

    public RestartRestoreBridgeResult() {
        super();
    }

    public RestartRestoreBridgeResult(String description, RestoreStatus status) {
        super(description,status);
    }

    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static RestartRestoreBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<RestartRestoreBridgeResult> serializer =
            new JaxbJsonSerializer<>(RestartRestoreBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
