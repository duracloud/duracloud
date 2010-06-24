/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.test;

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

    private static int getSpaceMetadataAttempts = 0;
    private static String lastFailedSpaceMetadataId;

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
    public Map<String, String> getSpaceMetadata(String spaceId) {
        // spaceId indicates the number of tries until failure
        int attemptsBeforeFailure = Integer.valueOf(spaceId);

        // allow the most recent failed id to complete successfully
        if(spaceId.equals(lastFailedSpaceMetadataId)) {
            return null;
        }

        if(getSpaceMetadataAttempts < attemptsBeforeFailure) {
            getSpaceMetadataAttempts++;
            return null;
        } else {
            getSpaceMetadataAttempts = 0;
            lastFailedSpaceMetadataId = spaceId;
            throw new StorageException(attemptsBeforeFailure +
                                       " calls required before success.",
                                       StorageException.NO_RETRY);
        }
    }

    public void setSpaceMetadata(String spaceId,
                                 Map<String, String> spaceMetadata) {
        // Default method body
    }

    public AccessType getSpaceAccess(String spaceId) {
        // Default method body
        return AccessType.OPEN;
    }

    public void setSpaceAccess(String spaceId, AccessType access) {
        // Default method body
    }

    public String addContent(String spaceId, String contentId,
                             String contentMimeType, long contentSize,
                             String contentChecksum, InputStream content) {
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

    public void setContentMetadata(String spaceId, String contentId,
                                   Map<String, String> contentMetadata) {
        // Default method body
    }

    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId) {
        // Default method body
        return null;
    }
}
