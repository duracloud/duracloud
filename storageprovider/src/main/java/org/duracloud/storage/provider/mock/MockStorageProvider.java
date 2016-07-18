/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider.mock;

import org.duracloud.common.model.AclType;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MockStorageProvider
        implements StorageProvider {

    private String spaceId;

    private String contentId;

    private String contentMimeType;

    private Map<String, String> userProperties;

    private long contentSize;

    private String contentChecksum;

    private InputStream content;

    private Map<String, String> contentProperties;

    private List<String> spaceContents;

    private Map<String, String> spaceProperties;

    private Iterator<String> spaces;

    public MockStorageProvider() {
        spaceProperties = new HashMap<>();
    }

    @Override
    public StorageProviderType getStorageProviderType() {
        return StorageProviderType.UNKNOWN;
    }

    public String addContent(String spaceId,
                             String contentId,
                             String contentMimeType,
                             Map<String, String> userProperties,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) throws StorageException {
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.contentMimeType = contentMimeType;
        this.userProperties = userProperties;
        this.contentSize = contentSize;
        this.contentChecksum = contentChecksum;
        this.content = content;
        this.spaceContents = new ArrayList<String>();
        spaceContents.add(content.toString());
        return new String();
    }

    @Override
    public String copyContent(String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) {
        return null;
    }

    public void createSpace(String spaceId) throws StorageException {
        this.spaceId = spaceId;
        List<String> spacesList = new ArrayList<String>();
        spacesList.add(spaceId);
        spaces = spacesList.iterator();
    }

    public void deleteContent(String spaceId, String contentId)
            throws StorageException {
        this.spaceId = spaceId;
        this.contentId = contentId;
    }

    public void deleteSpace(String spaceId) throws StorageException {
        this.spaceId = spaceId;
    }

    public InputStream getContent(String spaceId, String contentId)
            throws StorageException {
        return content;
    }

    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId)
            throws StorageException {
        return contentProperties;
    }

    public Iterator<String> getSpaceContents(String spaceId, String prefix)
            throws StorageException {
        return spaceContents.iterator();
    }

    public List<String> getSpaceContentsChunked(String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker)
        throws StorageException {
        return spaceContents;
    }

    public Map<String, String> getSpaceProperties(String spaceId)
    throws StorageException {
        return spaceProperties;
    }

    public Iterator<String> getSpaces() throws StorageException {
        return spaces;
    }

    public void setContentProperties(String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties)
            throws StorageException {
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.contentProperties = contentProperties;
    }

    @Override
    public Map<String, AclType> getSpaceACLs(String spaceId) {
        throw new UnsupportedOperationException("getSpaceACLs not implemented");
    }

    @Override
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs) {
        throw new UnsupportedOperationException("setSpaceACLs not implemented");
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getContentMimeType() {
        return contentMimeType;
    }

    public void setContentMimeType(String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public Map<String, String> getContentProperties() {
        return contentProperties;
    }

    public void setContentProperties(Map<String, String> contentProperties) {
        this.contentProperties = contentProperties;
    }

    public Map<String, String> getSpaceProperties() {
        return spaceProperties;
    }

    public void setSpaces(Iterator<String> spaces) {
        this.spaces = spaces;
    }

}
