/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

public class ContentCopyMessage extends ContentMessage {

    private String srcSpaceId;
    private String srcContentId;
    private String contentMd5;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ContentCopyMessage[");
        sb.append("storeId:'" + getStoreId() + "'");
        sb.append("|sourceSpaceId:'" + srcSpaceId + "'");
        sb.append("|sourceContentId:'" + srcContentId + "'");
        sb.append("|destSpaceId:'" + getSpaceId() + "'");
        sb.append("|destContentId:'" + getContentId() + "'");
        sb.append("|username:'" + getUsername() + "'");
        sb.append("|contentMd5:'" + contentMd5 + "'");
        sb.append("|action:'" + getAction() + "'");
        sb.append("|datetime:'" + getDatetime() + "'");
        sb.append("]\n");
        return sb.toString();
    }

    @Override
    public String asTSV() {
        StringBuilder sb = new StringBuilder(super.asTSV());
        sb.append(DELIM);
        sb.append(contentMd5);
        sb.append(DELIM);
        sb.append("tbd");
        sb.append(DELIM);
        sb.append("tbd");
        sb.append(DELIM);
        sb.append(srcSpaceId);
        sb.append(DELIM);
        sb.append(srcContentId);
        return sb.toString();
    }

    public String getSrcSpaceId() {
        return srcSpaceId;
    }

    public void setSrcSpaceId(String srcSpaceId) {
        this.srcSpaceId = srcSpaceId;
    }

    public String getSrcContentId() {
        return srcContentId;
    }

    public void setSrcContentId(String srcContentId) {
        this.srcContentId = srcContentId;
    }

    public String getContentMd5() {
        return contentMd5;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentCopyMessage)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ContentCopyMessage that = (ContentCopyMessage) o;

        if (contentMd5 != null ? !contentMd5.equals(that.contentMd5) :
            that.contentMd5 != null) {
            return false;
        }
        if (srcContentId != null ? !srcContentId.equals(that.srcContentId) :
            that.srcContentId != null) {
            return false;
        }
        if (srcSpaceId != null ? !srcSpaceId.equals(that.srcSpaceId) :
            that.srcSpaceId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (srcSpaceId != null ? srcSpaceId.hashCode() : 0);
        result =
            31 * result + (srcContentId != null ? srcContentId.hashCode() : 0);
        result = 31 * result + (contentMd5 != null ? contentMd5.hashCode() : 0);
        return result;
    }
}
