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
import java.util.Map;
import java.util.List;

public interface StatelessStorageProvider {

    public abstract String addContent(StorageProvider targetProvider,
                                      String storeId,
                                      String spaceId,
                                      String contentId,
                                      String contentMimeType,
                                      long contentSize,
                                      String contentChecksum,
                                      InputStream content)
            throws StorageException;

    public abstract void createSpace(StorageProvider targetProvider,
                                     String storeId,
                                     String spaceId)
            throws StorageException;

    public abstract void deleteContent(StorageProvider targetProvider,
                                       String storeId,
                                       String spaceId,
                                       String contentId)
            throws StorageException;

    public abstract void deleteSpace(StorageProvider targetProvider,
                                     String storeId,
                                     String spaceId)
            throws StorageException;

    public abstract InputStream getContent(StorageProvider targetProvider,
                                           String storeId,
                                           String spaceId,
                                           String contentId)
            throws StorageException;

    public abstract Map<String, String> getContentMetadata(StorageProvider targetProvider,
                                                           String storeId,
                                                           String spaceId,
                                                           String contentId)
            throws StorageException;

    public abstract AccessType getSpaceAccess(StorageProvider targetProvider,
                                              String storeId,
                                              String spaceId)
            throws StorageException;

    public abstract Iterator<String> getSpaceContents(StorageProvider targetProvider,
                                                      String storeId,
                                                      String spaceId,
                                                      String prefix)
            throws StorageException;

    public List<String> getSpaceContentsChunked(StorageProvider targetProvider,
                                                String storeId,
                                                String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker)
            throws StorageException;

    public abstract Map<String, String> getSpaceMetadata(StorageProvider targetProvider,
                                                         String storeId,
                                                         String spaceId)
            throws StorageException;

    public abstract Iterator<String> getSpaces(StorageProvider targetProvider,
                                               String storeId)
            throws StorageException;

    public abstract void setContentMetadata(StorageProvider targetProvider,
                                            String storeId,
                                            String spaceId,
                                            String contentId,
                                            Map<String, String> contentMetadata)
            throws StorageException;

    public abstract void setSpaceAccess(StorageProvider targetProvider,
                                        String storeId,
                                        String spaceId,
                                        AccessType access)
            throws StorageException;

    public abstract void setSpaceMetadata(StorageProvider targetProvider,
                                          String storeId,
                                          String spaceId,
                                          Map<String, String> spaceMetadata)
            throws StorageException;

}