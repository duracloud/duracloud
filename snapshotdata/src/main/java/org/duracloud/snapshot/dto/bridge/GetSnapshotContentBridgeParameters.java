/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.BaseDTO;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Daniel Bernstein
 *         Date: 7/31/14
 */
public class GetSnapshotContentBridgeParameters extends BaseDTO {

    /**
     * The host name of the DuraCloud instance
     */
    @XmlValue
    private int page;
    
    @XmlValue
    private int pageSize;
    
    @XmlValue
    private String prefix;

    public GetSnapshotContentBridgeParameters(){}

    
    public GetSnapshotContentBridgeParameters(
        int page, int pageSize, String prefix) {
        super();
        this.page = page;
        this.pageSize = pageSize;
        this.prefix = prefix;
    }


    /**
     * Creates a serialized version of bridge parameters
     *
     * @return JSON formatted bridge info
     */
    public String serialize() {
        JaxbJsonSerializer<GetSnapshotContentBridgeParameters> serializer =
            new JaxbJsonSerializer<>(GetSnapshotContentBridgeParameters.class);
        try {
            return serializer.serialize(this);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to create  result due to: " + e.getMessage());
        }
    }


    public int getPage() {
        return page;
    }


    public void setPage(int page) {
        this.page = page;
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
    
}
