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
     * Provides the checksum of the specified source file based on the file's
     * metadata.
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
