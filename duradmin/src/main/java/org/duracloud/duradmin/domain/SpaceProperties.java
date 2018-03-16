/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

import java.io.Serializable;
import java.util.Set;

/**
 * Stores space properties.
 *
 * @author Bill Branan
 */
public class SpaceProperties
    implements Serializable {

    private String created;

    private String count;

    private String size;

    private int queryCount;

    private String streamingHost;

    private String streamingType;

    private Set<String> tags;

    private String restoreId;

    private String snapshotId;

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;

    }

    public int getQueryCount() {
        return queryCount;
    }

    public String getStreamingHost() {
        return streamingHost;
    }

    public void setStreamingHost(String streamingHost) {
        this.streamingHost = streamingHost;
    }

    public String getStreamingType() {
        return streamingType;
    }

    public void setStreamingType(String streamingType) {
        this.streamingType = streamingType;
    }

    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String restoreId) {
        this.restoreId = restoreId;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

}
