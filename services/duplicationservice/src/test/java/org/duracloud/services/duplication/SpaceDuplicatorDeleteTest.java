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

public class SpaceDuplicatorDeleteTest {

    private SpaceDuplicator replicator;

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

        replicator = new SpaceDuplicator(fromStore, toStore);
    }

    @Test
    public void testDeleteSpace() throws Exception {
        init(Mode.OK);
        replicator.deleteSpace(spaceId);
    }

    @Test
    public void testDeleteSpaceNullInput() throws Exception {
        init(Mode.NULL_INPUT);
        replicator.deleteSpace(null);
    }

    @Test
    public void testDeleteSpaceException() throws Exception {
        init(Mode.EXCEPTION);
        replicator.deleteSpace(spaceId);
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

        mockSetDeleteSpaceExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockSetDeleteSpaceExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case OK:
                store.deleteSpace(spaceId);
                EasyMock.expectLastCall();
                break;
            case EXCEPTION:
                int retryTimes = 1;
                store.deleteSpace(spaceId);
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception")).times(retryTimes);
        }
    }

    private enum Mode {
        OK, NULL_INPUT, EXCEPTION;
    }

}
