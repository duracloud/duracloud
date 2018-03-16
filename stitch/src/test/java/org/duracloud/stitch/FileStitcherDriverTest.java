/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.domain.Content;
import org.duracloud.stitch.datasource.DataSource;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 * Date: 9/5/11
 */
public class FileStitcherDriverTest {

    private FileStitcherDriver driver;

    private DataSource dataSource;
    private List<InputStream> streams = new ArrayList<InputStream>();

    private File toDir;
    private static final String chunkIdPrefix = "chunk-id";
    private static final String spaceId = "space-id";
    private static final String contentId = "content-id";
    private static final String manifestId =
        contentId + ChunksManifest.manifestSuffix;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File("target");
        Assert.assertTrue("target/ must exist: " + targetDir.getAbsolutePath(),
                          targetDir.exists());
        toDir = new File(targetDir, "test-stitcher-driver");
        FileUtils.deleteDirectory(toDir);

        dataSource = EasyMock.createMock("DataSource", DataSource.class);
        driver = new FileStitcherDriver(dataSource);
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
    public void testStitch() throws Exception {
        createStitchMocks();
        replayMocks();
        driver.stitch(spaceId, manifestId, toDir);

        File result = new File(toDir, contentId);
        Assert.assertTrue("stitched file must exist", result.exists());
        Assert.assertTrue("stitched file must have size", result.length() > 0);
    }

    private void createStitchMocks() {
        // build manifest
        String sourceContentId = contentId;
        String sourceMimetype = "text/plain";
        long sourceByteSize = 99;
        ChunksManifest manifest = new ChunksManifest(sourceContentId,
                                                     sourceMimetype,
                                                     sourceByteSize);
        // build chunks
        String md5 = "md5";
        String index;
        String chunkId;
        String chunkText;
        final int numChunks = 5;
        for (int i = 0; i < numChunks; ++i) {
            // create chunk entry.
            index = getStringIndex(i);
            chunkId = chunkIdPrefix + ChunksManifest.chunkSuffix + index;
            chunkText = getChunkContent(i);
            manifest.addEntry(chunkId, md5, chunkText.length());

            // create chunk expectation.
            Content chunk = new Content();
            chunk.setId(chunkId);
            chunk.setStream(getStream(chunkText));
            EasyMock.expect(dataSource.getContent(spaceId, chunkId)).andReturn(
                chunk);
        }

        String xml = ManifestDocumentBinding.createDocumentFrom(manifest);

        Content content = new Content();
        content.setId(manifestId);
        content.setStream(getStream(xml));
        EasyMock.expect(dataSource.getContent(spaceId, manifestId)).andReturn(
            content);
    }

    private String getStringIndex(int i) {
        return String.format("%1$04d", i);
    }

    private String getChunkContent(int index) {
        String chunkTextIntro = "hello-";
        return chunkTextIntro + index;
    }

    private InputStream getStream(String xml) {
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        streams.add(stream);
        return stream;
    }
}
