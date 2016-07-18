/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.common.model.AclType;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatelessStorageProviderImpl
        implements StatelessStorageProvider {

    @Override
    public StorageProviderType getStorageProviderType(StorageProvider targetProvider) {
        return targetProvider.getStorageProviderType();
    }

    /**
     * {@inheritDoc}
     */
    public String addContent(StorageProvider targetProvider,
                             String storeId,
                             String spaceId,
                             String contentId,
                             String contentMimeType,
                             Map<String, String> userProperties,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) throws StorageException {
        return targetProvider.addContent(spaceId,
                                         contentId,
                                         contentMimeType,
                                         userProperties,
                                         contentSize,
                                         contentChecksum,
                                         content);
    }

    @Override
    public String copyContent(StorageProvider targetProvider,
                              String storeId,
                              String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) throws StorageException {
        return targetProvider.copyContent(sourceSpaceId,
                                          sourceContentId,
                                          destSpaceId,
                                          destContentId);
    }

    /**
     * {@inheritDoc}
     */
    public void createSpace(StorageProvider targetProvider,
                            String storeId,
                            String spaceId)
            throws StorageException {
        targetProvider.createSpace(spaceId);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContent(StorageProvider targetProvider,
                              String storeId,
                              String spaceId,
                              String contentId) throws StorageException {
        targetProvider.deleteContent(spaceId, contentId);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(StorageProvider targetProvider,
                            String storeId,
                            String spaceId)
            throws StorageException {
        targetProvider.deleteSpace(spaceId);
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContent(StorageProvider targetProvider,
                                  String storeId,
                                  String spaceId,
                                  String contentId) throws StorageException {
        return targetProvider.getContent(spaceId, contentId);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentProperties(StorageProvider targetProvider,
                                                    String storeId,
                                                    String spaceId,
                                                    String contentId)
            throws StorageException {
        return targetProvider.getContentProperties(spaceId, contentId);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaceContents(StorageProvider targetProvider,
                                             String storeId,
                                             String spaceId,
                                             String prefix)
            throws StorageException {
        return targetProvider.getSpaceContents(spaceId, prefix);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getSpaceContentsChunked(StorageProvider targetProvider,
                                                String storeId,
                                                String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker)
            throws StorageException {
        return targetProvider.getSpaceContentsChunked(spaceId,
                                                      prefix,
                                                      maxResults,
                                                      marker);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getSpaceProperties(StorageProvider targetProvider,
                                                  String storeId,
                                                  String spaceId) throws StorageException {
        return targetProvider.getSpaceProperties(spaceId);
    }

    @Override
    public Map<String, AclType> getSpaceACLs(StorageProvider targetProvider,
                                            String storeId,
                                            String spaceId)
        throws StorageException {
        return targetProvider.getSpaceACLs(spaceId);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces(StorageProvider targetProvider,
                                      String storeId)
            throws StorageException {
        return targetProvider.getSpaces();
    }

    /**
     * {@inheritDoc}
     */
    public void setContentProperties(StorageProvider targetProvider,
                                     String storeId,
                                     String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties)
            throws StorageException {
        targetProvider.setContentProperties(spaceId, contentId, contentProperties);
    }

    @Override
    public void setSpaceACLs(StorageProvider targetProvider,
                             String storeId,
                             String spaceId,
                             Map<String, AclType> spaceACLs)
        throws StorageException {
        targetProvider.setSpaceACLs(spaceId, spaceACLs);
    }

}
