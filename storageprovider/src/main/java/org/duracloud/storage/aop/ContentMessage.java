/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import java.text.ParseException;
import java.util.Date;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.bulk.ManifestVerifier;
import org.duracloud.storage.error.InvalidEventTSVException;

/**
 * This bean holds to common elements for all ContentStore AOP messages.
 *
 * @author Andrew Woods
 *         Date: 3/15/12
 */
public class ContentMessage {

    private String account;
    private String storeId;
    private String spaceId;
    private String contentId;
    private String contentMd5; // sometimes null
    private String username;
    private String action;
    private String datetime = DateUtil.nowVerbose();

    protected static final char DELIM = ManifestVerifier.DELIM;

    public ContentMessage() {
        // default constructor
    }

    public ContentMessage(String tsv) throws InvalidEventTSVException {
        String[] parts = tsv.split(Character.toString(DELIM));
        if (parts.length < 6) {
            throw new InvalidEventTSVException(tsv);
        }

        setStoreId(parts[0]);
        setSpaceId(parts[1]);
        setContentId(parts[2]);
        setUsername(parts[3]);
        setAction(parts[4]);
        setDatetime(parts[5]);

        if (parts.length > 6) {
            setContentMd5(parts[6]);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ContentMessage[");
        sb.append("storeId:'" + storeId + "'");
        sb.append("|spaceId:'" + spaceId + "'");
        sb.append("|contentId:'" + contentId + "'");
        sb.append("|username:'" + username + "'");
        sb.append("|action:'" + action + "'");
        sb.append("|datetime:'" + datetime + "'");
        sb.append("|account:'" + account + "'");
        sb.append("]\n");
        return sb.toString();
    }

    public String asTSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(storeId);
        sb.append(DELIM);
        sb.append(spaceId);
        sb.append(DELIM);
        sb.append(contentId);
        sb.append(DELIM);
        sb.append(username);
        sb.append(DELIM);
        sb.append(action);
        sb.append(DELIM);
        sb.append(datetime);

        if (null != contentMd5) {
            sb.append(DELIM);
            sb.append(contentMd5);
        }
        return sb.toString();
    }

    public static String tsvHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("store id");
        sb.append(DELIM);
        sb.append("space id");
        sb.append(DELIM);
        sb.append("content id");
        sb.append(DELIM);
        sb.append("user id");
        sb.append(DELIM);
        sb.append("action");
        sb.append(DELIM);
        sb.append("datetime");
        // below come from ContentCopyMessage and IngestMessage
        sb.append(DELIM);
        sb.append("content MD5");
        sb.append(DELIM);
        sb.append("content size");
        sb.append(DELIM);
        sb.append("mimetype");
        sb.append(DELIM);
        sb.append("src space id");
        sb.append(DELIM);
        sb.append("src content id");

        return sb.toString();
    }

    public static enum ACTION {
        INGEST, COPY, UPDATE, DELETE, ERROR;
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

    public String getContentMd5() {
        return contentMd5;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
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

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
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
        if (datetime != null ? !datetime.equals(that.datetime) :
            that.datetime != null) {
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
        if (account != null ? !account.equals(that.account) :
            that.account != null) {
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
        result = 31 * result + (datetime != null ? datetime.hashCode() : 0);
        result = 31 * result + (account != null ? account.hashCode() : 0);

        return result;
    }

    public Date getDate() {
        try {
            return DateUtil.convertToDate(this.datetime, DateUtil.DateFormat.VERBOSE_FORMAT);
        } catch (ParseException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    public String getAccount() {
        return this.account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
}
