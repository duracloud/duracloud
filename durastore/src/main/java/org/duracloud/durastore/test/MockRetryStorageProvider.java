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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Storage Provider implementation used for testing retry AOP
 *
 * @author: Bill Branan
 * Date: Oct 7, 2009
 */
public class MockRetryStorageProvider implements StorageProvider {

    private static int getSpaceContentsAttempts = 0;

    @Override
    public StorageProviderType getStorageProviderType() {
        return null;
    }

    public Iterator<String> getSpaces() {
        throw new StorageException("getSpaces is not retried",
                                   StorageException.NO_RETRY);
    }

    public Iterator<String> getSpaceContents(String spaceId, String prefix) {
        return getSpaceContentsChunked(spaceId, prefix, 0, null).iterator();
    }

    public List<String> getSpaceContentsChunked(String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker) {
        // spaceId indicates the number of tries until success
        int attemptsBeforeSuccess = Integer.valueOf(spaceId);
        if(getSpaceContentsAttempts < attemptsBeforeSuccess) {
            getSpaceContentsAttempts++;
            throw new StorageException(attemptsBeforeSuccess +
                                       " calls required before success.",
                                       StorageException.RETRY);
        } else {
            List<String> retries = new ArrayList<String>();
            retries.add(String.valueOf(getSpaceContentsAttempts));
            getSpaceContentsAttempts = 0;
            return retries;
        }
    }

    public void createSpace(String spaceId) {
        // Default method body
    }

    public void deleteSpace(String spaceId) {
        // Default method body
    }

    public Map<String, String> getSpaceProperties(String spaceId) {
        // Default method body
        return null;
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
