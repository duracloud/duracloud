/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.writer;

import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Feb 7, 2010
 */
public class DuracloudContentWriterTest {

    private DuracloudContentWriter writerError;
    private DuracloudContentWriter writerErrorThrow;
    private ContentStore contentStore;
    private ContentStore contentStoreThrow;
    private String username = "user-1";
    private final String checksum = "this-is-a-checksum";

    @Before
    public void setUp() throws ContentStoreException {
        contentStore = EasyMock.createMock(ContentStore.class);

        contentStoreThrow = EasyMock.createMock(ContentStore.class);
        writerError = new DuracloudContentWriter(contentStoreThrow, username, 3, 10);
        writerErrorThrow =
            new DuracloudContentWriter(contentStoreThrow, username, true, false,  3, 10);
    }

    private ContentStore createMockContentStore(boolean spaceExists,
                                                boolean expectAddContent)
        throws ContentStoreException {
        if(expectAddContent) {
            EasyMock.expect(contentStore.addContent(EasyMock.isA(String.class),
                                                    EasyMock.isA(String.class),
                                                    EasyMock.isA(InputStream.class),
                                                    EasyMock.anyLong(),
                                                    EasyMock.isA(String.class),
                                                    EasyMock.isA(String.class),
                                                    (Map) EasyMock.anyObject()))
                    .andReturn("")
                    .anyTimes();
        } else { // Expect only the manifest to be added
            EasyMock.expect(contentStore.addContent(EasyMock.eq("test-spaceId"),
                                                    EasyMock.eq("test-contentId.dura-manifest"),
                                                    EasyMock.isA(InputStream.class),
                                                    EasyMock.anyLong(),
                                                    EasyMock.eq("application/xml"),
                                                    EasyMock.isA(String.class),
                                                    (Map) EasyMock.anyObject()))
                    .andReturn("")
                    .once();
        }

        Map<String, AclType> acls = new HashMap<String, AclType>();
        acls.put(StorageProvider.PROPERTIES_SPACE_ACL_PUBLIC, AclType.READ);

        if (!spaceExists) {
            EasyMock.expect(contentStore.getSpaceACLs(EasyMock.isA(String.class)))
                    .andThrow(new ContentStoreException("canned-exception"));
            contentStore.createSpace(EasyMock.isA(String.class));
            EasyMock.expectLastCall();

            EasyMock.expect(contentStore.getSpaceACLs(EasyMock.isA(String.class)))
                    .andReturn(acls);

        } else {
            EasyMock.expect(contentStore.getSpaceACLs(EasyMock.isA(String.class)))
                    .andReturn(acls)
                    .times(2);
        }

        return contentStore;
    }

    private void updateMockContentStoreContentCheck(boolean chunkExists)
        throws ContentStoreException {
        if (!chunkExists) {
            EasyMock.expect(contentStore.contentExists(EasyMock.isA(String.class),
                                                       EasyMock.isA(String.class)))
                    .andReturn(false)
                    .anyTimes();
        } else {
            EasyMock.expect(contentStore.contentExists(EasyMock.isA(String.class),
                                                       EasyMock.isA(String.class)))
                .andReturn(true)
                .anyTimes();

            Map<String, String> props = new HashMap<>();
            props.put(ContentStore.CONTENT_CHECKSUM, checksum);
            EasyMock.expect(contentStore.getContentProperties(EasyMock.isA(String.class),
                                                              EasyMock.isA(String.class)))
                    .andReturn(props)
                    .anyTimes();
        }
    }

