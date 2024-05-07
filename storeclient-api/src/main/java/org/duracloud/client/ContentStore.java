/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.common.model.AclType;
import org.duracloud.common.retry.ExceptionHandler;
import org.duracloud.domain.Content;
import org.duracloud.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.InvalidIdException;
import org.duracloud.error.NotFoundException;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReport;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReportProperties;
import org.duracloud.storage.provider.StorageProvider;

/**
 * Provides access to a content store
 *
 * @author Bill Branan
 */
public interface ContentStore {

    /**
     * Basic space properties: Created date
     */
    public static final String SPACE_CREATED =
        StorageProvider.PROPERTIES_SPACE_CREATED;

    /**
     * Basic space properties: Count of items in a space
     */
    public static final String SPACE_COUNT =
        StorageProvider.PROPERTIES_SPACE_COUNT;

    /**
     * Basic space properties: Size of space
     */
    public static final String SPACE_SIZE =
        StorageProvider.PROPERTIES_SPACE_SIZE;

    /**
     * Basic content properties: MIME type
     */
    public static final String CONTENT_MIMETYPE =
        StorageProvider.PROPERTIES_CONTENT_MIMETYPE;

    /**
     * Basic content properties: Size
     */
    public static final String CONTENT_SIZE =
        StorageProvider.PROPERTIES_CONTENT_SIZE;

    /**
     * Basic content properties: MD5 checksum
     */
    public static final String CONTENT_CHECKSUM =
        StorageProvider.PROPERTIES_CONTENT_CHECKSUM;

    /**
     * Basic content properties: Last modified date
     */
    public static final String CONTENT_MODIFIED =
        StorageProvider.PROPERTIES_CONTENT_MODIFIED;

    /**
     * Original content file properties: Creation date
     */
    public static final String CONTENT_FILE_CREATED =
        StorageProvider.PROPERTIES_CONTENT_FILE_CREATED;

    /**
     * Original content file properties: Last accessed date
     */
    public static final String CONTENT_FILE_ACCESSED =
        StorageProvider.PROPERTIES_CONTENT_FILE_LAST_ACCESSED;

    /**
     * Original content file properties: Last modified date
     */
    public static final String CONTENT_FILE_MODIFIED =
        StorageProvider.PROPERTIES_CONTENT_FILE_MODIFIED;

    public static final String HLS_STREAMING_HOST = StorageProvider.PROPERTIES_HLS_STREAMING_HOST;
    public static final String HLS_STREAMING_TYPE = StorageProvider.PROPERTIES_HLS_STREAMING_TYPE;

    /**
     * Gets the base URL pointing to the DuraCloud DuraStore REST API
     */
    public String getBaseURL();

    /**
     * Gets the ID of this content store
     */
    public String getStoreId();

    /**
     * Indicates whether or not the content store is writable by non root users.
     * @return
     */
    public boolean isWritable();

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
     * @throws NotFoundException     if the space does not exist
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
     * @param prefix  only retrieve content ids with this prefix (null for all content ids)
     * @return Iterator for content IDs
     * @throws NotFoundException     if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Iterator<String> getSpaceContents(String spaceId, String prefix)
        throws ContentStoreException;

    /**
     * Provides a space, including the id and properties of the space as well as
     * a limited list of the content items within a space. This call allows for
     * manual paging of content IDs using the maxResults and marker parameters.
     *
     * @param spaceId    the identifier of the DuraCloud Space
     * @param prefix     only retrieve content ids with this prefix (null for all content ids)
     * @param maxResults the maximum number of content ids to return in the list (0 indicates default - which is 1000)
     * @param marker     the content id marking the last item in the previous set (null to specify first set of ids)
     * @return Space
     * @throws NotFoundException     if the space does not exist
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
     * @throws InvalidIdException    if the space ID is not valid
     * @throws ContentStoreException if the space already exists or cannot be created
     */
    public void createSpace(String spaceId)
        throws ContentStoreException;

    /**
     * Deletes a space.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @throws NotFoundException     if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void deleteSpace(String spaceId) throws ContentStoreException;

    /**
     * Retrieves the properties associated with a space.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @return Map of space properties or empty map if no properties exists
     * @throws NotFoundException     if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Map<String, String> getSpaceProperties(String spaceId)
        throws ContentStoreException;

    /**
     * Retrieves the ACLs associated with a space.
     *
     * @param spaceId the identifier of the DuraCloud Space
     * @return Map of space ACLs or empty map if no properties exists
     * @throws NotFoundException     if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Map<String, AclType> getSpaceACLs(String spaceId)
        throws ContentStoreException;

    /**
     * Sets the ACLs associated with a space. Only values included in the ACLs
     * map will be saved, others will be removed.
     *
     * @param spaceId   the identifier of the DuraCloud Space
     * @param spaceACLs a map of ACL entries for the space (user|group -> right)
     * @throws NotFoundException     if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs)
        throws ContentStoreException;

    /**
     * Determines if a space exists
     *
     * @param spaceId the identifier of the DuraCloud Space to check
     * @return true if the space exists, false otherwise
     * @throws ContentStoreException if an error occurs
     */
    public boolean spaceExists(String spaceId) throws ContentStoreException;

