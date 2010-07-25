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
 * Stores space metadata.
 * 
 * @author Bill Branan
 */
public class SpaceMetadata
        implements Serializable {

    private String access;

    private String created;

    private String count;

    private int queryCount;

    private Set<String> tags;

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

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

}
