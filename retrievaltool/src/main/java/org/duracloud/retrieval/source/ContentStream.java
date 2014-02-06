/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import org.duracloud.client.ContentStore;

import java.io.InputStream;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class ContentStream {

    private InputStream stream;
    private Map<String, String> properties;

    public ContentStream(InputStream stream,
                         Map<String, String> properties) {
        this.stream = stream;
        this.properties = properties;
    }

    public InputStream getStream() {
        return stream;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getChecksum() {
        return getProperties().get(ContentStore.CONTENT_CHECKSUM);
    }

    public String getDateCreated() {
        return getProperties().get(ContentStore.CONTENT_FILE_CREATED);
    }

    public String getDateLastAccessed() {
        return getProperties().get(ContentStore.CONTENT_FILE_ACCESSED);
    }

    public String getDateLastModified() {
        return getProperties().get(ContentStore.CONTENT_FILE_MODIFIED);
    }

}
