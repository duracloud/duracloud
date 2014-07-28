/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot.dto;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.storage.error.TaskException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 7/25/14
 */
public class CompleteSnapshotTaskResult {

    /**
     * The number of days before content expires
     */
    @XmlValue
    private int contentExpirationDays;

    public CompleteSnapshotTaskResult(int contentExpirationDays) {
        this.contentExpirationDays = contentExpirationDays;
    }

    public int getContentExpirationDays() {
        return contentExpirationDays;
    }

    public void setContentExpirationDays(int contentExpirationDays) {
        this.contentExpirationDays = contentExpirationDays;
    }

    /**
     * Creates a serialized version of task results
     *
     * @return JSON formatted task result info
     */
    public String serialize() {
        JaxbJsonSerializer<CompleteSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CompleteSnapshotTaskResult.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new TaskException("Unable to create task result due to: " +
                                    e.getMessage());
        }
    }

}
