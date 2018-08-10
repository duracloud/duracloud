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
public class StoreSignedCookieTaskParameters {

    /**
     * The policy statement which controls the access that a signed cookie grants to a user
     */
    @XmlValue
    private Map<String, String> signedCookies;

    @XmlValue
    private String streamingHost;

    @XmlValue
    private String redirectUrl;

    // Required by JAXB
    public StoreSignedCookieTaskParameters() {
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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task parameter info
     */
    public String serialize() {
        JaxbJsonSerializer<StoreSignedCookieTaskParameters> serializer =
            new JaxbJsonSerializer<>(StoreSignedCookieTaskParameters.class);
        try {
            return serializer.serialize(this);
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to create task parameters due to: " + e.getMessage());
        }
    }

    /**
     * Parses properties from task result
     *
     * @param taskParameters - JSON formatted set of properties
     */
    public static StoreSignedCookieTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<StoreSignedCookieTaskParameters> serializer =
            new JaxbJsonSerializer<>(StoreSignedCookieTaskParameters.class);
        try {
            return serializer.deserialize(taskParameters);
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to create task parameters due to: " + e.getMessage());
        }
    }

}
