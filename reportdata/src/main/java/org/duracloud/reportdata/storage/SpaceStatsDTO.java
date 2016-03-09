package org.duracloud.reportdata.storage;

/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;


/**
 * Represents a space's byte and object count at a moment in time.
 * @author Daniel Bernstein
 *
 */
public class SpaceStatsDTO {
    private Date timestamp;
    private String accountId;
    private String spaceId;
    private String storeId;
    private long byteCount = 0;
    private long objectCount = 0;
    
    public SpaceStatsDTO (){}
    
    public SpaceStatsDTO(Date timestamp, String account, String storeId, String spaceId, long byteCount, long objectCount) {
        setTimestamp(timestamp);
        setAccountId(account);
        setStoreId(storeId);
        setSpaceId(spaceId);
        setByteCount(byteCount);
        setObjectCount(objectCount);
    }
    
    public String getSpaceId() {
        return spaceId;
    }
    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
    public String getStoreId() {
        return storeId;
    }
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public long getByteCount() {
        return byteCount;
    }
    public void setByteCount(long byteCount) {
        this.byteCount = byteCount;
    }
    public long getObjectCount() {
        return objectCount;
    }
    public void setObjectCount(long objectCount) {
        this.objectCount = objectCount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