    private void
            setupThrowingMockContentStore(int addChunkFailures,
                                          int addChunkSuccesses,
                                          int addManifestFailures,
                                          int addManifestSuccesses)
                                              throws ContentStoreException {

        EasyMock.expect(contentStoreThrow.contentExists(EasyMock.isA(String.class),
                                                        EasyMock.isA(String.class)))
                .andReturn(false)
                .anyTimes();

        contentStoreThrow.createSpace(EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(contentStoreThrow.getSpaceACLs(EasyMock.isA(String.class)))
                .andReturn(new HashMap<String, AclType>())
                .anyTimes();

        expectAddContentErrors(addChunkFailures);
        expectAddContentSuccesses(addChunkSuccesses);
        expectAddContentErrors(addManifestFailures);
        expectAddContentSuccesses(addManifestSuccesses);
    }

    protected void expectAddContentSuccesses(int times)
                                                 throws ContentStoreException {
        if (times > 0) {

            EasyMock.expect(contentStoreThrow.addContent(EasyMock.isA(String.class),
                                                         EasyMock.isA(String.class),
                                                         isChunkInputStream(),
                                                         EasyMock.anyLong(),
                                                         EasyMock.isA(String.class),
                                                         EasyMock.isA(String.class),
                                                         (Map) EasyMock.anyObject()))
                    .andReturn(checksum)
                    .times(times);
        }
    }

    protected void expectAddContentErrors(int times)
        throws ContentStoreException {
        if (times > 0) {
            EasyMock.expect(contentStoreThrow.addContent(EasyMock.isA(String.class),
                                                         EasyMock.isA(String.class),
                                                         isChunkInputStream(),
                                                         EasyMock.anyLong(),
                                                         EasyMock.isA(String.class),
                                                         EasyMock.isA(String.class),
                                                         (Map) EasyMock.anyObject()))
                    .andThrow(new ContentStoreException("Expected addContent Error "))
                    .times(times);
        }
    }

    private void replayMocks() {
        EasyMock.replay(contentStore, contentStoreThrow);
    }

    @After
    public void tearDown() {
        contentStore = null;
        contentStoreThrow = null;
    }

    /*
     * Tests a write under the condition that the space exists
     * and the content chunk does not exist
     */
    @Test
    public void testWrite() throws Exception {
        createMockContentStore(true, true);
        updateMockContentStoreContentCheck(false);
        doTestWrite(false, false);
    }

    /*
     * Tests a write under the condition that the space exists
     * and jumpstart is on
     */
    @Test
    public void testWriteJumpstart() throws Exception {
        createMockContentStore(true, true);
        doTestWrite(false, true);
    }

    /*
     * Tests a write under the condition that the space exists
     * and the content chunk also exists, but has the wrong checksum
     */
    @Test
    public void testWriteWrongChunkExists() throws Exception {
        createMockContentStore(true, true);
        updateMockContentStoreContentCheck(true);
        doTestWrite(false, false);
    }

    /*
     * Tests a write under the condition that the space exists
     * and the content chunk also exists and has the expected checksum
     */
    @Test
    public void testWriteCorrectChunkExists() throws Exception {
        createMockContentStore(true, false);
        updateMockContentStoreContentCheck(true);
        doTestWrite(true, false);
    }

    /*
     * Tests a write under the condition that the space does not exist
     * and the content chunk also does not exist
     */
    @Test
    public void testWriteSpaceNotExist() throws Exception {
        createMockContentStore(false, true);
        updateMockContentStoreContentCheck(false);
        doTestWrite(false, false);
    }

    private void doTestWrite(boolean validChecksum, boolean jumpStart) throws Exception {
        DuracloudContentWriter writer =
            new DuracloudContentWriter(contentStore, username, false, jumpStart);
        if(validChecksum) {
            writer.setChecksumUtil(new ChecksumUtil (ChecksumUtil.Algorithm.MD5) {
                @Override
                public String generateChecksum(File file) throws IOException {
                    return checksum;
                }
            });
        }

        replayMocks();
        long contentSize = 4000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId";
        String contentId = "test-contentId";

        long maxChunkSize = 1000;
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentStream,
                                                          contentSize,
                                                          maxChunkSize);
        writer.write(spaceId, chunkable);

        EasyMock.verify(contentStore);
    }

    @Test
    public void testErrorOnWriteManifest() throws NotFoundException,ContentStoreException {
        int contentSize = 4000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId";
        String contentId = "test-contentId";

        int maxChunkSize = 1000;
        int chunkCount = contentSize/maxChunkSize;
        
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentStream,
                                                          contentSize,
                                                          maxChunkSize);
        
        setupThrowingMockContentStore(0, chunkCount, writerErrorThrow.getMaxRetries()+1, 0);
        replayMocks();
        // Test add with error, expecting error to result in a thrown exception
        try {

            writerErrorThrow.write(spaceId, chunkable);

            Assert.fail("Exception expected");
        } catch(DuraCloudRuntimeException expected) {
            Assert.assertNotNull(expected);
        }
        List<AddContentResult> results = writerError.getResults();
        results = writerErrorThrow.getResults();
        Assert.assertEquals(0, results.size());

