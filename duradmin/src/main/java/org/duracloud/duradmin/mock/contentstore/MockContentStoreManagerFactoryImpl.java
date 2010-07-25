/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.mock.contentstore;

import org.apache.commons.codec.binary.Hex;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.duracloud.duradmin.contentstore.ContentStoreManagerFactory;
import org.duracloud.domain.Content;
import org.duracloud.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.InvalidIdException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MockContentStoreManagerFactoryImpl
        implements ContentStoreManagerFactory {

    private ContentStoreManager contentStoreManager;

    public ContentStoreManager create() throws Exception {
        if (this.contentStoreManager == null) {
            this.contentStoreManager = new MockContentStoreManager();
        }
        return this.contentStoreManager;
    }

    private class MockContentStoreManager
            implements ContentStoreManager {

        private ContentStore primaryContentStore;

        private Map<String, ContentStore> contentStores =
                new HashMap<String, ContentStore>();

        public MockContentStoreManager() {
            for (int i = 0; i < 3; i++) {
                String storeId = "Mock Store #" + i;
                this.contentStores.put(storeId, new MockContentStore(storeId));
            }

            this.primaryContentStore =
                    this.contentStores.get(this.contentStores.keySet()
                            .iterator().next());
        }

        public void reinitialize(String host, String port, String context)
            throws ContentStoreException {
        }

        public ContentStore getContentStore(String storeID)
                throws ContentStoreException {
            return this.contentStores.get(storeID);
        }

        public Map<String, ContentStore> getContentStores()
                throws ContentStoreException {
            // TODO Auto-generated method stub
            return this.contentStores;
        }

        public ContentStore getPrimaryContentStore()
                throws ContentStoreException {
            return this.primaryContentStore;
        }

        public ContentStore getPrimaryContentStoreAsAnonymous()
                throws ContentStoreException {
            return this.primaryContentStore;
        }

        public void login(Credential appCred) {
        }

        public void logout() {
        }

    }

    private class MockContentStore
            implements ContentStore {

        Map<String, Space> spaceMap = new HashMap<String, Space>();

        Map<String, Content> contentMap = new HashMap<String, Content>();

        private String storeId;

        private String storageProviderType;

        MockContentStore(String storeId) {
            this.storeId = storeId;
            this.storageProviderType =
                    "Mock Storage Provider [" + storeId + "]";

            for (int i = 0; i < 10; i++) {
                Space s = new Space();
                s.setId("space-number-" + i);
                Map<String, String> metadata = new HashMap<String, String>();
                metadata.put(ContentStore.SPACE_COUNT, String.valueOf(i % 4));
                metadata.put(ContentStore.SPACE_CREATED, new Date().toString());
                metadata.put(ContentStore.SPACE_ACCESS, AccessType.OPEN.name());
                s.setMetadata(metadata);

                List<String> contentIds = new ArrayList<String>();
                for (int j = 0; j < 20; j++) {
                    String id = "Item-" + j;
                    contentIds.add(id);
                    Content content = new Content();
                    content.addMetadata(ContentStore.CONTENT_CHECKSUM, Hex
                            .encodeHex("2dflksjff2342sdfsdf".getBytes())
                            .toString());
                    content.addMetadata(ContentStore.CONTENT_MODIFIED,
                                        new Date().toGMTString());
                    content.addMetadata(ContentStore.CONTENT_SIZE, String
                            .valueOf(j + 10000));
                    content.addMetadata(ContentStore.CONTENT_MIMETYPE,
                                        "image/jpeg");
                    contentMap.put(getContentId(s.getId(), id), content);

                }

                s.setContentIds(contentIds);

                spaceMap.put(s.getId(), s);
            }

        }

        public String addContent(String spaceId,
                                 String contentId,
                                 InputStream content,
                                 long contentSize,
                                 String contentMimeType,
                                 String contentChecksum,
                                 Map<String, String> contentMetadata)
                throws ContentStoreException {

            Space space = spaceMap.get(spaceId);
            if (space == null) {
                throw new ContentStoreException("space " + spaceId
                        + " not found.");
            }

            if (!space.getContentIds().contains(contentId)) {
                space.getContentIds().add(contentId);
                Content c = new Content();
                c.setId(contentId);
                c.setMetadata(contentMetadata);
                c.setStream(content);
            }

            return contentId;
        }

        public void createSpace(String spaceId,
                                Map<String, String> spaceMetadata)
                throws ContentStoreException {

            Space space = new Space();
            space.setId(spaceId);
            if (spaceMetadata == null) {
                spaceMetadata = new HashMap<String, String>();
                spaceMetadata.put(ContentStore.SPACE_COUNT, String.valueOf(0));
                spaceMetadata.put(ContentStore.SPACE_CREATED, new Date()
                        .toString());
                spaceMetadata.put(ContentStore.SPACE_ACCESS, AccessType.OPEN
                        .name());
            }

            space.setMetadata(spaceMetadata);
            spaceMap.put(spaceId, space);
        }

        private Content getContentInternal(String spaceId, String contentId)
                throws ContentStoreException {
            try {
                return contentMap.get(getContentId(spaceId, contentId));
            } catch (Exception ex) {
                throw new ContentStoreException(ex);
            }
        }

        private String getContentId(String spaceId, String contentId) {
            return spaceId + "&" + contentId;
        }

        public void deleteContent(String spaceId, String contentId)
                throws ContentStoreException {
            try {
                Space space = spaceMap.get(spaceId);
                space.getContentIds().remove(contentId);
                contentMap.remove(getContentId(spaceId, contentId));
            } catch (Exception ex) {
                throw new ContentStoreException(ex);
            }

        }

        public void deleteSpace(String spaceId) throws ContentStoreException {
            spaceMap.remove(spaceId);
        }

        public String getBaseURL() {
            // TODO Auto-generated method stub
            return "/baseUrl";
        }

        public Content getContent(String spaceId, String contentId)
                throws ContentStoreException {
            return getContentInternal(spaceId, contentId);
        }

        public Map<String, String> getContentMetadata(String spaceId,
                                                      String contentId)
                throws ContentStoreException {
            return getContentInternal(spaceId, contentId).getMetadata();
        }

        public Iterator<String> getSpaceContents(String spaceId)
            throws ContentStoreException {
            return null;
        }

        public Iterator<String> getSpaceContents(String spaceId, String prefix)
            throws ContentStoreException {
            return null;
        }

        public Space getSpace(String spaceId,
                              String contentIdFilter,
                              long maxResultSize,
                              String marker)
                throws ContentStoreException {
            Space space = spaceMap.get(spaceId);
            return space;
        }

        public AccessType getSpaceAccess(String spaceId)
                throws ContentStoreException {
            String access =
                    spaceMap.get(spaceId).getMetadata()
                            .get(ContentStore.SPACE_ACCESS);
            return AccessType.valueOf(access);
        }

        public Map<String, String> getSpaceMetadata(String spaceId)
                throws ContentStoreException {
            try {
                return spaceMap.get(spaceId).getMetadata();
            } catch (Exception ex) {
                throw new ContentStoreException(ex);
            }
        }

        public List<String> getSpaces() throws ContentStoreException {
            return new ArrayList<String>(spaceMap.keySet());
        }

        public String getStorageProviderType() {
            return this.storageProviderType;
        }

        public String getStoreId() {
            return this.storeId;
        }

        public void setContentMetadata(String spaceId,
                                       String contentId,
                                       Map<String, String> contentMetadata)
                throws ContentStoreException {

        }

        public void setSpaceAccess(String spaceId, AccessType spaceAccess)
                throws ContentStoreException {
            // TODO Auto-generated method stub

        }

        public void setSpaceMetadata(String spaceId,
                                     Map<String, String> spaceMetadata)
                throws ContentStoreException {
            // TODO Auto-generated method stub

        }

        public void validateSpaceId(String spaceId) throws InvalidIdException {
            // TODO Auto-generated method stub
        }

        public void validateContentId(String contentId) throws InvalidIdException {
            // TODO Auto-generated method stub
        }

        public List<String> getSupportedTasks()
            throws ContentStoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public String performTask(String taskName, String taskParameters)
            throws ContentStoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public String getTaskStatus(String taskName)
            throws ContentStoreException {
            // TODO Auto-generated method stub
            return null;
        }
    }

}
