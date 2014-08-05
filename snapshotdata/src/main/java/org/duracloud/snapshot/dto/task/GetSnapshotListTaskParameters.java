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
import org.duracloud.snapshot.error.SnapshotDataException;
import org.springframework.util.StringUtils;

/**
 * @author Daniel Bernstein
 *         Date: 8/4/14
 */
public class GetSnapshotListTaskParameters {

    /**
     * The ID of the space in which the content to snapshot resides
     */
    @XmlValue
    private String host;

    // Required by JAXB
    public GetSnapshotListTaskParameters() {
    }

    public GetSnapshotListTaskParameters(String host) {
        if (StringUtils.isEmpty(host))
            throw new IllegalArgumentException("host must be non-null");
        this.host = host;
    }

    /**
     * Parses properties from task parameter string
     *
     * @param taskParameters - JSON formatted set of parameters
     */
    public static GetSnapshotListTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<GetSnapshotListTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshotListTaskParameters.class);
        try {
            GetSnapshotListTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getHost() || params.getHost().isEmpty()) {
                throw new SnapshotDataException(
                    "Task parameter values may not be empty");
            }
            return params;
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }


    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }

}
