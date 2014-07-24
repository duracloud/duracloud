/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot.dto;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Bill Branan
 *         Date: 1/30/14
 */
public class CreateSnapshotTaskParameters {

    @XmlValue
    private String spaceId;

    @XmlValue
    private String description;

    @XmlValue
    private String userEmail;

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

}
