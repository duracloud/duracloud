/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class ContentStream {

    private InputStream stream;
    private String checksum;

    public ContentStream(InputStream stream, String checksum) {
        this.stream = stream;
        this.checksum = checksum;
    }

    public InputStream getStream() {
        return stream;
    }

    public String getChecksum() {
        return checksum;
    }
}
