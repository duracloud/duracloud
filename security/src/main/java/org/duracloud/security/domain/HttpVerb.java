/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.domain;

/**
 * This enum provides helper methods to the HTTP verbs.
 * 
 * @author Andrew Woods
 *         Date: 11/18/11
 */
public enum HttpVerb {

    GET(true), HEAD(true), PUT(false), POST(false), DELETE(false);

    private boolean isRead;

    HttpVerb(boolean isRead) {
        this.isRead = isRead;
    }

    public static HttpVerb fromString(String text) {
        return valueOf(text.toUpperCase());
    }

    public boolean isRead() {
        return isRead;
    }
}
