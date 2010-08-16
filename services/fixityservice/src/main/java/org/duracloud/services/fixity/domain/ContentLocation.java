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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentLocation)) {
            return false;
        }

        ContentLocation that = (ContentLocation) o;

        if (contentId != null ? !contentId.equals(that.contentId) :
            that.contentId != null) {
            return false;
        }
        if (spaceId != null ? !spaceId.equals(that.spaceId) :
            that.spaceId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = contentId != null ? contentId.hashCode() : 0;
        result = 31 * result + (spaceId != null ? spaceId.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ContentLocation [");
        sb.append(spaceId);
        sb.append(",");
        sb.append(contentId);
        sb.append("]");
        return sb.toString();
    }
}
