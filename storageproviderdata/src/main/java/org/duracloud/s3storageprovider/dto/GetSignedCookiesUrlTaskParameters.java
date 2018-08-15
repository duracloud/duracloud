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
public class GetSignedCookiesUrlTaskParameters extends GetUrlTaskParameters {

    /**
     * Can be used as the value of the minutesToExpire parameter
     * to indicate that the default value should be used
     */
    public static final int USE_DEFAULT_MINUTES_TO_EXPIRE = -1;

    /**
     * The ID of the space in which the content item to be streamed resides
     */
    @XmlValue
    private String spaceId;

    /**
     * Ending date and time (in Unix epoch format to the millisecond)
     * for when URL expires
     */
    @XmlValue
    private int minutesToExpire;

    /**
     * IP address or range where requests to stream must originate
     */
    @XmlValue
    private String ipAddress;

    /**
     * URL to which the user should be redirected after cookies are set
     */
    @XmlValue
    private String redirectUrl;

    // Required by JAXB
    public GetSignedCookiesUrlTaskParameters() {
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public int getMinutesToExpire() {
        return minutesToExpire;
    }

    public void setMinutesToExpire(int minutesToExpire) {
        this.minutesToExpire = minutesToExpire;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSignedCookiesUrlTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetSignedCookiesUrlTaskParameters.class);
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
    public static GetSignedCookiesUrlTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<GetSignedCookiesUrlTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetSignedCookiesUrlTaskParameters.class);
        try {
            GetSignedCookiesUrlTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if (null == params.getSpaceId() || params.getSpaceId().isEmpty()) {
                throw new TaskDataException(
                    "Task parameter 'spaceId' may not be empty");
            }

            return params;
        } catch (IOException e) {
            throw new TaskDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }

}
