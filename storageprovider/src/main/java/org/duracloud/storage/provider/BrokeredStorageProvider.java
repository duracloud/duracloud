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

public class BrokeredStorageProvider
        implements StorageProvider {

    private final StatelessStorageProvider dispatchProvider;

    private final StorageProvider targetProvider;

    private final StorageProviderType targetType;

    private String storeId;

    public BrokeredStorageProvider(StatelessStorageProvider dispatchProvider,
                                   StorageProvider targetProvider,
                                   StorageProviderType targetType,
                                   String storeId) {
        this.dispatchProvider = dispatchProvider;
        this.targetProvider = targetProvider;
        this.targetType = targetType;
        this.storeId = storeId;
    }

    public StorageProviderType getTargetType() {
        return targetType;
    }

    @Override
    public StorageProviderType getStorageProviderType() {
        return dispatchProvider.getStorageProviderType(targetProvider);
    }

    public String addContent(String spaceId,
                             String contentId,
                             String contentMimeType,
                             Map<String, String> userProperties,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) throws StorageException {

        return dispatchProvider.addContent(targetProvider,
                                           storeId,
                                           spaceId,
                                           contentId,
                                           contentMimeType,
                                           userProperties,
                                           contentSize,
                                           contentChecksum,
                                           content);
    }

    @Override
    public String copyContent(String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) {
        return dispatchProvider.copyContent(targetProvider,
                                            storeId,
                                            sourceSpaceId,
                                            sourceContentId,
                                            destSpaceId,
                                            destContentId);
    }

    public void createSpace(String spaceId) throws StorageException {
        dispatchProvider.createSpace(targetProvider, storeId, spaceId);
    }

    public void deleteContent(String spaceId, String contentId)
            throws StorageException {
        dispatchProvider.deleteContent(targetProvider,
                                       storeId,
                                       spaceId,
                                       contentId);
    }

    public void deleteSpace(String spaceId) throws StorageException {
        dispatchProvider.deleteSpace(targetProvider, storeId, spaceId);

    }

    public InputStream getContent(String spaceId, String contentId)
            throws StorageException {
        return dispatchProvider.getContent(targetProvider,
                                           storeId,
                                           spaceId,
                                           contentId);
    }

    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId)
            throws StorageException {
        return dispatchProvider.getContentProperties(targetProvider,
                                                     storeId,
                                                     spaceId,
                                                     contentId);
    }

    public Iterator<String> getSpaceContents(String spaceId, String prefix)
            throws StorageException {
        return dispatchProvider.getSpaceContents(targetProvider,
                                                 storeId,
                                                 spaceId,
                                                 prefix);
    }

    public List<String> getSpaceContentsChunked(String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker)
        throws StorageException {
        return dispatchProvider.getSpaceContentsChunked(targetProvider,
                                                        storeId,
                                                        spaceId,
                                                        prefix,
                                                        maxResults,
                                                        marker);
    }

    public Map<String, String> getSpaceProperties(String spaceId)
        throws StorageException {
        return dispatchProvider.getSpaceProperties(targetProvider,
                                                   storeId,
                                                   spaceId);
    }

    public Iterator<String> getSpaces() throws StorageException {
        return dispatchProvider.getSpaces(targetProvider, storeId);
    }

    public void setContentProperties(String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties)
            throws StorageException {
        dispatchProvider.setContentProperties(targetProvider,
                                              storeId,
                                              spaceId,
                                              contentId,
                                              contentProperties);
    }

    @Override
    public Map<String, AclType> getSpaceACLs(String spaceId) {
        return dispatchProvider.getSpaceACLs(targetProvider, storeId, spaceId);
    }

    @Override
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs) {
        dispatchProvider.setSpaceACLs(targetProvider,
                                      storeId,
                                      spaceId,
                                      spaceACLs);
    }

}
