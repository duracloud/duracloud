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
public class GetSignedCookiesUrlTaskResult {

    /**
     * The URL at which the signed cookies can be set
     */
    @XmlValue
    private String signedCookiesUrl;

    // Required by JAXB
    public GetSignedCookiesUrlTaskResult() {
    }

    public String getSignedCookiesUrl() {
        return signedCookiesUrl;
    }

    public void setSignedCookiesUrl(String signedCookiesUrl) {
        this.signedCookiesUrl = signedCookiesUrl;
    }

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSignedCookiesUrlTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSignedCookiesUrlTaskResult.class);
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
    public static GetSignedCookiesUrlTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetSignedCookiesUrlTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSignedCookiesUrlTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
