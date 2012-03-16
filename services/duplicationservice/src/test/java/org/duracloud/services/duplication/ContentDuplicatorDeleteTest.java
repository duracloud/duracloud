/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.duplication.error.DuplicationException;
import org.duracloud.services.duplication.impl.ContentDuplicatorImpl;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public class ContentDuplicatorDeleteTest {

    private ContentDuplicator duplicator;
    private SpaceDuplicator spaceDuplicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private final int waitMillis = 1;

    private String spaceId = "space-id";
    private String contentId = "content-id";


    @After
    public void tearDown() throws Exception {
        EasyMock.verify(fromStore, toStore, spaceDuplicator);
    }

    private void init(Mode cmd) throws ContentStoreException {
        fromStore = createMockFromStore(cmd);
        toStore = createMockToStore(cmd);
        spaceDuplicator = EasyMock.createMock("SpaceDuplicator",
                                              SpaceDuplicator.class);
        EasyMock.replay(fromStore, toStore, spaceDuplicator);

        duplicator = new ContentDuplicatorImpl(fromStore,
                                               toStore,
                                               spaceDuplicator,
                                               waitMillis);
    }

    @Test
    public void testDeleteContent() throws Exception {
        init(Mode.OK);
        duplicator.deleteContent(spaceId, contentId);
    }

    @Test
    public void testDeleteContentException() throws Exception {
        init(Mode.EXCEPTION);
        try {
            duplicator.deleteContent(spaceId, contentId);
            Assert.fail("exception expected");
            
        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    private ContentStore createMockFromStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("FromStore",
                                                 ContentStore.class);

        EasyMock.expect(store.getStorageProviderType()).andReturn("f-type");
        return store;
    }

    private ContentStore createMockToStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ToStore", ContentStore.class);

        EasyMock.expect(store.getStorageProviderType())
            .andReturn("t-type")
            .anyTimes();

        mockSetDeleteContentExpectation(cmd, store);
        return store;
    }

    private void mockSetDeleteContentExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case OK:
                store.deleteContent(spaceId, contentId);
                EasyMock.expectLastCall();
                break;
            case EXCEPTION:
                int retryTimes = 4;
                store.deleteContent(spaceId, contentId);
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception")).times(retryTimes);
        }
    }

    private enum Mode {
        OK, EXCEPTION;
    }

}
