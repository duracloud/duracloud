/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Space - the container in which content is stored.
 *
 * @author Bill Branan
 */
public class Space {
    private String id;
    private Map<String, String> metadata = new HashMap<String, String>();
    private List<String> contentIds = new ArrayList<String>();

    /**
     * <p>Getter for the field <code>id</code>.</p>
     */
    public String getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * <p>Getter for the field <code>metadata</code>.</p>
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * <p>Setter for the field <code>metadata</code>.</p>
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * <p>Adds an item to the space metadata map</p>
     *
     * @param name metadata key
     * @param value metadata value
     */
    public void addMetadata(String name, String value) {
        metadata.put(name, value);
    }

    /**
     * <p>Getter for the field <code>contentIds</code>.</p>
     */
    public List<String> getContentIds() {
        return contentIds;
    }

    /**
     * <p>Setter for the field <code>contentIds</code>.</p>
     */
    public void setContentIds(List<String> contentIds) {
        this.contentIds = contentIds;
    }

    /**
     * <p>addContentId</p>
     */
    public void addContentId(String contentId) {
        contentIds.add(contentId);
    }

    /**
     * <p>Compares one space to another</p>
     *
     * @return true if the spaces metadata and contents are equal
     */
    public boolean equals(Space space) {
        boolean equals = false;
        if(getId().equals(space.getId()) &&
           getMetadata().equals(space.getMetadata()) &&
           getContentIds().equals(space.getContentIds())) {
            equals = true;
        }
        return equals;
    }
}
