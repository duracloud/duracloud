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
    private Map<String, String> properties = new HashMap<>();
    private List<String> contentIds = new ArrayList<>();

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
     * <p>Getter for the field <code>properties</code>.</p>
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * <p>Setter for the field <code>properties</code>.</p>
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * <p>Adds an item to the space properties map</p>
     *
     * @param name  properties key
     * @param value properties value
     */
    public void addProperties(String name, String value) {
        properties.put(name, value);
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
     * @return true if the spaces properties and contents are equal
     */
    public boolean equals(Space space) {
        boolean equals = false;
        if (getId().equals(space.getId()) &&
            getProperties().equals(space.getProperties()) &&
            getContentIds().equals(space.getContentIds())) {
            equals = true;
        }
        return equals;
    }
}
