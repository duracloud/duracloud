/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.error.TaskDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 09/25/2015
 */
public class SetStoragePolicyTaskParameters {

    @XmlValue
    private String spaceId;

    @XmlValue
    private String storageClass;

    @XmlValue
    private Integer daysToTransition;

    // Required by JAXB
    public SetStoragePolicyTaskParameters() {
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public Integer getDaysToTransition() {
        return daysToTransition;
    }

    public void setDaysToTransition(Integer daysToTransition) {
        this.daysToTransition = daysToTransition;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<SetStoragePolicyTaskParameters> serializer =
            new JaxbJsonSerializer<>(SetStoragePolicyTaskParameters.class);
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
    public static SetStoragePolicyTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<SetStoragePolicyTaskParameters> serializer =
            new JaxbJsonSerializer<>(SetStoragePolicyTaskParameters.class);
        try {
            SetStoragePolicyTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify expected parameters
            if(null == params.getSpaceId() || params.getSpaceId().isEmpty() ||
               null == params.getStorageClass() || params.getStorageClass().isEmpty() ||
               null == params.daysToTransition || params.daysToTransition < 0) {
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
