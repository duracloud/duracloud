/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import java.io.IOException;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Bill Branan
 *         Date: 8/14/14
 */
public class CleanupSnapshotTaskResult extends BaseDTO {

    /**
     * The number of days before content expires
     */
    @XmlValue
    private int contentExpirationDays;

    // Required by JAXB
    public CleanupSnapshotTaskResult() {}

    public CleanupSnapshotTaskResult(int contentExpirationDays) {
        this.contentExpirationDays = contentExpirationDays;
    }

    public int getContentExpirationDays() {
        return contentExpirationDays;
    }

    public void setContentExpirationDays(int contentExpirationDays) {
        this.contentExpirationDays = contentExpirationDays;
    }

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<CleanupSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CleanupSnapshotTaskResult.class);
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
    public static CleanupSnapshotTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<CleanupSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CleanupSnapshotTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