    /**
     * Adds content to a space. The contentId of the new content item can
     * include "/" symbols to indicate a folder heirarchy.
     * Returns the checksum of the content as computed by the
     * underlying storage provider to facilitate comparison
     *
     * @param spaceId           the identifier of the DuraCloud Space
     * @param contentId         the identifier of the new content item
     * @param content           the new content as a stream
     * @param contentSize       the size of the content
     * @param contentMimeType   the MIME type of the content
     * @param contentChecksum   the MD5 checksum of the content, or null if the checksum is not known
     * @param contentProperties a map of properties values to be linked to this content
     * @return content checksum
     * @throws InvalidIdException    if the content ID is not valid
     * @throws NotFoundException     if the space does not exist
     * @throws ContentStoreException if an error occurs
     */
    public String addContent(String spaceId,
                             String contentId,
                             InputStream content,
                             long contentSize,
                             String contentMimeType,
                             String contentChecksum,
                             Map<String, String> contentProperties)
        throws ContentStoreException;

    /**
     * This method copies the content item found in source-space with the id of
     * source-content-id into the dest-space, naming it to dest-content-id.
     *
     * @param srcSpaceId    of content to copy
     * @param srcContentId  of content to copy
     * @param destSpaceId   where copied content will end up
     * @param destContentId given to copied content
     * @return MD5 checksum of destination content item
     * @throws ContentStoreException on error
     */
    public String copyContent(String srcSpaceId,
                              String srcContentId,
                              String destSpaceId,
                              String destContentId) throws ContentStoreException;

    /**
     * This method provides a way to copy a content item to another content provider.
     *
     * @param srcSpaceId    of content to copy
     * @param srcContentId  of content to copy
     * @param destStoreId   where the copied content will end up
     * @param destSpaceId   where copied content will end up
     * @param destContentId given to copied content
     * @return MD5 checksum of destination content item
     * @throws ContentStoreException on error
     */
    public String copyContent(String srcSpaceId,
                              String srcContentId,
                              String destStoreId,
                              String destSpaceId,
                              String destContentId) throws ContentStoreException;

    /**
     * This method moves the content item found in source-space with the id of
     * source-content-id into the dest-space, naming it to dest-content-id.
     *
     * @param srcSpaceId    of content to move
     * @param srcContentId  of content to move
     * @param destSpaceId   where moved content will end up
     * @param destContentId given to moved content
     * @return MD5 checksum of destination content item
     * @throws ContentStoreException on error
     */
    public String moveContent(String srcSpaceId,
                              String srcContentId,
                              String destSpaceId,
                              String destContentId) throws ContentStoreException;

    /**
     * This method moves the content item found in source-space with the id of
     * source-content-id into the dest-space, naming it to dest-content-id.
     *
     * @param srcSpaceId    of content to move
     * @param srcContentId  of content to move
     * @param destStoreId   where moved content will end up
     * @param destSpaceId   where moved content will end up
     * @param destContentId given to moved content
     * @return MD5 checksum of destination content item
     * @throws ContentStoreException on error
     */
    public String moveContent(String srcSpaceId,
                              String srcContentId,
                              String destStoreId,
                              String destSpaceId,
                              String destContentId) throws ContentStoreException;

