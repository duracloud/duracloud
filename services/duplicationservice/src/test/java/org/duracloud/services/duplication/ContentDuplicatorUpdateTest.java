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
import org.duracloud.services.duplication.error.DuplicationException;
import org.duracloud.services.duplication.impl.ContentDuplicatorImpl;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
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
    private SpaceDuplicator spaceDuplicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private final int waitMillis = 1;

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

        EasyMock.verify(fromStore, toStore, spaceDuplicator);

        if (null != content) {
            EasyMock.verify(content);
        }
    }

    private void init(Mode cmd) throws ContentStoreException {
        fromStore = createMockFromStore(cmd);
        toStore = createMockToStore(cmd);
        spaceDuplicator = EasyMock.createMock("SpaceDuplicator",
                                              SpaceDuplicator.class);
        EasyMock.replay(fromStore, toStore, spaceDuplicator);

        replicator = new ContentDuplicatorImpl(fromStore,
                                               toStore,
                                               spaceDuplicator,
                                               waitMillis);
    }

    @Test
    public void testUpdateContent() throws Exception {
        init(Mode.OK);
        replicator.updateContent(spaceId, contentId);
    }

    @Test
    public void testUpdateContentGetPropertiesException() throws Exception {
        init(Mode.GET_PROPERTIES_EXCEPTION);
        try {
            replicator.updateContent(spaceId, contentId);
            Assert.fail("exception expected");
            
        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUpdateContentSetPropertiesException() throws Exception {
        init(Mode.SET_PROPERTIES_EXCEPTION);
        try {
            replicator.updateContent(spaceId, contentId);
            Assert.fail("exception expected");
            
        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUpdateContentNotFound() throws Exception {
        init(Mode.NOT_FOUND);
        try {
            replicator.updateContent(spaceId, contentId);
            Assert.fail("exception expected");

        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
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
        EasyMock.expect(store.getStorageProviderType())
            .andReturn("f-type").anyTimes();

        mockGetContentPropertiesExpectation(cmd, store);
        mockGetContentExpectation(cmd, store);
        return store;
    }

    private void mockGetContentPropertiesExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NULL_INPUT:
                break;

            case GET_PROPERTIES_EXCEPTION:
                EasyMock.expect(store.getContentProperties(spaceId, contentId))
                    .andThrow(new ContentStoreException("test-exception"))
                    .times(4);
                break;

            default:
                EasyMock.expect(store.getContentProperties(spaceId, contentId))
                    .andReturn(createContentProperties(cmd));
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
        EasyMock.expect(content.getProperties())
            .andReturn(createContentProperties(cmd));

        EasyMock.replay(content);
        return content;
    }

    private Map<String, String> createContentProperties(Mode cmd) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ContentStore.CONTENT_MIMETYPE, mimeGood);
        properties.put(ContentStore.CONTENT_SIZE, text.length() + "");
        properties.put(ContentStore.CONTENT_CHECKSUM, checksum);

        return properties;
    }

    private ContentStore createMockToStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ToStore", ContentStore.class);

        int times;
        switch (cmd) {
            case NOT_FOUND:
                times = 3;
                break;
            case SET_PROPERTIES_EXCEPTION:
                times = 2;
                break;
            default:
                times = 1;
        }
        
        EasyMock.expect(store.getStorageProviderType())
            .andReturn("t-type")
            .times(times);

        mockSetContentPropertiesExpectation(cmd, store);
        mockAddContentExpectation(cmd, store);
        return store;
    }

    private void mockSetContentPropertiesExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {

        switch (cmd) {
            case NULL_INPUT:
                break;

            case NOT_FOUND:
                store.setContentProperties(spaceId,
                                         contentId,
                                         createContentProperties(cmd));
                EasyMock.expectLastCall().andThrow(new NotFoundException(
                    "test-exception"));
                break;

            case SET_PROPERTIES_EXCEPTION:
                int numRetries = 5;
                store.setContentProperties(spaceId,
                                         contentId,
                                         createContentProperties(cmd));
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception")).times(numRetries);
                break;

            case GET_PROPERTIES_EXCEPTION:
                break;

            default:
                store.setContentProperties(spaceId,
                                         contentId,
                                         createContentProperties(cmd));
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
                                                 createContentProperties(cmd)))
                    .andReturn(checksum);
                break;
        }
    }

    private enum Mode {
        OK, GET_PROPERTIES_EXCEPTION, NOT_FOUND, SET_PROPERTIES_EXCEPTION,
        NULL_INPUT;
    }

}
