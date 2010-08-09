/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.domain;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class ContentLocation {
    private String contentId;
    private String spaceId;

    public ContentLocation(String spaceId, String contentId) {
        this.spaceId = spaceId;
        this.contentId = contentId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getContentId() {
        return contentId;
    }
}
