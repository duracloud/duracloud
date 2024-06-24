/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.chunk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.duracloud.client.ContentStore;
import org.duracloud.client.chunk.ChunkingContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.test.TestConfig;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.common.test.TestEndPoint;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Based on TestDuracloudContentWriter
 *
 * @author Michael Ritter
 */
public class TestChunkingContentStore {
    private static final long CONTENT_LEN = 4100;

    private static final String CONTENT_ID = "test-contentId-1";
    private static final String CONTENT_MIME_TYPE = "text/plain";


    private final ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

    private ChunkingContentStoreManagerImpl smallChunkStoreManager;
    private ChunkingContentStoreManagerImpl largeChunkStoreManager;
    private ContentStore smallChunkStore;
    private ContentStore largeChunkStore;

    private static List<String> spaceIds = new ArrayList<>();

    private static String getSpaceId() {
        String random = java.lang.String.valueOf(new Random().nextInt(99999));
        String spaceId = "contentwriter-test-space-" + random;
        spaceIds.add(spaceId);

        return spaceId;
    }

    @Before
    public void setUp() throws Exception {
        TestConfig testConfig = new TestConfigUtil().getTestConfig();
        TestEndPoint endpoint = testConfig.getTestEndPoint();
        smallChunkStoreManager = new ChunkingContentStoreManagerImpl(endpoint.getHost(), endpoint.getPort(), 1000);
        largeChunkStoreManager = new ChunkingContentStoreManagerImpl(endpoint.getHost(), endpoint.getPort(), 1000*1000);
        SimpleCredential cred = testConfig.getRootCredential();
        smallChunkStoreManager.login(new Credential(cred.getUsername(), cred.getPassword()));
        largeChunkStoreManager.login(new Credential(cred.getUsername(), cred.getPassword()));

        smallChunkStore = smallChunkStoreManager.getPrimaryContentStore();
        largeChunkStore = largeChunkStoreManager.getPrimaryContentStore();
    }

    @After
    public void tearDown() throws Exception {
        for (String spaceId : spaceIds) {
            try {
                smallChunkStore.deleteSpace(spaceId);
            } catch (ContentStoreException e) {
                // do nothing.
            }
        }

        smallChunkStore = null;
    }

    @Test
    public void testAddContent() throws Exception {
        String spaceId = getSpaceId();
        createSpace(spaceId);

        InputStream contentStream = createContentStream(CONTENT_LEN);
        String checksum = checksumUtil.generateChecksum(contentStream);
        contentStream.reset();

        long maxChunkSize = 1000;
        int numChunks = (int) (CONTENT_LEN / maxChunkSize + 1);
        smallChunkStore.addContent(spaceId, CONTENT_ID, contentStream, CONTENT_LEN, CONTENT_MIME_TYPE, checksum,
                                   new HashMap<>());

        List<String> contents = getSpaceContents(spaceId, CONTENT_ID);
        verifyContentAdded(contents, spaceId, CONTENT_ID, CONTENT_LEN, numChunks);
        verifyManifestAdded(contents, spaceId, CONTENT_ID);
    }

    @Test
    public void testWriteSingle() throws Exception {
        String spaceId = getSpaceId();
        createSpace(spaceId);

        InputStream contentStream = createContentStream(CONTENT_LEN);
        String checksum = checksumUtil.generateChecksum(contentStream);
        contentStream.reset();

        var md5 = largeChunkStore.addContent(spaceId, CONTENT_ID, contentStream, CONTENT_LEN, CONTENT_MIME_TYPE,
                                             checksum, new HashMap<>());
        Assert.assertNotNull(md5);
        Assert.assertEquals(checksum, md5);

        List<String> contents = getSpaceContents(spaceId, CONTENT_ID);

        int numChunks = 1;
        verifyContentAdded(contents, spaceId, CONTENT_ID, CONTENT_LEN, numChunks);

        contentStream.reset();
        md5 = largeChunkStore.addContent(spaceId, CONTENT_ID, contentStream, CONTENT_LEN, CONTENT_MIME_TYPE, checksum,
                                         new HashMap<>());
        Assert.assertEquals(checksum, md5);
    }

    @Test
    public void testOverwriteSingleWithChunked() throws Exception {
        String spaceId = getSpaceId();
        createSpace(spaceId);

        InputStream contentStream = createContentStream(CONTENT_LEN);
        String checksum = checksumUtil.generateChecksum(contentStream);
        contentStream.reset();

        // Upload unchunked version
        var md5 = smallChunkStore.addContent(spaceId, CONTENT_ID, contentStream, CONTENT_LEN, CONTENT_MIME_TYPE,
                                             checksum, new HashMap<>());
        Assert.assertNotNull(md5);
        Assert.assertEquals(checksum, md5);

        List<String> contents = getSpaceContents(spaceId, CONTENT_ID);
        verifyContentAdded(contents, spaceId, CONTENT_ID, CONTENT_LEN, 1);

        // Replace with chunked version
        long maxChunkSize = 1000;
        int numChunks = (int) (CONTENT_LEN / maxChunkSize + 1);

        contentStream.reset();
        largeChunkStore.addContent(spaceId, CONTENT_ID, contentStream, CONTENT_LEN, CONTENT_MIME_TYPE,
                                   checksum, new HashMap<>());

        contents = getSpaceContents(spaceId, CONTENT_ID);
        verifyContentAdded(contents, spaceId, CONTENT_ID, CONTENT_LEN, numChunks);
        verifyManifestAdded(contents, spaceId, CONTENT_ID);
    }

