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
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public class ContentDuplicatorDeleteTest {

    private ContentDuplicator replicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private String spaceId = "space-id";
    private String contentId = "content-id";


    @After
    public void tearDown() throws Exception {
        EasyMock.verify(fromStore);
        EasyMock.verify(toStore);
    }

    private void init(Mode cmd) throws ContentStoreException {
        fromStore = createMockFromStore(cmd);
        toStore = createMockToStore(cmd);

        replicator = new ContentDuplicator(fromStore, toStore);
    }

    @Test
    public void testDeleteContent() throws Exception {
        init(Mode.OK);
        replicator.deleteContent(spaceId, contentId);
    }

    @Test
    public void testDeleteContentException() throws Exception {
        init(Mode.EXCEPTION);
        replicator.deleteContent(spaceId, contentId);
    }

    private ContentStore createMockFromStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("FromStore",
                                                 ContentStore.class);

        EasyMock.expect(store.getStorageProviderType()).andReturn("f-type");

        EasyMock.replay(store);
        return store;
    }

    private ContentStore createMockToStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ToStore", ContentStore.class);

        EasyMock.expect(store.getStorageProviderType()).andReturn("t-type");

        mockSetDeleteContentExpectation(cmd, store);

        EasyMock.replay(store);
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
