/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import java.io.IOException;
import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.error.TaskDataException;

/**
 * @author Bill Branan
 * Date: 3/5/15
 */
public class DisableStreamingTaskResult {

    /**
     * The number of days before content expires
     */
    @XmlValue
    private String result;

    // Required by JAXB
    public DisableStreamingTaskResult() {
    }

    public DisableStreamingTaskResult(String result, String streamingHost) {
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
        JaxbJsonSerializer<DisableStreamingTaskResult> serializer =
            new JaxbJsonSerializer<>(DisableStreamingTaskResult.class);
        try {
            return serializer.serialize(this);
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

    /**
     * Parses properties from task result
     *
     * @param taskResult - JSON formatted set of properties
     */
    public static DisableStreamingTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<DisableStreamingTaskResult> serializer =
            new JaxbJsonSerializer<>(DisableStreamingTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
