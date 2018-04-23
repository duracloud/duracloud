/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import java.io.InputStream;
import java.util.Map;

/**
 * Container for retrieved content items
 *
 * @author Bill Branan
 * Date: 4/20/18
 */
public class RetrievedContent {

    private InputStream contentStream;
    private Map<String, String> contentProperties;

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public Map<String, String> getContentProperties() {
        return contentProperties;
    }

    public void setContentProperties(Map<String, String> contentProperties) {
        this.contentProperties = contentProperties;
    }

}
