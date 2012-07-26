/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.result;

import org.duracloud.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static org.duracloud.services.ComputeService.DELIM;

/**
 * This class holds details of a duplication result.
 * <p/>
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public class DuplicationEvent implements Delayed {

    private final Logger log = LoggerFactory.getLogger(DuplicationEvent.class);

    private static final String header =
        "from-store-id" + DELIM + "to-store-id" + DELIM + "space-id" + DELIM +
        "content-id" + DELIM + "md5" + DELIM + "event" + DELIM +
        "success" + DELIM + "date-time" + DELIM + "message";

    private String fromStoreId;
    private String toStoreId;
    private String spaceId;
    private String contentId;
    private String md5;
    private String error;
    private TYPE type;
    private final long eventTime;

    private boolean success;

    private long delayDeadline;

    public static enum TYPE {
        SPACE_CREATE,
        SPACE_UPDATE,
        SPACE_UPDATE_ACL,
        SPACE_DELETE,
        CONTENT_CREATE,
        CONTENT_UPDATE,
        CONTENT_DELETE;
    }

    public DuplicationEvent(String fromStoreId,
                            String toStoreId,
                            TYPE type,
                            String spaceId) {
        this(fromStoreId, toStoreId, type, spaceId, null);
    }

    public DuplicationEvent(String fromStoreId,
                            String toStoreId,
                            TYPE type,
                            String spaceId,
                            String contentId) {
        this(fromStoreId, toStoreId, type, spaceId, contentId, null);
    }

    public DuplicationEvent(String fromStoreId,
                            String toStoreId,
                            TYPE type,
                            String spaceId,
                            String contentId,
                            String md5) {
        this.fromStoreId = fromStoreId;
        this.toStoreId = toStoreId;
        this.type = type;
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.md5 = md5;

        this.eventTime = System.currentTimeMillis();
        this.success = true;
    }

    public void setDelay(long delayMillis) {
        log.debug("setDelay({})", delayMillis);
        delayDeadline = System.currentTimeMillis() + delayMillis;
    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
        long delay = delayDeadline - System.currentTimeMillis();
        return timeUnit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed that) {
        long thisDelay = this.getDelay(TimeUnit.NANOSECONDS);
        long thatDelay = that.getDelay(TimeUnit.NANOSECONDS);

        return thisDelay == thatDelay ? 0 : (thisDelay > thatDelay ? 1 : -1);
    }

    public String getFromStoreId() {
        return fromStoreId;
    }

    public String getToStoreId() {
        return toStoreId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getContentId() {
        return contentId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void fail(String error) {
        this.success = false;
        this.error = error;
    }

    public String getHeader() {
        return header;
    }

    public String getEntry() {
        StringBuilder entry = new StringBuilder();
        entry.append(fromStoreId);
        entry.append(DELIM);
        entry.append(toStoreId);
        entry.append(DELIM);
        entry.append(spaceId);
        entry.append(DELIM);
        entry.append(null == contentId ? "-" : contentId);
        entry.append(DELIM);
        entry.append(null == md5 ? "-" : md5);
        entry.append(DELIM);
        entry.append(getType());
        entry.append(DELIM);
        entry.append(isSuccess());
        entry.append(DELIM);
        entry.append(DateUtil.convertToStringMid(eventTime));
        entry.append(DELIM);
        entry.append(null == error ? "-" : error);

        return entry.toString();
    }

    public TYPE getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DuplicationEvent:[");
        sb.append(getEntry());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DuplicationEvent that = (DuplicationEvent) o;

        if (contentId != null ? !contentId.equals(that.contentId) :
            that.contentId != null) {
            return false;
        }
        if (fromStoreId != null ? !fromStoreId.equals(that.fromStoreId) :
            that.fromStoreId != null) {
            return false;
        }
        if (spaceId != null ? !spaceId.equals(that.spaceId) :
            that.spaceId != null) {
            return false;
        }
        if (toStoreId != null ? !toStoreId.equals(that.toStoreId) :
            that.toStoreId != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromStoreId != null ? fromStoreId.hashCode() : 0;
        result = 31 * result + (toStoreId != null ? toStoreId.hashCode() : 0);
        result = 31 * result + (spaceId != null ? spaceId.hashCode() : 0);
        result = 31 * result + (contentId != null ? contentId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

}
