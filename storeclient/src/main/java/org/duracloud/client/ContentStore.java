/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.domain.Content;
import org.duracloud.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.InvalidIdException;
import org.duracloud.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides access to a content store
 *
 * @author Bill Branan
 */
public interface ContentStore {

    public enum AccessType {OPEN, CLOSED};

    /** Basic space metadata: Created date */
    public static final String SPACE_CREATED =
            StorageProvider.METADATA_SPACE_CREATED;

    /** Basic space metadata: Count of items in a space */
    public static final String SPACE_COUNT =
            StorageProvider.METADATA_SPACE_COUNT;

    /** Basic space metadata: Space access (OPEN or CLOSED) */
    public static final String SPACE_ACCESS =
            StorageProvider.METADATA_SPACE_ACCESS;

    /** Basic content metadata: MIME type */
    public static final String CONTENT_MIMETYPE =
            StorageProvider.METADATA_CONTENT_MIMETYPE;

    /** Basic content metadata: Size */
    public static final String CONTENT_SIZE =
            StorageProvider.METADATA_CONTENT_SIZE;

    /** Basic content metadata: MD5 checksum */
    public static final String CONTENT_CHECKSUM =
            StorageProvider.METADATA_CONTENT_CHECKSUM;

    /** Basic content metadata: Last modified date */
    public static final String CONTENT_MODIFIED =
            StorageProvider.METADATA_CONTENT_MODIFIED;

    /**
     * Gets the base URL pointing to the DuraCloud DuraStore REST API
     */
    public String getBaseURL();
    
    /**
     * Gets the ID of this content store
     */
    public String getStoreId();

    /**
     * Gets the storage provider type
     * {@link org.duracloud.storage.domain.StorageProviderType}
     */
    public String getStorageProviderType();

    /**
     * Provides a listing of all spaces IDs.
     *
     * @return Iterator listing spaceIds
     * @throws ContentStoreException if an error occurs
     */
    public List<String> getSpaces() throws ContentStoreException;

    /**
     * Retrieves the complete list of content items within a space. For spaces
     * with many content items, the list is paged and updated automatically in
     * order to retrieve the entire list.
     *
     * This method is equivalent to getSpaceContents(spaceId, null)
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @return Iterator for content IDs
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Iterator<String> getSpaceContents(String spaceId)
        throws ContentStoreException;

    /**
     * Retrieves the complete list of content items within a space. For spaces
     * with many content items, the list is paged and updated automatically in
     * order to retrieve the entire list. Allows for limiting the content id
     * list to items which start with a given prefix.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param prefix only retrieve content ids with this prefix (null for all content ids)
     * @return Iterator for content IDs
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Iterator<String> getSpaceContents(String spaceId, String prefix)
        throws ContentStoreException;

    /**
     * Provides a space, including the id and metadata of the space as well as
     * a limited list of the content items within a space. This call allows for
     * manual paging of content IDs using the maxResults and marker parameters.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param prefix only retrieve content ids with this prefix (null for all content ids)
     * @param maxResults the maximum number of content ids to return in the list (0 indicates default - which is 1000)
     * @param marker the content id marking the last item in the previous set (null to specify first set of ids)
     * @return Space
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Space getSpace(String spaceId,
                          String prefix,
                          long maxResults,
                          String marker)
        throws ContentStoreException;

    /**
     * Creates a new space. Depending on the storage implementation, the spaceId
     * may be changed somewhat to comply with the naming rules of the underlying
     * storage provider. The same spaceId value used here can be used in all
     * other methods, as the conversion will be applied internally, however a
     * call to getSpaces() may not include a space with exactly this same name.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param spaceMetadata a map of metadata entries for the space
     * @throws InvalidIdException if the space ID is not valid
     * @throws ContentStoreException if the space already exists or cannot be created
     */
    public void createSpace(String spaceId, Map<String, String> spaceMetadata)
            throws ContentStoreException;   

