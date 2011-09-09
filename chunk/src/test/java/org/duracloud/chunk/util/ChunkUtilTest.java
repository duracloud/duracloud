/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.util;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 9/8/11
 */
public class ChunkUtilTest {

    private ChunkUtil util = new ChunkUtil();

    @Test
    public void testPreChunkedContentId() throws Exception {
        String baseId = "baseId";
        String fullId = baseId + ChunksManifest.manifestSuffix;
        doTestPreChunkedContentId(baseId, fullId);

        fullId = baseId;
        doTestPreChunkedContentId(baseId, fullId);

        fullId = baseId + ChunksManifest.chunkSuffix;
        doTestPreChunkedContentId(fullId, fullId);

        fullId = baseId + ChunksManifest.manifestSuffix + 9;
        doTestPreChunkedContentId(fullId, fullId);

        doTestPreChunkedContentId(null, null);
    }

    private void doTestPreChunkedContentId(String baseId, String fullId) {
        String contentId = util.preChunkedContentId(fullId);
        Assert.assertEquals(baseId, contentId);
    }

    @Test
    public void testIsChunkManifest() throws Exception {
        final String baseId = "content-id";
        String contentId = baseId;
        Assert.assertFalse(util.isChunkManifest(contentId));

        contentId = baseId + ChunksManifest.chunkSuffix;
        Assert.assertFalse(util.isChunkManifest(contentId));
        Assert.assertFalse(util.isChunkManifest(null));

        contentId = baseId + ChunksManifest.manifestSuffix;
        Assert.assertTrue(util.isChunkManifest(contentId));
    }

    @Test
    public void testIsChunk() throws Exception {
        final String baseId = "content-id";
        String contentId = baseId;
        Assert.assertFalse(util.isChunkManifest(contentId));

        contentId = baseId + ChunksManifest.manifestSuffix;
        Assert.assertFalse(util.isChunk(contentId));

        contentId = baseId + ChunksManifest.chunkSuffix;
        Assert.assertFalse(util.isChunk(contentId));
        Assert.assertFalse(util.isChunk(null));

        contentId = baseId + ChunksManifest.chunkSuffix + 0;
        Assert.assertTrue(util.isChunk(contentId));
    }
}
