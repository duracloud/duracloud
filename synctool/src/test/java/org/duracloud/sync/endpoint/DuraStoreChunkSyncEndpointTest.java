/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.duracloud.chunk.manifest.ChunksManifest.chunkSuffix;
import static org.duracloud.chunk.manifest.ChunksManifest.manifestSuffix;

/**
 * @author Andrew Woods
 *         Date: 9/9/11
 */
public class DuraStoreChunkSyncEndpointTest {

    private DuraStoreChunkSyncEndpoint endpoint;

    private ContentStore contentStore;
    private final String spaceId = "space-id";
    private boolean syncDeletes;
    private final long maxFileSize = 1024;

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
        syncDeletes = false;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testGetFilesList() throws Exception {
        List<String> contents = new ArrayList<String>();
        contents.add("item-0");
        contents.add("item-1");
        contents.add("item-2");
        contents.add("chunk" + chunkSuffix + 0);
        contents.add("chunk" + chunkSuffix + 1);
        contents.add("manifest" + manifestSuffix);

        createGetFilesListMocks(contents);
        replayMocks();

        endpoint = new DuraStoreChunkSyncEndpoint(contentStore,
                                                  spaceId,
                                                  syncDeletes,
                                                  maxFileSize);
        Iterator<String> filesList = endpoint.getFilesList();
        Assert.assertNotNull(filesList);

        Assert.assertEquals(contents.get(0), filesList.next());
        Assert.assertEquals(contents.get(1), filesList.next());
        Assert.assertEquals(contents.get(2), filesList.next());
        Assert.assertEquals(contents.get(5), filesList.next() + manifestSuffix);
        Assert.assertNull(filesList.next());
    }

    private void createGetFilesListMocks(List<String> contents)
        throws ContentStoreException {

        EasyMock.expect(contentStore.getSpaceContents(spaceId)).andReturn(
            contents.iterator()).times(2);

    }
}
