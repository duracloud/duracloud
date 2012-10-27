/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chron;

/**
 * @author Andrew Woods
 *         Date: 10/25/12
 */
public enum RequestKey {

    CONTENT_MD5("Content-MD5");

    private String text;

    RequestKey(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }

    public static RequestKey fromString(String text) {
        if (null == text || text.isEmpty()) {
            return null;
        }

        for (RequestKey key : values()) {
            if (text.equalsIgnoreCase(key.toString())) {
                return key;
            }
        }
        return null;
    }

}
