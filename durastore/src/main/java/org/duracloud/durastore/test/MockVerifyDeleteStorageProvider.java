/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.test;

import org.duracloud.common.model.AclType;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Storage Provider implementation used for testing verify deletion AOP
 *
 * @author: Bill Branan
 * Date: Oct 12, 2009
 */
public class MockVerifyDeleteStorageProvider implements StorageProvider {

    private static int getSpacePropertiesAttempts = 0;
    private static String lastFailedSpacePropertiesId;

    @Override
    public StorageProviderType getStorageProviderType() {
        return null;
    }

    public Iterator<String> getSpaces() {
        // Default method body
        return null;
    }

    public Iterator<String> getSpaceContents(String spaceId, String prefix) {
        // Default method body
        return null;
    }

    public List<String> getSpaceContentsChunked(String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker) {
        // Default method body
        return null;
    }

    public void createSpace(String spaceId) {
        // Default method body
    }

    public void deleteSpace(String spaceId) {
        // Default method body
    }

    /**
     * Provides a successful response the number of times
     * indicated by the spaceId. After which the next request
     * fails and resets the count.
     */
    public Map<String, String> getSpaceProperties(String spaceId) {
        // spaceId indicates the number of tries until failure
        int attemptsBeforeFailure = Integer.valueOf(spaceId);

        // allow the most recent failed id to complete successfully
        if(spaceId.equals(lastFailedSpacePropertiesId)) {
            return null;
        }

        if(getSpacePropertiesAttempts < attemptsBeforeFailure) {
            getSpacePropertiesAttempts++;
            return null;
        } else {
            getSpacePropertiesAttempts = 0;
            lastFailedSpacePropertiesId = spaceId;
            throw new StorageException(attemptsBeforeFailure +
                                       " calls required before success.",
                                       StorageException.NO_RETRY);
        }
    }

    public void setSpaceProperties(String spaceId,
                                   Map<String, String> spaceProperties) {
        // Default method body
    }

    @Override
    public Map<String, AclType> getSpaceACLs(String spaceId) {
        // Default method body
        return null;
    }

    @Override
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs) {
        // Default method body
    }

    public String addContent(String spaceId, String contentId,
                             String contentMimeType,
                             Map<String, String> userProperties,
                             long contentSize,
                             String contentChecksum, InputStream content) {
        // Default method body
        return null;
    }

    @Override
    public String copyContent(String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) {
        // Default method body
        return null;
    }

    public InputStream getContent(String spaceId, String contentId) {
        // Default method body
        return null;
    }

    public void deleteContent(String spaceId, String contentId) {
        // Default method body
    }

    public void setContentProperties(String spaceId, String contentId,
                                     Map<String, String> contentProperties) {
        // Default method body
    }

    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId) {
        // Default method body
        return null;
    }
}
