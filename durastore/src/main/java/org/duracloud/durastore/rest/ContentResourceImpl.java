/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.durastore.util.StorageProviderFactory;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

/**
 * Provides interaction with content
 *
 * @author Bill Branan
 */
public class ContentResourceImpl implements ContentResource {

    private static final Logger log = LoggerFactory.getLogger(ContentResource.class);

    private StorageProviderFactory storageProviderFactory;

    public ContentResourceImpl(StorageProviderFactory storageProviderFactory) {
        this.storageProviderFactory = storageProviderFactory;
    }

    /**
     * Retrieves content from a space.
     *
     * @param spaceID
     * @param contentID
     * @return InputStream which can be used to read content.
     */
    @Override
    public InputStream getContent(String spaceID,
                                  String contentID,
                                  String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                storageProviderFactory.getStorageProvider(storeID);
            return storage.getContent(spaceID, contentID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("get content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("get content", spaceID, contentID, e);
        }
    }

    /**
     * Retrieves the properties of a piece of content.
     *
     * @param spaceID
     * @param contentID
     * @return Map of content properties
     */
    @Override
    public Map<String, String> getContentProperties(String spaceID,
                                                    String contentID,
                                                    String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                storageProviderFactory.getStorageProvider(storeID);
            return storage.getContentProperties(spaceID, contentID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("get properties for content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("get properties for content",
                                        spaceID,
                                        contentID,
                                        e);
        }
    }

    /**
     * Updates the properties of a piece of content.
     *
     * @return success
     */
    @Override
    public void updateContentProperties(String spaceID,
                                        String contentID,
                                        String contentMimeType,
                                        Map<String, String> userProperties,
                                        String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                storageProviderFactory.getStorageProvider(storeID);

            // Update content properties
            if(userProperties != null) {
                storage.setContentProperties(spaceID, contentID, userProperties);
            }
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("update properties for content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("update properties for content",
                                        spaceID,
                                        contentID,
                                        e);
        }
    }

    /**
     * Adds content to a space.
     *
     * @return the checksum of the content as computed by the storage provider
     */
    @Override
    public String addContent(String spaceID,
                             String contentID,
                             InputStream content,
                             String contentMimeType,
                             int contentSize,
                             String checksum,
                             String storeID)
    throws ResourceException, InvalidIdException {
        IdUtil.validateContentId(contentID);

        try {
            StorageProvider storage =
                storageProviderFactory.getStorageProvider(storeID);

            return storage.addContent(spaceID,
                                      contentID,
                                      contentMimeType,
                                      contentSize,
                                      checksum,
                                      content);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("add content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("add content", spaceID, contentID, e);
        }
    }

    /**
     * Removes a piece of content.
     *
     * @param spaceID
     * @param contentID
     * @return success
     */
    @Override
    public void deleteContent(String spaceID, String contentID, String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                storageProviderFactory.getStorageProvider(storeID);

            storage.deleteContent(spaceID, contentID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("delete content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("delete content", spaceID, contentID, e);
        }
    }
}
