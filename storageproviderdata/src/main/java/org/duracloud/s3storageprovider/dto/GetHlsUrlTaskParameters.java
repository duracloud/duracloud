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
 * Date: Aug 6, 2018
 */
public class GetHlsUrlTaskParameters {

    /**
     * The ID of the space in which the content item to be streamed resides
     */
    @XmlValue
    private String spaceId;

    /**
     * The ID of the content item which is to be streamed
     */
    @XmlValue
    private String contentId;

    // Required by JAXB
    public GetHlsUrlTaskParameters() {
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetHlsUrlTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetHlsUrlTaskParameters.class);
        try {
            return serializer.serialize(this);
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to create task parameters due to: " + e.getMessage());
        }
    }

    /**
     * Parses properties from task parameter string
     *
     * @param taskParameters - JSON formatted set of parameters
     */
    public static GetHlsUrlTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<GetHlsUrlTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetHlsUrlTaskParameters.class);
        try {
            GetHlsUrlTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if (null == params.getSpaceId() || params.getSpaceId().isEmpty()) {
                throw new TaskDataException(
                    "Task parameter 'spaceId' may not be empty");
            } else if (null == params.getContentId() || params.getContentId().isEmpty()) {
                throw new TaskDataException(
                    "Task parameter 'contentId' may not be empty");
            }

            return params;
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }

}
