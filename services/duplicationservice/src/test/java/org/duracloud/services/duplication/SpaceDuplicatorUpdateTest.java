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
import org.duracloud.error.NotFoundException;
import org.duracloud.services.duplication.error.DuplicationException;
import org.duracloud.services.duplication.impl.SpaceDuplicatorImpl;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public class SpaceDuplicatorUpdateTest {

    private SpaceDuplicator replicator;

    private ContentStore fromStore;
    private ContentStore toStore;

    private final int waitMillis = 1;

    private String spaceId = "space-id";


    @Before
    public void setUp() throws Exception {
    }

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
    public void testUpdateSpace() throws Exception {
        init(Mode.OK);
        replicator.updateSpace(spaceId);
    }

    @Test
    public void testNullInputUpdateSpace() throws Exception {
        init(Mode.NULL_INPUT);
        replicator.updateSpace(null);
    }

    @Test
    public void testSetPropertiesExceptionUpdateSpace() throws Exception {
        init(Mode.SET_PROPERTIES_EXCEPTION);
        replicator.updateSpace(spaceId);
    }

    @Test
    public void testNotFoundUpdateSpace() throws Exception {
        init(Mode.NOT_FOUND);
        try {
            replicator.updateSpace(spaceId);
            Assert.fail("exception expected");
            
        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNotFoundCreateExceptionUpdateSpace() throws Exception {
        init(Mode.NOT_FOUND_CREATE_EXCEPTION);
        try {
            replicator.updateSpace(spaceId);
            Assert.fail("exception expected");

        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCreateExceptionUpdateSpace() throws Exception {
        init(Mode.CREATE_EXCEPTION);
        try {
            replicator.updateSpace(spaceId);
            Assert.fail("exception expected");

        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetAccessExceptionUpdateSpace() throws Exception {
        init(Mode.GET_ACCESS_EXCEPTION);
        try {
            replicator.updateSpace(spaceId);
            Assert.fail("exception expected");

        } catch (DuplicationException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSetAccessExceptionUpdateSpace() throws Exception {
        init(Mode.SET_ACCESS_EXCEPTION);
        try {
            replicator.updateSpace(spaceId);
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

        mockGetSpacePropertiesExpectation(cmd, store);
        mockGetSpaceAccessExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockGetSpacePropertiesExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NULL_INPUT:
                break;
            default:
                EasyMock.expect(store.getSpaceProperties(spaceId)).andReturn(
                    createSpaceProperties(cmd));
                break;
        }
    }

    private void mockGetSpaceAccessExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NOT_FOUND_CREATE_EXCEPTION:
            case NOT_FOUND:
            case CREATE_EXCEPTION:
                break;
            case SET_PROPERTIES_EXCEPTION:
            case OK:
                EasyMock.expect(store.getSpaceAccess(spaceId)).andReturn(
                    createSpaceAccess(cmd));
                break;
            case SET_ACCESS_EXCEPTION:
            case GET_ACCESS_EXCEPTION:
                store.getSpaceAccess(spaceId);
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception")).times(4);
                break;
        }
    }

    private Map<String, String> createSpaceProperties(Mode cmd) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ContentStore.SPACE_COUNT, "10");

        return properties;
    }

    private ContentStore.AccessType createSpaceAccess(Mode cmd) {
        return ContentStore.AccessType.OPEN;
    }

    private ContentStore createMockToStore(Mode cmd)
        throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ToStore", ContentStore.class);
        EasyMock.expect(store.getStorageProviderType()).andReturn("t-type");

        mockSetSpacePropertiesExpectation(cmd, store);
        mockSetSpaceAccessExpectation(cmd, store);

        EasyMock.replay(store);
        return store;
    }

    private void mockSetSpacePropertiesExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case SET_ACCESS_EXCEPTION:
            case GET_ACCESS_EXCEPTION:
            case OK:
                store.setSpaceProperties(spaceId, createSpaceProperties(cmd));
                EasyMock.expectLastCall();              
                break;
            case SET_PROPERTIES_EXCEPTION:
                store.setSpaceProperties(spaceId, createSpaceProperties(cmd));
                EasyMock.expectLastCall().andThrow(new ContentStoreException(
                    "test-exception"));
                store.setSpaceProperties(spaceId, createSpaceProperties(cmd));
                EasyMock.expectLastCall();
                break;
            case CREATE_EXCEPTION:
            case NOT_FOUND_CREATE_EXCEPTION:
            case NOT_FOUND:
                store.setSpaceProperties(spaceId, createSpaceProperties(cmd));
                EasyMock.expectLastCall().andThrow(new NotFoundException(
                    "test-exception")).times(4);
                break;
        }
    }

    private void mockSetSpaceAccessExpectation(Mode cmd, ContentStore store)
        throws ContentStoreException {
        switch (cmd) {
            case NOT_FOUND_CREATE_EXCEPTION:
            case NOT_FOUND:
            case CREATE_EXCEPTION:
            case SET_ACCESS_EXCEPTION:
                break;
            case SET_PROPERTIES_EXCEPTION:
            case OK:
                store.setSpaceAccess(spaceId, createSpaceAccess(cmd));
                EasyMock.expectLastCall();
                break;
        }
    }

    private enum Mode {
        OK, NULL_INPUT, SET_PROPERTIES_EXCEPTION,
        NOT_FOUND, NOT_FOUND_CREATE_EXCEPTION, CREATE_EXCEPTION,
        GET_ACCESS_EXCEPTION, SET_ACCESS_EXCEPTION;
    }
}
