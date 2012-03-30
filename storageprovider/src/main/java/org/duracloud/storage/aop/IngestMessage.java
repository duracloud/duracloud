/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.duracloud.storage.error.InvalidEventTSVException;

/**
 * This class is the bean that is published-to/consumed-from the ingest topic.
 *
 * @author Andrew Woods
 */
public class IngestMessage extends ContentMessage {

    private String contentMimeType;
    private long contentSize;

    public IngestMessage() {
        // default constructor
    }

    public IngestMessage(String tsv) throws InvalidEventTSVException {
        super(tsv);

        String[] parts = tsv.split(Character.toString(DELIM));
        if (parts.length < 9) {
            throw new InvalidEventTSVException(tsv);
        }

        setContentSize(Long.parseLong(parts[7]));
        setContentMimeType(parts[8]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("IngestMessage[");
        sb.append("storeId:'" + getStoreId() + "'");
        sb.append("|spaceId:'" + getSpaceId() + "'");
        sb.append("|contentId:'" + getContentId() + "'");
        sb.append("|mime:'" + contentMimeType + "'");
        sb.append("|username:'" + getUsername() + "'");
        sb.append("|contentMd5:'" + getContentMd5() + "'");
        sb.append("|contentSize:'" + contentSize + "'");
        sb.append("|action:'" + getAction() + "'");
        sb.append("|datetime:'" + getDatetime() + "'");
        sb.append("]\n");
        return sb.toString();
    }

    @Override
    public String asTSV() {
        StringBuilder sb = new StringBuilder(super.asTSV());
        sb.append(DELIM);
        sb.append(contentSize);
        sb.append(DELIM);
        sb.append(contentMimeType);
        return sb.toString();
    }

    public String getContentMimeType() {
        return contentMimeType;
    }

    public void setContentMimeType(String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }
}
