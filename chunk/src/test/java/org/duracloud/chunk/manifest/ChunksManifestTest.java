/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.manifest;

import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Feb 7, 2010
 */
public class ChunksManifestTest {

    private ChunksManifest manifest;
    private String sourceContentId = "sourceContentId";
    private String sourceMimetype = "sourceMimetype";
    private long sourceSize = 1000;
    private String sourceMD5 = "sourceMD5";

    private String chunkIdPrefix = sourceContentId + ".dura-chunk-";
    private String chunkMD5Prefix = "md5-";

    private int NUM_ENTRIES = 11;

    @Before
    public void setUp() {
        manifest = new ChunksManifest(sourceContentId,
                                      sourceMimetype,
                                      sourceSize);
        manifest.setMD5OfSourceContent(sourceMD5);
    }

    @After
    public void tearDown() {
        manifest = null;
    }

    @Test
    public void testNextChunkId() {
        String chunkId;
        String prefix = sourceContentId + ".dura-chunk-";
        for (int i = 0; i < 10000; ++i) {
            chunkId = manifest.nextChunkId();
            Assert.assertNotNull(chunkId);
            Assert.assertEquals(prefix + getChunkIndex(i), chunkId);
        }

        boolean thrown = false;
        try {
            manifest.nextChunkId();
            Assert.fail("Exception expected");
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    private String getChunkIndex(int i) {
        StringBuilder sb = new StringBuilder(Integer.toString(i));
        while (sb.length() < 4) {
            sb.insert(0, "0");
        }
        String num = sb.toString();
        return num;
    }

    @Test
    public void testGetBody() {
        String chunkId;
        for (int i = 0; i < NUM_ENTRIES; ++i) {
            chunkId = chunkIdPrefix + getChunkIndex(i);
            manifest.addEntry(chunkId, chunkMD5Prefix + i, i);
        }

        InputStream body = manifest.getBody();
        Assert.assertNotNull(body);

        verifyManifestXml(body);
    }

    private void verifyManifestXml(InputStream body) {
        ChunksManifest cm = ManifestDocumentBinding.createManifestFrom(body);
        Assert.assertNotNull(cm);

        ChunksManifestBean.ManifestHeader header = cm.getHeader();
        List<ChunksManifestBean.ManifestEntry> entries = cm.getEntries();

        Assert.assertNotNull(header);
        Assert.assertNotNull(entries);

        verifyHeader(header);
        verifyEntries(entries);

    }

    private void verifyHeader(ChunksManifestBean.ManifestHeader header) {
        String contentId = header.getSourceContentId();
        String md5 = header.getSourceMD5();
        String mime = header.getSourceMimetype();
        long size = header.getSourceByteSize();

        Assert.assertNotNull(contentId);
        Assert.assertNotNull(md5);
        Assert.assertNotNull(mime);

        Assert.assertEquals(sourceContentId, contentId);
        Assert.assertEquals(sourceMD5, md5);
        Assert.assertEquals(sourceMimetype, mime);
        Assert.assertEquals(sourceSize, size);

    }

    private void verifyEntries(List<ChunksManifestBean.ManifestEntry> entries) {
        Assert.assertEquals(NUM_ENTRIES, entries.size());

        for (ChunksManifestBean.ManifestEntry entry : entries) {
            String chunkId = entry.getChunkId();
            String md5 = entry.getChunkMD5();
            int index = entry.getIndex();
            long size = entry.getByteSize();

            Assert.assertNotNull(chunkId);
            Assert.assertNotNull(md5);

            Assert.assertTrue(index > -1);
            String sIndex = getChunkIndex(index);
            Assert.assertEquals(chunkIdPrefix + sIndex, chunkId);
            Assert.assertEquals(chunkMD5Prefix + index, md5);
            Assert.assertEquals(index, size);
        }
    }

}
