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
 *         Date: 7/25/14
 */
public class CompleteSnapshotTaskResult extends BaseDTO {

    /**
     * The number of days before content expires
     */
    @XmlValue
    private String result;

    // Required by JAXB
    public CompleteSnapshotTaskResult() {}

    public CompleteSnapshotTaskResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<CompleteSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CompleteSnapshotTaskResult.class);
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
    public static CompleteSnapshotTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<CompleteSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CompleteSnapshotTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }
    
}
