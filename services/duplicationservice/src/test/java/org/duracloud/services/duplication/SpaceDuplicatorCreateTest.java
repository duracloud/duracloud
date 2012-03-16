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
import org.duracloud.services.duplication.impl.SpaceDuplicatorImpl;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public class SpaceDuplicatorCreateTest {

    private SpaceDuplicator replicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private String spaceId = "space-id";


    @After
    public void tearDown() throws Exception {
        EasyMock.verify(fromStore);
        EasyMock.verify(toStore);
    }

    private void init(Mode cmd) throws ContentStoreException {
        fromStore = createMockFromStore(cmd);
        toStore = createMockToStore(cmd);

        replicator = new SpaceDuplicatorImpl(fromStore, toStore);
    }

    @Test
    public void testCreateSpace() throws Exception {
        init(Mode.OK);
        replicator.createSpace(spaceId);
    }

    @Test
    public void testCreateSpaceNullInput() throws Exception {
        init(Mode.NULL_INPUT);
        replicator.createSpace(null);
    }

//    @Test
//    public void testCreateSpacePropertiesException() throws Exception {
//        init(Mode.PROPERTIES_EXCEPTION);
//        replicator.createSpace(spaceId);
//    }

    @Test
    public void testCreateSpaceCreateException() throws Exception {
        init(Mode.CREATE_EXCEPTION);
        replicator.createSpace(spaceId);
    }

    private ContentStore createMockFromStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("FromStore",
                                                 ContentStore.class);
        EasyMock.expect(store.getStorageProviderType()).andReturn("f-type");

        mockGetSpacePropertiesExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockGetSpacePropertiesExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case CREATE_EXCEPTION:
                // fall-through
            case OK:
                EasyMock.expect(store.getSpaceProperties(spaceId)).andReturn(
                    createContentProperties(cmd));
                break;
        }
    }

    private Map<String, String> createContentProperties(Mode cmd) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ContentStore.SPACE_COUNT, "10");

        return properties;
    }

    private ContentStore createMockToStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ToStore", ContentStore.class);
        EasyMock.expect(store.getStorageProviderType()).andReturn("t-type");

        mockCreateSpaceExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockCreateSpaceExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case OK:
                store.createSpace(spaceId, createContentProperties(cmd));
                EasyMock.expectLastCall();
                break;
            case CREATE_EXCEPTION:
                store.createSpace(spaceId, createContentProperties(cmd));
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception")).times(2);
                
                store.createSpace(spaceId, createContentProperties(cmd));
                EasyMock.expectLastCall();
                break;
        }
    }

    private enum Mode {
        OK, NULL_INPUT, CREATE_EXCEPTION;
    }
}
