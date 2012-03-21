/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

/**
 * This bean holds to common elements for all ContentStore AOP messages.
 *
 * @author Andrew Woods
 *         Date: 3/15/12
 */
public class ContentMessage {

    private String storeId;
    private String spaceId;
    private String contentId;
    private String username;
    private String action;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ContentMessage[");
        sb.append("storeId:'" + storeId + "'");
        sb.append("|spaceId:'" + spaceId + "'");
        sb.append("|contentId:'" + contentId + "'");
        sb.append("|username:'" + username + "'");
        sb.append("|action:'" + action + "'");
        sb.append("]\n");
        return sb.toString();
    }

    public static enum ACTION {
        INGEST, COPY, UPDATE, DELETE;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentMessage)) {
            return false;
        }

        ContentMessage that = (ContentMessage) o;

        if (action != null ? !action.equals(that.action) :
            that.action != null) {
            return false;
        }
        if (contentId != null ? !contentId.equals(that.contentId) :
            that.contentId != null) {
            return false;
        }
        if (spaceId != null ? !spaceId.equals(that.spaceId) :
            that.spaceId != null) {
            return false;
        }
        if (storeId != null ? !storeId.equals(that.storeId) :
            that.storeId != null) {
            return false;
        }
        if (username != null ? !username.equals(that.username) :
            that.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = storeId != null ? storeId.hashCode() : 0;
        result = 31 * result + (spaceId != null ? spaceId.hashCode() : 0);
        result = 31 * result + (contentId != null ? contentId.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
