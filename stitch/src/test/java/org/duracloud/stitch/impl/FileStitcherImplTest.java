/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.impl;

import static org.duracloud.stitch.impl.FileStitcherImplTest.MODE.*;
import static org.duracloud.storage.provider.StorageProvider.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.domain.Content;
import org.duracloud.stitch.FileStitcher;
import org.duracloud.stitch.datasource.DataSource;
import org.duracloud.stitch.error.InvalidManifestException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 9/3/11
 */
public class FileStitcherImplTest {

    private static final String COLOR_PROPERTY = "color";

    private FileStitcher stitcher;

    private DataSource dataSource;
    private List<InputStream> streams = new ArrayList<InputStream>();

    private static final String spaceId = "space-id";
    private static final String contentId =
        "content-id" + ChunksManifest.manifestSuffix;
    private static final String chunkIdPrefix = "chunk-id";
    private static final int NUM_CHUNKS = 5;

    @Before
    public void setUp() throws Exception {
        dataSource = EasyMock.createMock("DataSource", DataSource.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(dataSource);
        for (InputStream stream : streams) {
            stream.close();
        }
    }

    private void replayMocks() {
        EasyMock.replay(dataSource);
    }

    @Test
    public void testGetContentFromManifestErrorName() throws Exception {
        doTestGetContentFromManifestError(ERROR_NAME, "bad-id");
    }

    @Test
    public void testGetContentFromManifestErrorNull() throws Exception {
        doTestGetContentFromManifestError(ERROR_NULL, contentId);
    }

    @Test
    public void testGetContentFromManifestErrorDeserialize() throws Exception {
        doTestGetContentFromManifestError(ERROR_DESERIALIZE, contentId);
    }

    private void doTestGetContentFromManifestError(MODE mode, String manifestId)
        throws Exception {
        createMocks(mode);
        replayMocks();

        stitcher = new FileStitcherImpl(dataSource);

        try {
            stitcher.getContentFromManifest(spaceId, manifestId);
            Assert.fail("exception expected");

        } catch (InvalidManifestException e) {
            // do nothing.
        }
    }

    @Test
    public void testGetContentFromManifest() throws Exception {
        createMocks(VALID_CHUNKS);
        replayMocks();
        
        stitcher = new FileStitcherImpl(dataSource);
        Content content = stitcher.getContentFromManifest(spaceId, contentId);
        Assert.assertNotNull(content);

        InputStream stream = content.getStream();
        while (stream.read() != -1) {
            // spin through the content.
        }

        Map<String, String> props = content.getProperties();
        Assert.assertNotNull(props);

        Assert.assertEquals(5, props.size());
        Assert.assertTrue(props.containsKey(PROPERTIES_CONTENT_SIZE));
        Assert.assertNotNull(props.get(PROPERTIES_CONTENT_SIZE));
        Long.parseLong(props.get(PROPERTIES_CONTENT_SIZE));

        Assert.assertTrue(props.containsKey(COLOR_PROPERTY));
        Assert.assertNotNull(props.get(COLOR_PROPERTY));

        Assert.assertTrue(props.containsKey(PROPERTIES_CONTENT_MIMETYPE));
        Assert.assertNotNull(props.get(PROPERTIES_CONTENT_MIMETYPE));

        Assert.assertTrue(props.containsKey(PROPERTIES_CONTENT_MD5));
        Assert.assertNotNull(props.get(PROPERTIES_CONTENT_MD5));

        Assert.assertTrue(props.containsKey(PROPERTIES_CONTENT_CHECKSUM));
        Assert.assertNotNull(props.get(PROPERTIES_CONTENT_CHECKSUM));
    }

    @Test
    public void testGetContentFromManifestUnordered() throws Exception {
        createMocks(VALID_CHUNKS);
        replayMocks();

        stitcher = new FileStitcherImpl(dataSource);
        Content content = stitcher.getContentFromManifest(spaceId, contentId);
        Assert.assertNotNull(content);

        InputStream contentStream = content.getStream();
        Assert.assertNotNull(contentStream);

        OutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(contentStream, outputStream);

        String fullContent = outputStream.toString();
        contentStream.close();
        outputStream.close();

        // verify order of chunks.
        String chunkText;
        for (int i = 0; i < NUM_CHUNKS; ++i) {
            chunkText = getChunkContent(i);
            Assert.assertTrue(fullContent.startsWith(chunkText));

            fullContent = fullContent.substring(chunkText.length());
        }

        Assert.assertEquals("All chunks should be found and pruned.",
                            0,
                            fullContent.length());
    }

    private void createMocks(MODE mode) {
        if (mode == ERROR_NAME) {
            return;
        }

        Content content = null;
        if (mode == VALID_CHUNKS) {
            List<Integer> indexes = new ArrayList<Integer>();
            indexes.add(3);
            indexes.add(4);
            indexes.add(0);
            indexes.add(2);
            indexes.add(1);
            content = createManifestContentWithChunks(indexes);

        } else if (mode == ERROR_DESERIALIZE) {
            content = new Content();
        }

        EasyMock.expect(dataSource.getContent(spaceId, contentId)).andReturn(
            content);
    }

    private Content createManifestContentWithChunks(List<Integer> chunkIndexes) {
        long sourceByteSize = 99;
        ChunksManifest manifest = createManifest(sourceByteSize);

        // sanity check.
        Assert.assertEquals(NUM_CHUNKS, chunkIndexes.size());

        String md5 = "md5";
        String index;
        String chunkId;
        String chunkText;
        for (int chunkIndex : chunkIndexes) {
            // create chunk entry.
            index = getStringIndex(chunkIndex);
            chunkId = chunkIdPrefix + ChunksManifest.chunkSuffix + index;
            chunkText = getChunkContent(chunkIndex);
            manifest.addEntry(chunkId, md5, chunkText.length());

            // create chunk expectation.
            Content chunk = new Content();
            chunk.setId(chunkId);
            chunk.setStream(getStream(chunkText));
            EasyMock.expect(dataSource.getContent(spaceId, chunkId)).andReturn(
                chunk);
        }

        return doCreateManifestContent(manifest);
    }

    private ChunksManifest createManifest(long sourceByteSize) {
        String sourceContentId = "content-id";
        String sourceMimetype = "text/plain";
        ChunksManifest manifest = new ChunksManifest(sourceContentId,
                                                     sourceMimetype,
                                                     sourceByteSize);
        manifest.setMD5OfSourceContent("source-md5");
        return manifest;
    }

    private Content doCreateManifestContent(ChunksManifest manifest) {
        Content content = new Content();

        String xml = ManifestDocumentBinding.createDocumentFrom(manifest);

        content.setId(contentId);
        Map<String,String> properties = new HashMap<>();
        properties.put(StorageProvider.PROPERTIES_CONTENT_SIZE, "xxx");
        properties.put(COLOR_PROPERTY, "green");
        
        content.setProperties(properties);
        content.setStream(getStream(xml));

        return content;
    }

    private String getStringIndex(int i) {
        return String.format("%1$04d", i);
    }

    private String getChunkContent(int index) {
        String chunkTextIntro = "hello-";
        return chunkTextIntro + index;
    }

    private InputStream getStream(String text) {
        InputStream stream = new ByteArrayInputStream(text.getBytes());
        streams.add(stream);
        return stream;
    }

    protected enum MODE {
        VALID_CHUNKS,
        ERROR_NAME,
        ERROR_NULL,
        ERROR_DESERIALIZE;
    }
}
