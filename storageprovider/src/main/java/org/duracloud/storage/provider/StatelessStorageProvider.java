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
import java.util.Map;
import java.util.List;

public interface StatelessStorageProvider {

    public abstract StorageProviderType getStorageProviderType(StorageProvider targetProvider);

    public abstract String addContent(StorageProvider targetProvider,
                                      String storeId,
                                      String spaceId,
                                      String contentId,
                                      String contentMimeType,
                                      Map<String, String> userProperties,
                                      long contentSize,
                                      String contentChecksum,
                                      InputStream content)
            throws StorageException;

    public String copyContent(StorageProvider targetProvider,
                              String storeId,
                              String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId)
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

    public abstract Map<String, String> getContentProperties(StorageProvider targetProvider,
                                                             String storeId,
                                                             String spaceId,
                                                             String contentId)
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

    public abstract Map<String, String> getSpaceProperties(StorageProvider targetProvider,
                                                           String storeId,
                                                           String spaceId)
            throws StorageException;

    public abstract Map<String, AclType> getSpaceACLs(StorageProvider targetProvider,
                                                      String storeId,
                                                      String spaceId)
        throws StorageException;

    public abstract Iterator<String> getSpaces(StorageProvider targetProvider,
                                               String storeId)
            throws StorageException;

    public abstract void setContentProperties(StorageProvider targetProvider,
                                              String storeId,
                                              String spaceId,
                                              String contentId,
                                              Map<String, String> contentProperties)
            throws StorageException;

    public abstract void setSpaceACLs(StorageProvider targetProvider,
                                      String storeId,
                                      String spaceId,
                                      Map<String, AclType> spaceACLs)
        throws StorageException;

}