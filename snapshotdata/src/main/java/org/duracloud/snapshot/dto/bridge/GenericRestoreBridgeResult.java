/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import java.io.IOException;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.error.SnapshotDataException;

/**
 * @author Daniel Bernstein 
 *         Date: 11/06/15
 */
public class GenericRestoreBridgeResult extends BaseDTO {

    /**
     * Describes in human parseable terms what happened.
     */
    @XmlValue
    private String description;

    /**
     * The current status of the restore
     */
    @XmlValue
    private RestoreStatus status;

    // Required by JAXB
    public GenericRestoreBridgeResult() {
    }

    public GenericRestoreBridgeResult(String description, RestoreStatus status) {
        this.description = description;
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RestoreStatus getStatus() {
        return status;
    }

    public void setStatus(RestoreStatus status) {
        this.status = status;
    }

}
