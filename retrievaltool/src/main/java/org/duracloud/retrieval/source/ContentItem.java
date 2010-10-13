/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

/**
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class ContentItem {

    private String spaceId;
    private String contentId;

    public ContentItem(String spaceId, String contentId) {
        this.spaceId = spaceId;
        this.contentId = contentId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getContentId() {
        return contentId;
    }

    @Override
    public String toString() {
        return "DuraCloud file '" + spaceId + "/" + contentId + "'";
    }
}