    /**
     * Deletes a space.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void deleteSpace(String spaceId) throws ContentStoreException;
    
    /**
     * Retrieves the metadata associated with a space.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @return Map of space metadata or null if no metadata exists
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Map<String, String> getSpaceMetadata(String spaceId)
            throws ContentStoreException;
    
    /**
     * Sets the metadata associated with a space. Only values included
     * in the  metadata map will be updated, others will remain unchanged.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param spaceMetadata a map of metadata entries for the space
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void setSpaceMetadata(String spaceId,
                                 Map<String, String> spaceMetadata)
            throws ContentStoreException;
    
    /**
     * Gets the access setting of the space, either OPEN or CLOSED. An OPEN
     * space is available for public viewing. A CLOSED space requires
     * authentication prior to viewing any of the contents.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @return AccessType - OPEN or CLOSED
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public AccessType getSpaceAccess(String spaceId) throws ContentStoreException;
    
    /**
     * Sets the accessibility of a space to either OPEN or CLOSED.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param spaceAccess the access setting for this space - OPEN or CLOSED
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void setSpaceAccess(String spaceId, AccessType spaceAccess)
            throws ContentStoreException;
    
    /**
     * Adds content to a space. The contentId of the new content item can
     * include "/" symbols to indicate a folder heirarchy.
     * Returns the checksum of the content as computed by the
     * underlying storage provider to facilitate comparison
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param contentId the identifier of the new content item
     * @param content the new content as a stream
     * @param contentSize the size of the content
     * @param contentMimeType the MIME type of the content
     * @param contentChecksum the MD5 checksum of the content, or null if the checksum is not known
     * @param contentMetadata a map of metadata values to be linked to this content
     * @throws InvalidIdException if the content ID is not valid
     * @throws NotFoundException if the space does not exist
     * @throws ContentStoreException if an error occurs
     * @return content checksum
     */
    public String addContent(String spaceId,
                             String contentId,
                             InputStream content,
                             long contentSize,
                             String contentMimeType,
                             String contentChecksum,
                             Map<String, String> contentMetadata)
            throws ContentStoreException;

    /**
     * Gets content from a space.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @return the content stream
     * @throws NotFoundException if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Content getContent(String spaceId, String contentId)
            throws ContentStoreException;

    /**
     * Removes content from a space.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @throws NotFoundException if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void deleteContent(String spaceId, String contentId)
            throws ContentStoreException;

    /**
     * Sets the metadata associated with content. This effectively removes all
     * of the current content metadata and adds a new set of metadata. Some
     * metadata, such as system metadata provided by the underlying storage
     * system, cannot be updated or removed. Some of the values which cannot be
     * updated or removed: content-checksum, content-modified, content-size
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @param contentMetadata a map of metadata values to be linked to this content
     * @throws NotFoundException if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void setContentMetadata(String spaceId,
                                   String contentId,
                                   Map<String, String> contentMetadata)
            throws ContentStoreException;

    /**
     * Retrieves the metadata associated with content. This includes both
     * metadata generated by the underlying storage system and user defined
     * metadata
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @return the map of metadata values linked to the given contentId
     * @throws NotFoundException if the space or content does not exist
     * @throws ContentStoreException if an error occurs 
     */
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId)
            throws ContentStoreException;

    /**
     * Checks a space ID to ensure that it conforms to all restrictions
     *
     * @param spaceId ID to validate
     * @throws InvalidIdException if the space ID is invalid
     */
    public void validateSpaceId(String spaceId) throws InvalidIdException;

    /**
     * Checks a content ID to ensure that it conforms to all restrictions
     *
     * @param contentId ID to validate
     * @throws InvalidIdException if the content ID is invalid
     */
    public void validateContentId(String contentId) throws InvalidIdException;

    /**
     * Gets a listing of the supported tasks. A task is an activity which is
     * outside of the standard set of storage activites but is available
     * through one or more storage providers.
     *
     * @return the return value of the task
     */
    public List<String> getSupportedTasks()
        throws ContentStoreException;

    /**
     * Perform a task which is outside of the standard set of storage activites
     * but is available through one or more storage providers.
     *
     * @param taskName the name of the task to be performed
     * @param taskParameters the parameters of the task, what is included here
     *                       and how the information is formatted is
     *                       task-specific
     * @return the return value of the task
     */
    public String performTask(String taskName, String taskParameters)
        throws ContentStoreException;

}
