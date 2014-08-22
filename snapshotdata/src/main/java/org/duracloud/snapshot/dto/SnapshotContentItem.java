/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import java.util.Map;

/**
 * Contains information about a single content item which was included in a
 * snapshot.
 *
 * @author Bill Branan
 *         Date: 8/11/14
 */
public class SnapshotContentItem extends BaseDTO {

    private String contentId;
    private Map<String, String> contentProperties;

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Map<String, String> getContentProperties() {
        return contentProperties;
    }

    public void setContentProperties(Map<String, String> contentProperties) {
        this.contentProperties = contentProperties;
    }

}
