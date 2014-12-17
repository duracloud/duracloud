/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.durastore.error.ResourceChecksumException;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.durastore.error.ResourceStateException;
import org.duracloud.storage.error.ChecksumMismatchException;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageStateException;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.IdUtil;
import org.duracloud.storage.util.StorageProviderFactory;
import org.duracloud.storage.util.StorageProviderUtil;
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
        } catch (StorageStateException e) {
            throw new ResourceStateException("get content",
                                             spaceID,
                                             contentID,
                                             e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
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
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
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
        } catch (StorageStateException e) {
            throw new ResourceStateException("update properties for content",
                                             spaceID,
                                             contentID,
                                             e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
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
                             Map<String, String> userProperties,
                             long contentSize,
                             String checksum,
                             String storeID)
    throws ResourceException, InvalidIdException {
        IdUtil.validateContentId(contentID);

        try {
            StorageProvider storage =
                storageProviderFactory.getStorageProvider(storeID);
                
            try {
                // overlay new properties on top of older extended properties
                // so that old tags and custom properties are preserved.
                // c.f. https://jira.duraspace.org/browse/DURACLOUD-757
                Map<String, String> oldUserProperties =
                    storage.getContentProperties(spaceID, contentID);
                //remove all non extended properties
                if (userProperties != null) {
                    oldUserProperties.putAll(userProperties);
                    //use old mimetype if none specified.
                    String oldMimetype =
                        oldUserProperties.remove(StorageProvider.PROPERTIES_CONTENT_MIMETYPE);
                    if(contentMimeType == null || contentMimeType.trim() == ""){
                        contentMimeType = oldMimetype;
                    } 
                    
                    oldUserProperties = StorageProviderUtil.removeCalculatedProperties(oldUserProperties);
                }
                
                userProperties = oldUserProperties;
            } catch (NotFoundException ex) {
                // do nothing - no properties to update
                // since file did not previous exist.
            }
               
            return storage.addContent(spaceID,
                                      contentID,
                                      contentMimeType,
                                      userProperties,
                                      contentSize,
                                      checksum,
                                      content);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("add content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (ChecksumMismatchException e) {
            throw new ResourceChecksumException("add content",
                                                spaceID,
                                                contentID,
                                                e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("add content", spaceID, contentID, e);
        }
    }

    /**
     * This method copies the content found in space srcSpaceID with id
     * srcContentID to the space destSpaceID within the same content store
     * (storeID) to the id of destContentID.
     * 
     * @param srcStoreID of content to copy
     * @param srcSpaceID of content to copy
     * @param srcContentID of content to copy
     * @param destSpaceID of copied content
     * @param destContentID of copied content
     * @param destStoreID of copied content
     * @return MD5 checksum of the content as computed by the storage provider
     * @throws ResourceException
     */
    @Override
    public String copyContent(String srcStoreID,
                              String srcSpaceID,
                              String srcContentID,
                              String destStoreID,
                              String destSpaceID,
                              String destContentID) throws ResourceException {
        BrokeredStorageProvider srcProvider = getStorageProvider(srcStoreID);
        BrokeredStorageProvider destProvider = getStorageProvider(destStoreID);

        if(srcProvider.equals(destProvider)){
            return copyContent(srcProvider,
                               srcSpaceID,
                               srcContentID,
                               destSpaceID,
                               destContentID,
                               srcStoreID);
        }else{
            return copyContentBetweenStorageProviders(srcProvider,
                                                      srcSpaceID,
                                                      srcContentID,
                                                      srcStoreID,
                                                      destProvider,
                                                      destSpaceID,
                                                      destContentID,
                                                      destStoreID);
        }
    }

    private BrokeredStorageProvider getStorageProvider(String storeID) {
        return (BrokeredStorageProvider) storageProviderFactory.getStorageProvider(
            storeID);
    }

    private String copyContentBetweenStorageProviders(BrokeredStorageProvider srcStorage,
                                                      String srcSpaceID,
                                                      String srcContentID,
                                                      String srcStoreID,
                                                      BrokeredStorageProvider destStorage,
                                                      String destSpaceID,
                                                      String destContentID,
                                                      String destStoreID) throws ResourceException {
        try {
            InputStream inputStream =
                srcStorage.getContent(srcSpaceID, srcContentID);

            Map<String, String> properties =
                srcStorage.getContentProperties(srcSpaceID, srcContentID);
            
            Long contentSize = null;
            
            try{
                String contentSizeString = properties.get(StorageProvider.PROPERTIES_CONTENT_SIZE);
                if(contentSizeString != null){
                    contentSize = Long.parseLong(contentSizeString);
                }
            } catch(NumberFormatException ex){
                String msg = "content size could not be parsed: " + ex.getMessage();
                log.warn(msg, ex);
            }
            
            String md5 =
                destStorage.addContent(destSpaceID,
                                       destContentID,
                                       properties.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE),
                                       properties,
                                       contentSize,
                                       properties.get(StorageProvider.PROPERTIES_CONTENT_CHECKSUM),
                                       inputStream);
            
            return md5;
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("copy content",
                                                srcStorage.getTargetType().name(),
                                                srcSpaceID,
                                                srcContentID,
                                                destStorage.getTargetType().name(),
                                                destSpaceID,
                                                destContentID,
                                                e);
        } catch (StorageStateException e) {
            throw new ResourceStateException("copy content",
                                             srcStorage.getTargetType().name(),
                                             srcSpaceID,
                                             srcContentID,
                                             destStorage.getTargetType().name(),
                                             destSpaceID,
                                             destContentID,
                                             e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(srcStoreID);
            storageProviderFactory.expireStorageProvider(destStoreID);
            throw new ResourceException("copy content",
                                        srcStorage.getTargetType().name(),
                                        srcSpaceID,
                                        srcContentID,
                                        destStorage.getTargetType().name(),
                                        destSpaceID,
                                        destContentID,
                                        e);
        }
    }

    private String copyContent(StorageProvider storage,
                               String srcSpaceID,
                               String srcContentID,
                               String destSpaceID,
                               String destContentID,
                               String storeID) throws ResourceException {

        try {
            return storage.copyContent(srcSpaceID,
                                       srcContentID,
                                       destSpaceID,
                                       destContentID);

        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("copy content",
                                                srcSpaceID,
                                                srcContentID,
                                                destSpaceID,
                                                destContentID,
                                                e);
        } catch (StorageStateException e) {
            throw new ResourceStateException("copy content",
                                             srcSpaceID,
                                             srcContentID,
                                             destSpaceID,
                                             destContentID,
                                             e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("copy content",
                                        srcSpaceID,
                                        srcContentID,
                                        destSpaceID,
                                        destContentID,
                                        e);
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
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("delete content", spaceID, contentID, e);
        }
    }
}
