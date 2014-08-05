/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class GetSnapshotContentBridgeResult {

    /**
     * The details of the current status
     */
    @XmlValue
    private List<String> contentIds;

    public GetSnapshotContentBridgeResult(){}

    public GetSnapshotContentBridgeResult(List<String> contentIds) {
        this.setContentIds(contentIds);
    }

    public List<String> getContentIds() {
        return contentIds;
    }

    public void setContentIds(List<String> contentIds) {
        this.contentIds = contentIds;
    }

    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static GetSnapshotContentBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetSnapshotContentBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetSnapshotContentBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }
}
