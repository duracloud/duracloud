/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.constant;

/**
 * This enum defines the supported output manifest formats.
 *
 * @author Bill Branan
 * Date: 12/15/14
 */
public enum ManifestFormat {
    TSV("text/tab-separated-values"),
    EXTENDED_TSV("text/tab-separated-values"),
    BAGIT("text/bagit");

    private String mimeType;

    private ManifestFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
