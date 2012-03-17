/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

/**
 * This class is the bean that is published-to/consumed-from the ingest topic.
 *
 * @author Andrew Woods
 */
public class IngestMessage extends ContentStoreMessage {

    private String contentId;
    private String contentMimeType;
    private String contentMd5;
    private long contentSize;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("IngestMessage[");
        sb.append("storeId:'" + getStoreId() + "'");
        sb.append("|spaceId:'" + getSpaceId() + "'");
        sb.append("|contentId:'" + contentId + "'");
        sb.append("|mime:'" + contentMimeType + "'");
        sb.append("|username:'" + getUsername() + "'");
        sb.append("|contentMd5:'" + contentMd5 + "'");
        sb.append("|contentSize:'" + contentSize + "'");
        sb.append("]\n");
        return sb.toString();
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getContentMimeType() {
        return contentMimeType;
    }

    public void setContentMimeType(String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }

    public String getContentMd5() {
        return contentMd5;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }
}
