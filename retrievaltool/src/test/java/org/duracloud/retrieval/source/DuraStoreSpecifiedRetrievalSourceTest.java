/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import static org.duracloud.retrieval.source.DuraStoreSpecifiedRetrievalSourceTest.ContentType.BASIC;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.ContentItem;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Erik Paulsson
 * Date: 7/30/13
 */
public class DuraStoreSpecifiedRetrievalSourceTest {

    private DuraStoreSpecifiedRetrievalSource retrievalSource;
    private ContentStore store;
    private List<String> spaces;
    private List<String> specifiedContentIds;

    private final static String spaceId0 = "space-0";
    private final static String spaceId1 = "space-1";
    private final static String contentIdBase = "content-id-";

    @Before
    public void setUp() throws Exception {
        spaces = new ArrayList<String>();
        spaces.add(spaceId0);
        store = EasyMock.createMock("ContentStore", ContentStore.class);
        EasyMock.expect(store.getSpaces()).andReturn(spaces).times(1);

        specifiedContentIds = new ArrayList<String>();
        specifiedContentIds.add(BASIC.getContentId(0));
        specifiedContentIds.add(BASIC.getContentId(1));
        specifiedContentIds.add(BASIC.getContentId(4));
    }

    @Test
    public void testGetNextContentItem() throws Exception {
        // Verify ContentStore#getSpaceContents does not get called since
        // DuraStoreSpecifiedRetrievalSource overrides method getNextSpace() and only
        // uses the specified content IDs and not all the content IDs of the space.
        EasyMock.expect(store.getSpaceContents(spaceId0))
                .andThrow(new AssertionFailedError("method getSpaceContents should never " +
                                                   "be called for DuraStoreSpecifiedRetrievalSource.")).anyTimes();
        replayMocks();

        retrievalSource = new DuraStoreSpecifiedRetrievalSource(store,
                                                                spaces,
                                                                specifiedContentIds.iterator());
        verifyContents(spaceId0, specifiedContentIds);

        ContentItem item = retrievalSource.getNextContentItem();
        Assert.assertNull(item);
    }

    @Test(expected = DuraCloudRuntimeException.class)
    public void testSpecifiedRetrievalWithMultipleSpaces() {
        // add a second space ID to the list
        // this should cause DuraCloudRuntimeException when
        // constructing DuraStoreSpecifiedRetrievalSource
        spaces.add(spaceId1);
        replayMocks();

        retrievalSource = new DuraStoreSpecifiedRetrievalSource(store,
                                                                spaces,
                                                                specifiedContentIds.iterator());
    }

    private void verifyContents(String spaceId, List<String> specifiedContentIds) {
        ContentItem item = null;
        List<ContentItem> retrievedItems = new ArrayList<ContentItem>();
        int i = 0;
        while ((item = retrievalSource.getNextContentItem()) != null) {
            retrievedItems.add(item);
            Assert.assertNotNull(item);
            Assert.assertEquals(spaceId, item.getSpaceId());
            Assert.assertEquals(specifiedContentIds.get(i), item.getContentId());
            i++;
        }
        Assert.assertEquals(specifiedContentIds.size(), retrievedItems.size());
    }

    private void replayMocks() {
        EasyMock.replay(store);
    }

    /**
     * This inner class helps define types of test content items.
     */
    protected enum ContentType {
        BASIC {
            public String getContentId(int i) {
                return contentIdBase + i;
            }
        }, MANIFEST {
            public String getContentId(int i) {
                return contentIdBase + i + ChunksManifest.manifestSuffix;
            }
        }, CHUNK {
            public String getContentId(int i) {
                return contentIdBase + i + ChunksManifest.chunkSuffix + i;
            }
        };

        public abstract String getContentId(int i);
    }
}
