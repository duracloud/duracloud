/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chrontask.snapshot;

import javax.xml.bind.annotation.XmlValue;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bill Branan
 *         Date: 1/30/14
 */
public class SnapshotTaskParameters {

    @XmlValue
    private String spaceId;

    @XmlValue
    private Map<String, String> snapshotProperties;

    public SnapshotTaskParameters() {
        snapshotProperties = new HashMap<>();
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public Map<String, String> getSnapshotProperties() {
        return snapshotProperties;
    }

    public void setSnapshotProperties(Map<String, String> snapshotProperties) {
        this.snapshotProperties = snapshotProperties;
    }

}
