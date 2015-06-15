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
 *         Date: 3/5/15
 */
public class EnableStreamingTaskParameters {

    /**
     * The ID of the space for which streaming is to be enabled
     */
    @XmlValue
    private String spaceId;

    @XmlValue
    private boolean secure;

    // Required by JAXB
    public EnableStreamingTaskParameters() {
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<EnableStreamingTaskParameters> serializer =
            new JaxbJsonSerializer<>(EnableStreamingTaskParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new TaskDataException(
                "Unable to create task parameters due to: " + e.getMessage());
        }
    }

    /**
     * Parses properties from task parameter string
     *
     * @param taskParameters - JSON formatted set of parameters
     */
    public static EnableStreamingTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<EnableStreamingTaskParameters> serializer =
            new JaxbJsonSerializer<>(EnableStreamingTaskParameters.class);
        try {
            EnableStreamingTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSpaceId() || params.getSpaceId().isEmpty()) {
                throw new TaskDataException(
                    "Task parameter values may not be empty");
            }
            return params;
        } catch(IOException e) {
            throw new TaskDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }

}
