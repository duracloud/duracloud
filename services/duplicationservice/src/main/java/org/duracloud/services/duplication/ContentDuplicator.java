/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

/**
 * This interface defines the contract of content duplicators.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public interface ContentDuplicator {

    /**
     * This method creates a newly duplicated content item in the arg spaceId
     * with the arg contentId.
     *
     * @param spaceId   of content item
     * @param contentId of content item
     * @return checksum of content
     */
    public String createContent(String spaceId, String contentId);

    /**
     * This method updates an existing content item in the arg spaceId with the
     * arg contentId
     *
     * @param spaceId   of content item
     * @param contentId of content item
     */
    public void updateContent(String spaceId, String contentId);

    /**
     * This method deletes an existing content item in the arg spaceId with the
     * arg contentId
     *
     * @param spaceId   of content item
     * @param contentId of content item
     */
    public void deleteContent(String spaceId, String contentId);

    /**
     * This method performs any necessary clean-up of the ContentDuplicator.
     */
    public void stop();
}
