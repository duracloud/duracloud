/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public class ContentDuplicatorUpdateTest {

    private ContentDuplicator replicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private String spaceId = "space-id";
    private String contentId = "content-id";

    private String text = "hello";
    private Content content;
    private InputStream contentStream;
    private String checksum = "123abc";
    private String mimeGood = "text/plain";


    @Before
    public void setUp() throws Exception {
        contentStream = new ByteArrayInputStream(text.getBytes());
    }

    @After
    public void tearDown() throws Exception {
        if (null != contentStream) {
            contentStream.close();
        }

        EasyMock.verify(fromStore);
        EasyMock.verify(toStore);

        if (null != content) {
            EasyMock.verify(content);
        }
    }

    private void init(Mode cmd) throws ContentStoreException {
        fromStore = createMockFromStore(cmd);
        toStore = createMockToStore(cmd);

        replicator = new ContentDuplicator(fromStore, toStore);
    }

    @Test
    public void testUpdateContent() throws Exception {
        init(Mode.OK);
        replicator.updateContent(spaceId, contentId);
    }

    @Test
    public void testUpdateContentGetMetadataException() throws Exception {
        init(Mode.GET_METADATA_EXCEPTION);
        replicator.updateContent(spaceId, contentId);
    }

    @Test
    public void testUpdateContentSetMetadataException() throws Exception {
        init(Mode.SET_METADATA_EXCEPTION);
        replicator.updateContent(spaceId, contentId);
    }

    @Test
    public void testUpdateContentNotFound() throws Exception {
        init(Mode.NOT_FOUND);
        replicator.updateContent(spaceId, contentId);
    }

    @Test
    public void testUpdateContentNullInput() throws Exception {
        init(Mode.NULL_INPUT);
        replicator.updateContent(null, null);
    }

    private ContentStore createMockFromStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("FromStore",
                                                 ContentStore.class);
        EasyMock.expect(store.getStorageProviderType()).andReturn("f-type").anyTimes();

        mockGetContentMetadataExpectation(cmd, store);
        mockGetContentExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockGetContentMetadataExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NULL_INPUT:
                break;

            case GET_METADATA_EXCEPTION:
                EasyMock.expect(store.getContentMetadata(spaceId, contentId))
                    .andThrow(new ContentStoreException("test-exception"));
                break;

            default:
                EasyMock.expect(store.getContentMetadata(spaceId, contentId))
                    .andReturn(createContentMetadata(cmd));
                break;
        }
    }

    private void mockGetContentExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NOT_FOUND:
                content = createMockContent(cmd);
                EasyMock.expect(store.getContent(spaceId, contentId)).andReturn(
                    content);
                break;
        }
    }

    private Content createMockContent(Mode cmd) {
        Content content = EasyMock.createMock("Content", Content.class);

        EasyMock.expect(content.getStream()).andReturn(contentStream);
        EasyMock.expect(content.getMetadata()).andReturn(createContentMetadata(
            cmd));

        EasyMock.replay(content);
        return content;
    }

    private Map<String, String> createContentMetadata(Mode cmd) {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(ContentStore.CONTENT_MIMETYPE, mimeGood);
        metadata.put(ContentStore.CONTENT_SIZE, text.length() + "");
        metadata.put(ContentStore.CONTENT_CHECKSUM, checksum);

        return metadata;
    }

    private ContentStore createMockToStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ToStore", ContentStore.class);

        int times = cmd == Mode.NOT_FOUND ? 2 : 1;
        EasyMock.expect(store.getStorageProviderType())
            .andReturn("t-type")
            .times(times);

        mockSetContentMetadataExpectation(cmd, store);
        mockAddContentExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockSetContentMetadataExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {

        switch (cmd) {
            case NULL_INPUT:
                break;

            case NOT_FOUND:
                store.setContentMetadata(spaceId,
                                         contentId,
                                         createContentMetadata(cmd));
                EasyMock.expectLastCall().andThrow(new NotFoundException(
                    "test-exception"));
                break;

            case SET_METADATA_EXCEPTION:
                int numRetries = 4;
                store.setContentMetadata(spaceId,
                                         contentId,
                                         createContentMetadata(cmd));
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception")).times(numRetries);
                break;

            case GET_METADATA_EXCEPTION:
                break;

            default:
                store.setContentMetadata(spaceId,
                                         contentId,
                                         createContentMetadata(cmd));
                EasyMock.expectLastCall();
                break;
        }
    }

    private void mockAddContentExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NOT_FOUND:
                EasyMock.expect(store.addContent(spaceId,
                                                 contentId,
                                                 contentStream,
                                                 text.length(),
                                                 mimeGood,
                                                 checksum,
                                                 createContentMetadata(cmd)))
                    .andReturn(checksum);
                break;
        }
    }

    private enum Mode {
        OK, GET_METADATA_EXCEPTION, NOT_FOUND, SET_METADATA_EXCEPTION,
        NULL_INPUT;
    }

}
