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
import org.duracloud.storage.provider.mock.MockStorageProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BrokeredStorageProviderTest {

    private BrokeredStorageProvider broker;

    private MockStorageProvider targetProvider;

    private MockStorageProvider directProvider;

    private StatelessStorageProvider dispatchProvider;

    private final String spaceId = "spaceId";

    private final String contentId = "contentId";

    private final String contentMimeType = "contentMimeType";

    private final long contentSize = 12L;

    private InputStream content;

    private final AccessType access = AccessType.OPEN;

    private final Map<String, String> spaceMetadata = new HashMap<String, String>();

    private final Map<String, String> contentMetadata = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {

        targetProvider = new MockStorageProvider();
        directProvider = new MockStorageProvider();
        dispatchProvider = new StatelessStorageProviderImpl();

        broker = new BrokeredStorageProvider(dispatchProvider, targetProvider, "0");

        content = new ByteArrayInputStream("".getBytes());
    }

    @After
    public void tearDown() throws Exception {
        broker = null;
        dispatchProvider = null;
        targetProvider = null;
        directProvider = null;

        content.close();
        content = null;
    }

    @Test
    public void testAddContent() throws StorageException {
        directProvider.addContent(spaceId,
                                  contentId,
                                  contentMimeType,
                                  contentSize,
                                  null,
                                  content);
        broker.addContent(spaceId,
                          contentId,
                          contentMimeType,
                          contentSize,
                          null,
                          content);

        verifySpaceId();
        verifyContentId();
        verifyContentMimeType();
        verifyContentSize();
        verifyContent();
    }

    @Test
    public void testCreateSpace() throws StorageException {
        directProvider.createSpace(spaceId);
        broker.createSpace(spaceId);

        verifySpaceId();
    }

    @Test
    public void testDeleteContent() throws StorageException {
        directProvider.deleteContent(spaceId, contentId);
        broker.deleteContent(spaceId, contentId);

        verifySpaceId();
        verifyContentId();
    }

    @Test
    public void deleteSpace() throws StorageException {
        directProvider.deleteSpace(spaceId);
        broker.deleteSpace(spaceId);

        verifySpaceId();
    }

    @Test
    public void getContent() throws StorageException {
        directProvider.setContent(content);
        targetProvider.setContent(content);

        InputStream content0 = directProvider.getContent(spaceId, contentId);
        InputStream content1 = broker.getContent(spaceId, contentId);

        verify(content0, content1);
    }

    @Test
    public void getContentMetadata() throws StorageException {
        directProvider.setContentMetadata(spaceId, contentId, contentMetadata);
        broker.setContentMetadata(spaceId, contentId, contentMetadata);

        Map<String, String> meta0 =
                directProvider.getContentMetadata(spaceId, contentId);
        Map<String, String> meta1 = broker.getContentMetadata(spaceId, contentId);

        verify(meta0, meta1);
    }

    @Test
    public void getSpaceAccess() throws StorageException {
        directProvider.setSpaceAccess(spaceId, access);
        broker.setSpaceAccess(spaceId, access);

        AccessType access0 = directProvider.getSpaceAccess(spaceId);
        AccessType access1 = broker.getSpaceAccess(spaceId);

        verify(access0, access1);
    }

    @Test
    public void getSpaceContents() throws StorageException {
        directProvider.addContent(spaceId, contentId, contentMimeType, contentSize, null, content);
        broker.addContent(spaceId, contentId, contentMimeType, contentSize, null, content);
        Iterator<String> spaceContents0 = directProvider.getSpaceContents(spaceId, null);
        Iterator<String> spaceContents1 = broker.getSpaceContents(spaceId, null);

        verifyIteratorContents(spaceContents0, spaceContents1);
    }

    @Test
    public void getSpaceContentsChunked() throws StorageException {
        directProvider.addContent(spaceId, contentId, contentMimeType, contentSize, null, content);
        broker.addContent(spaceId, contentId, contentMimeType, contentSize, null, content);
        List<String> spaceContents0 =
            directProvider.getSpaceContentsChunked(spaceId, null, 0, null);
        List<String> spaceContents1 =
            broker.getSpaceContentsChunked(spaceId, null, 0, null);

        verifyIteratorContents(spaceContents0.iterator(),
                               spaceContents1.iterator());
    }

    @Test
    public void getSpaceMetadata() throws StorageException {
        directProvider.setSpaceMetadata(spaceId, spaceMetadata);
        broker.setSpaceMetadata(spaceId, spaceMetadata);

        Map<String, String> meta0 = directProvider.getSpaceMetadata(spaceId);
        Map<String, String> meta1 = broker.getSpaceMetadata(spaceId);

        verify(meta0, meta1);
    }

    @Test
    public void getSpaces() throws StorageException {
        directProvider.createSpace(spaceId);
        broker.createSpace(spaceId);

        Iterator<String> spaces0 = directProvider.getSpaces();
        Iterator<String> spaces1 = broker.getSpaces();

        verifyIteratorContents(spaces0, spaces1);
    }

    @Test
    public void setSpaceAccess() throws StorageException {
        directProvider.setSpaceAccess(spaceId, access);
        broker.setSpaceAccess(spaceId, access);

        verifySpaceId();
        verifyAccess();
    }

    @Test
    public void setSpaceMetadata() throws StorageException {
        directProvider.setSpaceMetadata(spaceId, spaceMetadata);
        broker.setSpaceMetadata(spaceId, spaceMetadata);

        verifySpaceId();
        verifySpaceMetadata();
    }

    private void verifySpaceId() {
        Assert.assertNotNull(directProvider.getSpaceId());
        Assert.assertEquals(directProvider.getSpaceId(), targetProvider
                .getSpaceId());
    }

    private void verifyContentId() {
        Assert.assertNotNull(directProvider.getContentId());
        Assert.assertEquals(directProvider.getContentId(), targetProvider
                .getContentId());
    }

    private void verifyContentMimeType() {

        Assert.assertNotNull(directProvider.getContentMimeType());
        Assert.assertEquals(directProvider.getContentMimeType(), targetProvider
                .getContentMimeType());
    }

    private void verifyContentSize() {
        Assert.assertNotNull(directProvider.getContentSize());
        Assert.assertEquals(directProvider.getContentSize(), targetProvider
                .getContentSize());
    }

    private void verifyContent() {
        Assert.assertNotNull(directProvider.getContent());
        Assert.assertEquals(directProvider.getContent(), targetProvider
                .getContent());
    }

    private void verifyAccess() {
        Assert.assertNotNull(directProvider.getAccess());
        Assert.assertEquals(directProvider.getAccess(), targetProvider
                .getAccess());
    }

    private void verifySpaceMetadata() {
        Assert.assertNotNull(directProvider.getSpaceMetadata());
        Assert.assertEquals(directProvider.getSpaceMetadata(), targetProvider
                .getSpaceMetadata());
    }

    private void verifyIteratorContents(Iterator<String> iter0, Iterator<String> iter1) {
        Assert.assertNotNull(iter0);
        Assert.assertNotNull(iter1);
        while(iter0.hasNext()) {
            Assert.assertEquals(iter0.next(), iter1.next());
        }
        Assert.assertFalse(iter1.hasNext());
    }

    private void verify(Object obj0, Object obj1) {
        Assert.assertNotNull(obj0);
        Assert.assertEquals(obj0, obj1);
    }

}
