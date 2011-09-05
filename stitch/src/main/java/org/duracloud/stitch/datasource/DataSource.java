/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.datasource;

import org.duracloud.domain.Content;

/**
 * This interface defines the contract of DataSource used by a FileStitcher.
 *
 * @author Andrew Woods
 *         Date: 9/2/11
 */
public interface DataSource {

    /**
     * This method returns the content item specified by the arg space-id and
     * content-id.
     *
     * @param spaceId   of content item
     * @param contentId of content item
     * @return content
     */
    public Content getContent(String spaceId, String contentId);
}
