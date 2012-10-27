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
public enum ResponseKey {

    RETRY_AFTER("Retry-After"),
    ACCT_ID("accountId"),
    IDENTIFIER("identifier"),
    REQUEST_TYPE("requestType"),
    SPACE_ID("spaceId"),
    ITEM_ID("itemId"),
    STATUS("status"),
    STATUS_MSG("statusMessage"),
    HTTP_STATUS("httpStatus");

    private String text;

    ResponseKey(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }

    public static ResponseKey fromString(String text) {
        if (null == text || text.isEmpty()) {
            return null;
        }

        for (ResponseKey key : values()) {
            if (text.equalsIgnoreCase(key.toString())) {
                return key;
            }
        }
        return null;
    }

}
