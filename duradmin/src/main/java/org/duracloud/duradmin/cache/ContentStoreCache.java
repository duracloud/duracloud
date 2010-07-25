/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.cache;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.InvalidIdException;
import org.duracloud.domain.Content;
import org.duracloud.domain.Space;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContentStoreCache
        implements ContentStore {

    private ContentStore backendStore;

    private Set<String> spaceIdCache;

    public void setContentStore(ContentStore contentStore) {
        this.backendStore = contentStore;
    }

    public ContentStore getContentStore() {
        return this.backendStore;
    }

    public String getBaseURL() {
        return this.backendStore.getBaseURL();
    }

    public String getStoreId() {
        return this.backendStore.getStoreId();
    }

    public String getStorageProviderType() {
        return this.backendStore.getStorageProviderType();
    }

    /**
     * Provides a listing of all spaces. Spaces in the list include metadata but
     * not a listing of content.
     * 
     * @return Iterator listing spaceIds
     * @throws ContentStoreException
     */
    public List<String> getSpaces() throws ContentStoreException {
        if (spaceIdCache == null) {
            refreshCache();
        }

        return new LinkedList<String>(spaceIdCache);
    }

    private void refreshCache() throws ContentStoreException{
        if(spaceIdCache == null){
            spaceIdCache = new HashSet<String>();
        }
        
        List<String> spaces = this.backendStore.getSpaces();
        synchronized (spaceIdCache) {
            spaceIdCache.clear();
            spaceIdCache.addAll(spaces);
        }
    }

    /**
     * Provides a list of space contents.
     *
     * @return Iterator of content Ids
     * @throws ContentStoreException
     */
    public Iterator<String> getSpaceContents(String spaceId)
        throws ContentStoreException {
        return this.backendStore.getSpaceContents(spaceId);
    }

    /**
     * Provides a list of space contents.
     *
     * @return Iterator of content Ids
     * @throws ContentStoreException
     */
    public Iterator<String> getSpaceContents(String spaceId, String prefix)
        throws ContentStoreException {
        return this.backendStore.getSpaceContents(spaceId, prefix);
    }

    /**
     * {@inheritDoc}
     */
    public Space getSpace(String spaceId,
                          String prefix,
                          long maxResults,
                          String marker) throws ContentStoreException {
        return this.backendStore.getSpace(spaceId, prefix, maxResults, marker);
    }

    /**
     * Creates a new space. Depending on the storage implementation, the spaceId
     * may be changed somewhat to comply with the naming rules of the underlying
     * storage provider. The same spaceId value used here can be used in all
     * other methods, as the conversion will be applied internally, however a
     * call to getSpaces() may not include a space with exactly this same name.
     * 
     * @param spaceId
     * @throws ContentStoreException
     */
    public void createSpace(String spaceId, Map<String, String> spaceMetadata)
            throws ContentStoreException {
        //TODO implement
    }

    /**
     * Deletes a space.
     * 
     * @param spaceId
     * @throws ContentStoreException
     */
    public void deleteSpace(String spaceId) throws ContentStoreException {
        //TODO implement
    }

    /**
     * Retrieves the metadata associated with a space.
     * 
     * @param spaceId
     * @return Map of space metadata or null if no metadata exists
     * @throws ContentStoreException
     */
    public Map<String, String> getSpaceMetadata(String spaceId)
            throws ContentStoreException {
        //TODO implement
        return null;
    }

    /**
     * Sets the metadata associated with a space. Only values included in the
     * metadata map will be updated, others will remain unchanged.
     * 
     * @param spaceId
     * @param spaceMetadata
     * @throws ContentStoreException
     */
    public void setSpaceMetadata(String spaceId,
                                 Map<String, String> spaceMetadata)
            throws ContentStoreException {
        //TODO implement
    }

    /**
     * Gets the access setting of the space, either OPEN or CLOSED. An OPEN
     * space is available for public viewing. A CLOSED space requires
     * authentication prior to viewing any of the contents.
     * 
     * @param spaceId
     * @return
     * @throws ContentStoreException
     */
    public AccessType getSpaceAccess(String spaceId)
            throws ContentStoreException {
        //TODO implement
        return null;
    }

    /**
     * Sets the accessibility of a space to either OPEN or CLOSED.
     * 
     * @param spaceId
     * @param spaceAccess
     * @throws ContentStoreException
     */
    public void setSpaceAccess(String spaceId, AccessType spaceAccess)
            throws ContentStoreException {
        //TODO implement
    }

    /**
     * Adds content to a space. Returns the checksum of the content as computed
     * by the underlying storage provider to facilitate comparison
     * 
     * @param spaceId
     * @param contentId
     * @param content
     * @param contentMimeType
     * @param contentSize
     * @param contentChecksum
     * @param contentMetadata
     * @return
     * @throws ContentStoreException
     */
    public String addContent(String spaceId,
                             String contentId,
                             InputStream content,
                             long contentSize,
                             String contentMimeType,
                             String contentChecksum,
                             Map<String, String> contentMetadata)
            throws ContentStoreException {
        //TODO implement
        return null;

    }

    /**
     * Gets content from a space.
     * 
     * @param spaceId
     * @param contentId
     * @return the content stream or null if the content does not exist
     * @throws ContentStoreException
     */
    public Content getContent(String spaceId, String contentId)
            throws ContentStoreException {
        //TODO implement
        return null;
    }

    /**
     * Removes content from a space.
     * 
     * @param spaceId
     * @param contentId
     * @throws ContentStoreException
     */
    public void deleteContent(String spaceId, String contentId)
            throws ContentStoreException {
        //TODO implement
    }

    /**
     * Sets the metadata associated with content. This effectively removes all
     * of the current content metadata and adds a new set of metadata. Some
     * metadata, such as system metadata provided by the underlying storage
     * system, cannot be updated or removed. Some of the values which cannot be
     * updated or removed: content-checksum content-modified content-size
     * 
     * @param spaceId
     * @param contentId
     * @param contentMetadata
     * @throws ContentStoreException
     */
    public void setContentMetadata(String spaceId,
                                   String contentId,
                                   Map<String, String> contentMetadata)
            throws ContentStoreException {
        //TODO implement 
    }

    /**
     * Retrieves the metadata associated with content. This includes both
     * metadata generated by the underlying storage system as well as
     * 
     * @param spaceId
     * @param contentId
     * @return
     * @throws ContentStoreException
     */
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId)
            throws ContentStoreException {
        return null;
    }

    public void validateSpaceId(String spaceId) throws InvalidIdException {
        this.backendStore.validateSpaceId(spaceId);
    }

    public void validateContentId(String contentId) throws InvalidIdException {
        this.backendStore.validateContentId(contentId);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getSupportedTasks()
        throws ContentStoreException {
        //TODO implement
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String performTask(String taskName, String taskParameters)
        throws ContentStoreException {
        //TODO implement
        return null;
    }

    /**
     * {@inheritDoc}
     */    
    public String getTaskStatus(String taskName)
        throws ContentStoreException {
        //TODO implement
        return null;
    }
}
