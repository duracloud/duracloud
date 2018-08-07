/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import java.io.IOException;
import java.util.Map;
import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.error.TaskDataException;

/**
 * @author Bill Branan
 * Date: Aug 6, 2018
 */
public class GetSignedCookieTaskResult {

    /**
     * The policy statement which controls the access that a signed cookie grants to a user
     */
    @XmlValue
    private Map<String, String> signedCookies;

    @XmlValue
    private String streamingHost;

    // Required by JAXB
    public GetSignedCookieTaskResult() {
    }

    public Map<String, String> getSignedCookies() {
        return signedCookies;
    }

    public void setSignedCookies(Map<String, String> signedCookies) {
        this.signedCookies = signedCookies;
    }

    public String getStreamingHost() {
        return streamingHost;
    }

    public void setStreamingHost(String streamingHost) {
        this.streamingHost = streamingHost;
    }

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSignedCookieTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSignedCookieTaskResult.class);
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
    public static GetSignedCookieTaskResult deserialize(String taskResult) {
        JaxbJsonSerializer<GetSignedCookieTaskResult> serializer =
            new JaxbJsonSerializer<>(GetSignedCookieTaskResult.class);
        try {
            return serializer.deserialize(taskResult);
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to create task result due to: " + e.getMessage());
        }
    }

}
