/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public class ContentDuplicatorCreateTest {

    private ContentDuplicator replicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private String spaceId = "space-id";
    private String contentId = "content-id";

    private String text = "hello";
    private Content content;
    private InputStream contentStream;
    private String checksum;
    private String mimeGood = "text/plain";
    private String mimeOther = "application/octet-stream";


    @Before
    public void setUp() throws Exception {
        contentStream = getStream(text);
        checksum = getChecksum(text);
    }

    private InputStream getStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

    private String getChecksum(String text) throws IOException {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        return util.generateChecksum(getStream(text));
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
    public void testReplicateContent() throws Exception {
        init(Mode.OK);
        replicator.createContent(spaceId, contentId);
    }

    @Test
    public void testReplicateContentNoMetadata() throws Exception {
        init(Mode.EMPTY_METADATA);
        replicator.createContent(spaceId, contentId);
    }

    @Test
    public void testReplicateContentNullMetadata() throws Exception {
        init(Mode.NULL_METADATA);
        replicator.createContent(spaceId, contentId);
    }

    @Test
    public void testReplicateContentNullContent() throws Exception {
        init(Mode.NULL_CONTENT);
        replicator.createContent(spaceId, contentId);
    }

    @Test
    public void testReplicateContentNullInput() throws Exception {
        init(Mode.NULL_INPUT);
        replicator.createContent(null, null);
    }

    @Test
    public void testReplicateContentAddException() throws Exception {
        init(Mode.ADD_EXCEPTION);
        replicator.createContent(spaceId, contentId);
    }

    private ContentStore createMockFromStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("FromStore",
                                                 ContentStore.class);

        EasyMock.expect(store.getStorageProviderType()).andReturn("f-type");

        mockGetContentExpectation(cmd, store);
        mockGetSpaceMetadataExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockGetContentExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {

        int times = cmd == Mode.ADD_EXCEPTION ? 2 : 1;
        switch (cmd) {
            case NULL_INPUT:
                break;

            case NULL_CONTENT:
                EasyMock.expect(store.getContent(spaceId, contentId)).andReturn(
                    null);
                break;

            default:
                content = createMockContent(cmd);
                EasyMock.expect(store.getContent(spaceId, contentId)).andReturn(
                    content).times(times);
        }
    }

    private void mockGetSpaceMetadataExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NOT_FOUND_EXCEPTION:
                EasyMock.expect(store.getSpaceMetadata(spaceId))
                    .andReturn(null);
                break;
        }
    }

    private Content createMockContent(Mode cmd) {
        Content content = EasyMock.createMock("Content", Content.class);

        int times = cmd == Mode.ADD_EXCEPTION ? 2 : 1;
        EasyMock.expect(content.getStream()).andReturn(contentStream).times(times);
        EasyMock.expect(content.getMetadata()).andReturn(createContentMetadata(
            cmd));

        EasyMock.replay(content);
        return content;
    }

    private Map<String, String> createContentMetadata(Mode cmd) {
        Map<String, String> metadata = new HashMap<String, String>();

        switch (cmd) {

            case EMPTY_METADATA:
                break;

            case NULL_METADATA:
                metadata = null;
                break;

            case NULL_CONTENT:
                break;

            default:
                metadata.put(ContentStore.CONTENT_MIMETYPE, mimeGood);
                metadata.put(ContentStore.CONTENT_SIZE, text.length() + "");
                metadata.put(ContentStore.CONTENT_CHECKSUM, checksum);
                break;
        }

        return metadata;
    }

    private ContentStore createMockToStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ToStore", ContentStore.class);
        EasyMock.expect(store.getStorageProviderType()).andReturn("t-type");

        mockAddContentExpectation(cmd, store);
        mockCreateSpaceExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockAddContentExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        String mimeType = mimeGood;
        switch (cmd) {
            case NULL_INPUT:
                // fall-through
            case NULL_CONTENT:
                break;

            case ADD_EXCEPTION:
                EasyMock.expect(store.addContent(spaceId,
                                                 contentId,
                                                 contentStream,
                                                 text.length(),
                                                 mimeType,
                                                 checksum,
                                                 createContentMetadata(cmd)))
                    .andThrow(new ContentStoreException("test-exception"));
                EasyMock.expect(store.addContent(spaceId,
                                                 contentId,
                                                 contentStream,
                                                 text.length(),
                                                 mimeType,
                                                 checksum,
                                                 createContentMetadata(cmd)))
                    .andReturn(checksum);
                break;

            case EMPTY_METADATA:
                // fall-through
            case NULL_METADATA:
                mimeType = mimeOther;
                // fall-through
            default:
                EasyMock.expect(store.addContent(spaceId,
                                                 contentId,
                                                 contentStream,
                                                 text.length(),
                                                 mimeType,
                                                 checksum,
                                                 createContentMetadata(cmd)))
                    .andReturn(checksum);
        }
    }

    private void mockCreateSpaceExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NOT_FOUND_EXCEPTION:
                store.createSpace(spaceId, null);
                EasyMock.expectLastCall();
        }
    }

    private enum Mode {
        OK, EMPTY_METADATA, NULL_METADATA, NULL_CONTENT, NULL_INPUT,
        ADD_EXCEPTION, NOT_FOUND_EXCEPTION;
    }

}
