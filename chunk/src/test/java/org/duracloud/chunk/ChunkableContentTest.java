/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.chunk.stream.KnownLengthInputStream;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.ChecksumUtil;
import static org.duracloud.common.util.ChecksumUtil.Algorithm;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Andrew Woods
 *         Date: Feb 2, 2010
 */
public class ChunkableContentTest {

    private ChunkableContent chunkable;
    private String contentId = "contentId";
    private final long MAX_CHUNK_SIZE = 10000;

    private DigestInputStream contentInputStream;
    private MessageDigest contentChecksum;
    private long contentSize;

    private List<File> chunkFiles;
    private File contentFile;

    private final String CHUNK_PREFIX = "a-chunk-";
    private final String LARGE_PREFIX = "a-large-";

    @Before
    public void setUp() throws IOException {
        contentSize = MAX_CHUNK_SIZE * 4 + (MAX_CHUNK_SIZE / 2);
        contentInputStream = createContent(contentSize);

        chunkFiles = new ArrayList<File>();
        chunkable = new ChunkableContent(contentId,
                                         contentInputStream,
                                         contentSize,
                                         MAX_CHUNK_SIZE);
    }

    @After
    public void tearDown() {
        IOUtils.closeQuietly(contentInputStream);

        for (File f : chunkFiles) {
            FileUtils.deleteQuietly(f);
        }
        FileUtils.deleteQuietly(contentFile);
    }

    @Test
    public void testBasicChunking() throws Exception {
        doChunking();
        verifyTotalChunkLength();
        verifyTotalChunkChecksum();
    }

    private void doChunking() throws IOException {
        int i = 0;
        File f;
        FileOutputStream out;
        for (ChunkInputStream chunk : chunkable) {
            f = File.createTempFile(CHUNK_PREFIX + i++ + "-", ".txt");
            chunkFiles.add(f);

            out = new FileOutputStream(f);
            IOUtils.copy(chunk, out);

            IOUtils.closeQuietly(chunk);
            IOUtils.closeQuietly(out);
        }

        Assert.assertNotNull(chunkFiles);
        Assert.assertTrue(chunkFiles.size() > 0);

        contentChecksum = contentInputStream.getMessageDigest();
        Assert.assertNotNull(contentChecksum);
        Assert.assertTrue(contentChecksum.getDigestLength() > 0);
    }

    private void verifyTotalChunkLength() {
        long size = 0;
        for (File chunk : chunkFiles) {
            size += chunk.length();
        }

        Assert.assertEquals(contentSize, size);
    }

    private void verifyTotalChunkChecksum() throws Exception {
        MessageDigest md5 = MessageDigest.getInstance(Algorithm.MD5.name());
        DigestInputStream istream;
        for (File chunk : chunkFiles) {
            istream = new DigestInputStream(new FileInputStream(chunk), md5);
            read(istream);
            md5 = istream.getMessageDigest();
            IOUtils.closeQuietly(istream);
        }

        Assert.assertNotNull(md5);
        Assert.assertTrue(MessageDigest.isEqual(contentChecksum.digest(),
                                                md5.digest()));

    }

    private void read(DigestInputStream istream) throws IOException {
        while (istream.read() != -1) {
            // walk through the stream
        }
    }

    private DigestInputStream createContent(long size) throws IOException {
        contentFile = File.createTempFile(LARGE_PREFIX, ".txt");
        FileOutputStream out = new FileOutputStream(contentFile);

        int MIN_CHAR = 32;
        int MAX_CHAR_MINUS_MIN_CHAR = 126 - MIN_CHAR;
        Random r = new Random();
        for (long i = 0; i < size; ++i) {

            if (i % 101 == 0) {
                out.write("\n".getBytes());
            } else {
                out.write(r.nextInt(MAX_CHAR_MINUS_MIN_CHAR) + MIN_CHAR);
            }
        }
        IOUtils.closeQuietly(out);

        return ChecksumUtil.wrapStream(new FileInputStream(contentFile),
                                       Algorithm.MD5);
    }

    @Test
    public void testNext() throws IOException {
        ChunkInputStream chunk = chunkable.next();
        Assert.assertNotNull(chunk);

        verifyThrow(true);

        int bytesRead = 0;

        // Read less than a full chunk.
        while (bytesRead < MAX_CHUNK_SIZE - 10) {
            chunk.read();
            bytesRead++;
        }

        verifyThrow(true);

        // Rest of chunk.
        while (bytesRead < MAX_CHUNK_SIZE) {
            chunk.read();
            bytesRead++;
        }

        verifyThrow(false);
        Assert.assertEquals(bytesRead, MAX_CHUNK_SIZE);

    }

    private void verifyThrow(boolean expected) {
        boolean exceptionThrown = false;
        try {
            chunkable.next();
            Assert.assertEquals(false, expected);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        Assert.assertEquals(expected, exceptionThrown);
    }

    @Test
    public void testManifest() throws Exception {
        doChunking();
        ChunksManifest manifest = chunkable.finalizeManifest();
        Assert.assertNotNull(manifest);

        ChunksManifestBean.ManifestHeader header = manifest.getHeader();
        Assert.assertNotNull(header);

        List<ChunksManifestBean.ManifestEntry> entries = manifest.getEntries();
        Assert.assertNotNull(entries);
        Assert.assertEquals(chunkFiles.size(), entries.size());

        KnownLengthInputStream body = manifest.getBody();
        Assert.assertNotNull(body);
        Assert.assertTrue(body.getLength() > 0);
    }

    @Test
    public void testCalculateBufferSize() {
        // Testing values of maxChunkSize, which must be multiples of 1000
        // Tests values to the 5GB byte limit
        for(long i=1000; i<5000000000l; i+=(1000+i)) {
            int bufferSize = chunkable.calculateBufferSize(i);
            // Resulting buffer size should be less than or equal to 8000
            Assert.assertTrue(bufferSize <= 8000);
            // Resulting buffer size must be a multiple of the maxChunkSize
            Assert.assertEquals(0, i % bufferSize);
        }

        // Verify exception on invalid maxChunkSize
        try {
            chunkable.calculateBufferSize(500);
            Assert.fail("Exception expected");
        } catch(DuraCloudRuntimeException e) {
        }
        try {
            chunkable.calculateBufferSize(12345);
            Assert.fail("Exception expected");
        } catch(DuraCloudRuntimeException e) {
        }
    }

}
