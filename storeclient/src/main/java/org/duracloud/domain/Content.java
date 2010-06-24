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
 * Content - a stream of bits and metadata to describe the stream.
 *
 * @author Bill Branan
 */
public class Content {
    private String id;
    private Map<String, String> metadata = new HashMap<String, String>();
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
     * <p>Adds an item to the content metadata map</p>
     *
     * @param name metadata key
     * @param value metadata value
     */
    public void addMetadata(String name, String value) {
        metadata.put(name, value);
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
