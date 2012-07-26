/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

/**
 * This interface defines the contract of space duplicators.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public interface SpaceDuplicator {

    /**
     * Gets the ID of the store from which spaces are to be retrieved
     * @return storeId of the FROM storage provider
     */
    public String getFromStoreId();

    /**
     * Gets the ID of the store from which spaces are to be duplicated
     * @return storeId of the TO storage provider
     */
    public String getToStoreId();

    /**
     * This method creates a newly duplicated space with the arg spaceId.
     *
     * @param spaceId of duplicated space
     */
    public void createSpace(String spaceId);

    /**
     * This method updates the duplicated space with arg spaceId.
     *
     * @param spaceId of space to update
     */
    public void updateSpace(String spaceId);

    /**
     * This method updates the duplicated space ACL with arg spaceId.
     *
     * @param spaceId of space to update
     */
    public void updateSpaceAcl(String spaceId);

    /**
     * This method deletes the duplicated space with arg spaceId.
     *
     * @param spaceId of space to delete
     */
    public void deleteSpace(String spaceId);

    /**
     * This method performs any necessary clean-up of the SpaceDuplicator.
     */
    public void stop();
}
