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
 *         Date: 8/11/14
 */
public class GetSnapshotContentsTaskParameters extends BaseDTO {

    /**
     * The ID of the snapshot to consider
     */
    @XmlValue
    private String snapshotId;

    /**
     * The number of the page of results that is being requested
     */
    @XmlValue
    private int pageNumber;

    /**
     * The number of content items to include in the result set
     */
    @XmlValue
    private int pageSize;

    /**
     * An optional value used to select only those items which begin with the
     * given prefix
     */
    @XmlValue
    private String prefix;

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Creates a serialized version of task parameters
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotContentsTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshotContentsTaskParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create task parameters due to: " + e.getMessage());
        }
    }

    /**
     * Parses properties from task parameter string
     *
     * @param taskParameters - JSON formatted set of parameters
     */
    public static GetSnapshotContentsTaskParameters deserialize(String taskParameters) {
        JaxbJsonSerializer<GetSnapshotContentsTaskParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshotContentsTaskParameters.class);
        try {
            GetSnapshotContentsTaskParameters params =
                serializer.deserialize(taskParameters);
            // Verify required parameters
            if(null == params.getSnapshotId() || params.getSnapshotId().isEmpty()) {
                throw new SnapshotDataException(
                    "Value for snapshot ID may not be empty");
            }
            return params;
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to parse task parameters due to: " + e.getMessage());
        }
    }
    
}
