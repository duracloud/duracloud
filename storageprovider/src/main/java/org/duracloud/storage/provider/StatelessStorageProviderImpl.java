/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider.AccessType;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatelessStorageProviderImpl
        implements StatelessStorageProvider {

    /**
     * {@inheritDoc}
     */
    public String addContent(StorageProvider targetProvider,
                             String storeId,
                             String spaceId,
                             String contentId,
                             String contentMimeType,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) throws StorageException {
        return targetProvider.addContent(spaceId,
                                         contentId,
                                         contentMimeType,
                                         contentSize,
                                         contentChecksum,
                                         content);
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
    public Map<String, String> getContentMetadata(StorageProvider targetProvider,
                                                  String storeId,
                                                  String spaceId,
                                                  String contentId)
            throws StorageException {
        return targetProvider.getContentMetadata(spaceId, contentId);
    }

    /**
     * {@inheritDoc}
     */
    public AccessType getSpaceAccess(StorageProvider targetProvider,
                                     String storeId,
                                     String spaceId) throws StorageException {
        return targetProvider.getSpaceAccess(spaceId);
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
    public Map<String, String> getSpaceMetadata(StorageProvider targetProvider,
                                                String storeId,
                                                String spaceId) throws StorageException {
        return targetProvider.getSpaceMetadata(spaceId);
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
    public void setContentMetadata(StorageProvider targetProvider,
                                   String storeId,
                                   String spaceId,
                                   String contentId,
                                   Map<String, String> contentMetadata)
            throws StorageException {
        targetProvider.setContentMetadata(spaceId, contentId, contentMetadata);
    }

    /**
     * {@inheritDoc}
     */
    public void setSpaceAccess(StorageProvider targetProvider,
                               String storeId,
                               String spaceId,
                               AccessType access) throws StorageException {
        targetProvider.setSpaceAccess(spaceId, access);
    }

    /**
     * {@inheritDoc}
     */
    public void setSpaceMetadata(StorageProvider targetProvider,
                                 String storeId,
                                 String spaceId,
                                 Map<String, String> spaceMetadata)
            throws StorageException {
        targetProvider.setSpaceMetadata(spaceId, spaceMetadata);
    }

}