    @Test
    public void testOverwriteChunkedWithSingle() throws Exception {
        final String spaceId = getSpaceId();
        createSpace(spaceId);

        InputStream contentStream = createContentStream(CONTENT_LEN);
        String checksum = checksumUtil.generateChecksum(contentStream);
        contentStream.reset();

        // Upload chunked version
        long maxChunkSize = 1000;
        int numChunks = (int) (CONTENT_LEN / maxChunkSize + 1);
        largeChunkStore.addContent(spaceId, CONTENT_ID, contentStream, CONTENT_LEN, CONTENT_MIME_TYPE, checksum,
                                   new HashMap<>());

        List<String> contents = getSpaceContents(spaceId, CONTENT_ID);
        verifyContentAdded(contents, spaceId, CONTENT_ID, CONTENT_LEN, numChunks);
        verifyManifestAdded(contents, spaceId, CONTENT_ID);

        // Replace with unchunked version
        var md5 = smallChunkStore.addContent(spaceId, CONTENT_ID, contentStream, CONTENT_LEN, CONTENT_MIME_TYPE,
                                             checksum, new HashMap<>());
        Assert.assertNotNull(md5);
        Assert.assertEquals(checksum, md5);

        contents = getSpaceContents(spaceId, CONTENT_ID);
        verifyContentAdded(contents, spaceId, CONTENT_ID, CONTENT_LEN, 1);
    }

    private void verifyContentAdded(List<String> contents,
                                    String spaceId,
                                    String contentId,
                                    long contentSize,
                                    int numChunks) {
        long totalSize = 0;
        int itemCount = 0;
        for (String id : contents) {
            Assert.assertTrue(id.startsWith(contentId));

            if (!id.contains("manifest")) {
                Retrier retrier = new Retrier();
                Content content = getContent(spaceId, id);
                Assert.assertNotNull(content);

                Map<String, String> properties = content.getProperties();
                Assert.assertNotNull(properties);

                String size = properties.get("content-size");
                Assert.assertNotNull(size);
                itemCount++;
                totalSize += Long.parseLong(size);
            }
        }

        Assert.assertEquals(numChunks, itemCount);
        Assert.assertEquals(contentSize, totalSize);
    }

    private void verifyManifestAdded(List<String> contents,
                                     String spaceId,
                                     String contentId)
        {

        boolean manifestFound = false;
        long manifestSize = 0;
        for (String id : contents) {
            Assert.assertTrue(id.startsWith(contentId));

            if (id.contains("manifest")) {
                Content content = getContent(spaceId, contentId);
                Assert.assertNotNull(content);

                Map<String, String> properties = content.getProperties();
                Assert.assertNotNull(properties);

                String size = properties.get("content-size");
                Assert.assertNotNull(size);

                manifestFound = true;
                manifestSize = Long.parseLong(size);
            }
        }

        Assert.assertTrue(manifestFound);
        Assert.assertTrue(manifestSize > 0);
    }

    private Content getContent(String spaceId, String id) {
        Retrier retrier = new Retrier();
        try {
            return retrier.execute(() -> smallChunkStore.getContent(spaceId, id));
        } catch (Exception e) {
            Assert.fail("Unable to get contents for " + id);
        }

        return null;
    }

    private List<String> getSpaceContents(String spaceId, String contentId) {
        Retrier retrier = new Retrier();
        Iterator<String> contents = null;
        try {
            contents = retrier.execute(() -> smallChunkStore.getSpaceContents(spaceId, contentId));
        } catch (Exception e) {
            Assert.fail("Unable to get space contents for spaceId=" + spaceId + " prefix=" + contentId);
        }

        Assert.assertNotNull(contents);
        Assert.assertTrue(contents.hasNext());

        List<String> contentList = new ArrayList<>();
        contents.forEachRemaining(contentList::add);

        return contentList;
    }

    private InputStream createContentStream(long size) {
        Assert.assertTrue("let's keep it reasonable", size < 10001);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (long i = 0; i < size; ++i) {
            if (i % 101 == 0) {
                out.write('\n');
            } else {
                out.write('a');
            }
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void createSpace(final String spaceId) {
        final var retrier = new Retrier();
        try {
            final Object acls = retrier.execute(() -> {
                smallChunkStore.createSpace(spaceId);
                return smallChunkStore.getSpaceACLs(spaceId);
            });

            Assert.assertNotNull(acls);
        } catch (Exception e) {
            Assert.fail("Unable to create space " + spaceId);
        }
    }

}