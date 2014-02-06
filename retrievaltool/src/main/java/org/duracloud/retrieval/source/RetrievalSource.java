/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import org.duracloud.common.model.ContentItem;

import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public interface RetrievalSource {

    /**
     * Provides the next content item to be processed, cycles through all spaces
     * as necessary. Returns null when there are no further content ids to
     * process.
     *
     * @return the next content item to be processed
     */
    public ContentItem getNextContentItem();

    /**
     * Retrieves the Duracloud properties for the specified ContentItem.
     *
     * @param contentItem the file whose properties to retrieve
     * @return the Map of Duracloud properties
     */
    public Map<String,String> getSourceProperties(ContentItem contentItem);

    /**
     * Provides the checksum of the specified source file based on the file's
     * properties.
     *
     * @param contentItem the file to consider
     * @return MD5 checksum of the given file
     */
    public String getSourceChecksum(ContentItem contentItem);

    /**
     * Gets the actual content, including the stream and the checksum.
     *
     * @param contentItem the file to retrieve
     * @return content stream of the specified file
     */
    public ContentStream getSourceContent(ContentItem contentItem);
    
}
