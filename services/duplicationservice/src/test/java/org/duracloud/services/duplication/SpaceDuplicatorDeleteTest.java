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
import org.duracloud.services.duplication.impl.SpaceDuplicatorImpl;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public class SpaceDuplicatorDeleteTest {

    private SpaceDuplicator replicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private int waitMillis = 1;

    private String spaceId = "space-id";

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(fromStore, toStore);
    }

    private void init(Mode cmd) throws ContentStoreException {
        fromStore = createMockFromStore(cmd);
        toStore = createMockToStore(cmd);

        replicator = new SpaceDuplicatorImpl(fromStore, toStore, waitMillis);
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
        try {
            replicator.deleteSpace(spaceId);
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
                int retryTimes = 4;
                store.deleteSpace(spaceId);
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception")).times(retryTimes);
        }
    }

    private enum Mode {
        OK, NULL_INPUT, EXCEPTION;
    }

}
