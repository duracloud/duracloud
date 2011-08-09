/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.domain;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

/**
 * Content - a stream of bits and properties to describe the stream.
 *
 * @author Bill Branan
 */
public class Content {
    private String id;
    private Map<String, String> properties = new HashMap<String, String>();
    private InputStream stream = null;

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
     * <p>Adds an item to the content properties map</p>
     *
     * @param name properties key
     * @param value properties value
     */
    public void addProperties(String name, String value) {
        properties.put(name, value);
    }

    /**
     * <p>Getter for the field <code>stream</code>.</p>
     */
    public InputStream getStream() {
        return stream;
    }

    /**
     * <p>Setter for the field <code>stream</code>.</p>
     */
    public void setStream(InputStream stream) {
        this.stream = stream;
    }
}
