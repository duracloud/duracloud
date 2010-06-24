/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.manifest.xml;

import org.apache.commons.io.IOUtils;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Feb 9, 2010
 */
public class ManifestElementReaderWriterTest {

    private static final int NUM_ENTRIES = 5;

    private ChunksManifestBean bean;

    private InputStream stream;

    @Before
    public void setUp() {
        bean = new ChunksManifestBean();

        String sourceContentId = "sourceContentId";
        String sourceMimetype = "application/pdf";
        long sourceByteSize = 1234;
        ChunksManifestBean.ManifestHeader header = new ChunksManifestBean.ManifestHeader(
            sourceContentId,
            sourceMimetype,
            sourceByteSize);
        header.setSourceMD5("md5-source");

        List<ChunksManifestBean.ManifestEntry> entries = new ArrayList<ChunksManifestBean.ManifestEntry>();
        ChunksManifestBean.ManifestEntry entry;
        String chunkId = "chunkId-";
        String chunkMD5 = "md5-";
        long chunkSize = 5;
        for (int i = 0; i < NUM_ENTRIES; ++i) {
            entry = new ChunksManifestBean.ManifestEntry(chunkId + i,
                                                         chunkMD5 + i,
                                                         i,
                                                         chunkSize * i);
            entries.add(entry);
        }

        bean.setHeader(header);
        bean.setEntries(entries);
    }

    @After
    public void tearDown() {
        IOUtils.closeQuietly(stream);
        stream = null;
    }

    @Test
    public void testReadWrite() {
        String xml = ManifestDocumentBinding.createDocumentFrom(bean);
        Assert.assertNotNull(xml);

        stream = new ByteArrayInputStream(xml.getBytes());
        ChunksManifestBean b = ManifestDocumentBinding.createManifestFrom(stream);
        Assert.assertNotNull(b);

        verifyBean(b);
    }

    private void verifyBean(ChunksManifestBean b) {
        ChunksManifestBean.ManifestHeader header = b.getHeader();
        Assert.assertNotNull(header);

        String contentId = header.getSourceContentId();
        String mime = header.getSourceMimetype();
        String md5 = header.getSourceMD5();
        long size = header.getSourceByteSize();

        Assert.assertNotNull(contentId);
        Assert.assertNotNull(mime);
        Assert.assertNotNull(md5);

        Assert.assertEquals(bean.getHeader().getSourceContentId(), contentId);
        Assert.assertEquals(bean.getHeader().getSourceMimetype(), mime);
        Assert.assertEquals(bean.getHeader().getSourceMD5(), md5);
        Assert.assertEquals(bean.getHeader().getSourceByteSize(), size);


        List<ChunksManifestBean.ManifestEntry> entries = b.getEntries();
        Assert.assertNotNull(entries);
        Assert.assertEquals(bean.getEntries().size(), entries.size());

        for (int i = 0; i < entries.size(); ++i) {
            ChunksManifestBean.ManifestEntry entry = getEntry(bean.getEntries(),
                                                              i);
            ChunksManifestBean.ManifestEntry testEntry = getEntry(entries, i);

            Assert.assertNotNull(testEntry.getChunkId());
            Assert.assertNotNull(testEntry.getChunkMD5());
            Assert.assertNotNull(testEntry.getIndex());
            Assert.assertNotNull(testEntry.getByteSize());

            Assert.assertEquals(entry.getChunkId(), testEntry.getChunkId());
            Assert.assertEquals(entry.getChunkMD5(), testEntry.getChunkMD5());
            Assert.assertEquals(entry.getIndex(), testEntry.getIndex());
            Assert.assertEquals(entry.getByteSize(), testEntry.getByteSize());
        }

    }

    private ChunksManifestBean.ManifestEntry getEntry(List<ChunksManifestBean.ManifestEntry> entries,
                                                      int i) {
        for (ChunksManifestBean.ManifestEntry entry : entries) {
            if (entry.getIndex() == i) {
                return entry;
            }
        }
        Assert.fail("Entry not found for index: " + i);
        return null;
    }
}