        EasyMock.verify(contentStoreThrow);
        
    }

    @Test
    public void testRetrySuccessOnWriteManifest() throws NotFoundException,ContentStoreException {
        int contentSize = 4000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId";
        String contentId = "test-contentId";

        int maxChunkSize = 1000;
        
        int chunkCount = contentSize/maxChunkSize;
        
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentStream,
                                                          contentSize,
                                                          maxChunkSize);

        setupThrowingMockContentStore(0, chunkCount, writerErrorThrow.getMaxRetries(), 1);
        replayMocks();
        writerErrorThrow.write(spaceId, chunkable);

        EasyMock.verify(contentStoreThrow);
    }

    
    @Test
    public void testErrorOnWriteChunkNoThrow() throws NotFoundException,ContentStoreException {
        int contentSize = 4000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId";
        String contentId = "test-contentId";

        int maxChunkSize = 1000;
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentStream,
                                                          contentSize,
                                                          maxChunkSize);

        //no retries on non throwing 
        setupThrowingMockContentStore(writerErrorThrow.getMaxRetries()+1, 0, 0, 0);
        replayMocks();

        // Test add with error, expecting error to be listed in results
        writerError.write(spaceId, chunkable);
        List<AddContentResult> results = writerError.getResults();
        
        Assert.assertEquals(1, results.size());
        for(AddContentResult result :  results) {
            Assert.assertNotNull(result);
            Assert.assertTrue(result.getContentId().startsWith(contentId));
            Assert.assertEquals(AddContentResult.State.ERROR, result.getState());
        }

        EasyMock.verify(contentStoreThrow);
    }

    
    @Test
    public void testErrorOnWriteChunkThrows() throws NotFoundException,ContentStoreException {
        int contentSize = 4000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId";
        String contentId = "test-contentId";

        int maxChunkSize = 1000;
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentStream,
                                                          contentSize,
                                                          maxChunkSize);

        setupThrowingMockContentStore(writerErrorThrow.getMaxRetries()+1, 0, 0, 0);
        replayMocks();
        // Test add with error, expecting error to result in a thrown exception
        try {

            writerErrorThrow.write(spaceId, chunkable);

            Assert.fail("Exception expected");
        } catch(DuraCloudRuntimeException expected) {
            Assert.assertNotNull(expected);
        }
        List<AddContentResult> results = writerError.getResults();
        results = writerErrorThrow.getResults();
        Assert.assertEquals(0, results.size());

        EasyMock.verify(contentStoreThrow);
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

    @Test
    public void testWriteSingle() throws Exception {
        createMockContentStore(true, true);
        replayMocks();
        long contentSize = 1000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId-1";
        String contentId = "test-contentId-1";

        boolean preserveMD5 = true;
        ChunkInputStream chunk = new ChunkInputStream(contentId,
                                                      contentStream,
                                                      contentSize,
                                                      preserveMD5);

        DuracloudContentWriter writer =
            new DuracloudContentWriter(contentStore, username);
        String md5 = writer.writeSingle(spaceId, "checksum", chunk);
        Assert.assertNotNull(md5);

        EasyMock.verify(contentStore);
    }

    /**
     * This class is an EasyMock helper.
     */
    private static class ChunkInputsStreamMatcher implements IArgumentMatcher {

        public boolean matches(Object o) {
            if (null == o || !(o instanceof InputStream)) {
                return false;
            } else {
                InputStream is = (InputStream) o;
                try {
                    while (is.read() != -1) {
                        // spin through content;
                    }
                } catch (IOException e) {
                    // do nothing
                }
            }
            return true;
        }

        public void appendTo(StringBuffer stringBuffer) {
            stringBuffer.append(ChunkInputsStreamMatcher.class.getCanonicalName());
        }
    }

    /**
     * This method registers the EasyMock helper.
     *
     * @return
     */
    private static InputStream isChunkInputStream() {
        EasyMock.reportMatcher(new ChunkInputsStreamMatcher());
        return null;
    }

}