    /**
     * Gets content from a space.
     *
     * @param spaceId   the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @return the content stream
     * @throws NotFoundException     if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Content getContent(String spaceId, String contentId)
        throws ContentStoreException;

    /**
     * Gets a byte range of a content item from a space.
     * The startByte must be greater than or equal to 0 and less than the content length.
     * The endByte can be null which indicates you would like all bytes beginning with the specified start byte.
     * Otherwise the endByte must be less than the content length and greater than the startByte.
     *
     * @param spaceId   the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @param startByte   the starting byte of the range.
     * @param endByte  The end byte of the range.
     * @return the content stream
     * @throws NotFoundException     if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Content getContent(String spaceId, String contentId, Long startByte, Long endByte)
        throws ContentStoreException;

    /**
     * Removes content from a space.
     *
     * @param spaceId   the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @throws NotFoundException     if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void deleteContent(String spaceId, String contentId)
        throws ContentStoreException;

    /**
     * Sets the properties associated with content. This effectively removes all
     * of the current content properties and adds a new set of properties. Some
     * properties, such as system properties provided by the underlying storage
     * system, cannot be updated or removed. Some of the values which cannot be
     * updated or removed: content-checksum, content-modified, content-size
     *
     * @param spaceId           the identifier of the DuraCloud Space
     * @param contentId         the identifier of the content item
     * @param contentProperties a map of properties values to be linked to this content
     * @throws NotFoundException     if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public void setContentProperties(String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties)
        throws ContentStoreException;

    /**
     * Retrieves the properties associated with content. This includes both
     * properties generated by the underlying storage system and user defined
     * properties
     *
     * @param spaceId   the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item
     * @return the map of properties values linked to the given contentId
     * @throws NotFoundException     if the space or content does not exist
     * @throws ContentStoreException if an error occurs
     */
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId)
        throws ContentStoreException;

    /**
     * Determines if a content item exists in a given space
     *
     * @param spaceId   the identifier of the DuraCloud Space
     * @param contentId the identifier of the content item to check
     * @return true if the content item exists, false otherwise
     * @throws ContentStoreException if an error occurs
     */
    public boolean contentExists(String spaceId, String contentId)
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
     * Perform a task (with retries on failure) which is outside of the standard set of storage activities
     * but is available through one or more storage providers.
     *
     * @param taskName       the name of the task to be performed
     * @param taskParameters the parameters of the task, what is included here
     *                       and how the information is formatted is
     *                       task-specific
     * @return the return value of the task
     */
    public String performTask(String taskName, String taskParameters)
        throws ContentStoreException;

    /**
     * Perform a task (without retrying on failure) which is outside of the standard set of storage activities
     * but is available through one or more storage providers.
     *
     * @param taskName       the name of the task to be performed
     * @param taskParameters the parameters of the task, what is included here
     *                       and how the information is formatted is
     *                       task-specific
     * @return the return value of the task
     */
    public String performTaskWithNoRetries(String taskName, String taskParameters)
        throws ContentStoreException;

    /**
     * Sets the Exception Handler which will be used to process any Exceptions
     * that are thrown when an action fails but will be retried. The default
     * Exception handler logs the exception messages at the WARN level.
     */
    public void setRetryExceptionHandler(ExceptionHandler retryExceptionHandler);

    /**
     * Gets a manifest for the specific space if one exists.  If the space does not
     * exist or the manifest is empty, an exception will be thrown.
     *
     * @param spaceId the space id of the desired manifest
     * @param format
     * @return
     * @throws ContentStoreException
     */
    public InputStream getManifest(String spaceId, ManifestFormat format)
        throws ContentStoreException;

    /**
     * Gets an audit log for the specific space if one exists.  If the space does not
     * exist or the audit is empty, an exception will be thrown.
     *
     * @param spaceId the space id
     * @return
     * @throws ContentStoreException
     */
    public InputStream getAuditLog(String spaceId)
        throws ContentStoreException;

    /**
     * Returns the most recent bit integrity report.
     * If no bit integrity reports exist for this space, a null value is returned.
     *
     * @param spaceId
     * @return
     * @throws ContentStoreException
     */
    public BitIntegrityReport getBitIntegrityReport(String spaceId)
        throws ContentStoreException;

    /**
     * Returns the properties associated with the most recent bit integrity report.
     * If no bit integrity reports exist for this space, a null value is returned.
     *
     * @param spaceId
     * @return
     * @throws ContentStoreException
     */
    public BitIntegrityReportProperties getBitIntegrityReportProperties(String spaceId)
        throws ContentStoreException;

    /**
     * Returns a space stats time series for presenting in a graph.
     *
     * @param spaceId
     * @param from
     * @param to
     * @return
     * @throws ContentStoreException
     */
    public SpaceStatsDTOList getSpaceStats(String spaceId, Date from, Date to) throws ContentStoreException;

    /**
     * Returns a base based series of stats for all spaces within a storage provider for the
     * specified time range.
     *
     * @param from
     * @param to
     * @return
     * @throws ContentStoreException
     */
    public SpaceStatsDTOList getStorageProviderStats(Date from, Date to)
        throws ContentStoreException;

    /**
     * Returns stats for all spaces within a storage provider for a particular
     * day, averaging bit and object counts between 0:00:00 and 23:59:59 UTC.
     *
     * @param date
     * @return
     * @throws ContentStoreException
     */
    public SpaceStatsDTOList getStorageProviderStatsByDay(Date date)
        throws ContentStoreException;
}
