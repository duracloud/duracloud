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
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Feb 7, 2010
 */
public class DuracloudContentWriterTest {

    private DuracloudContentWriter writer;
    private DuracloudContentWriter writerError;
    private DuracloudContentWriter writerErrorThrow;
    private ContentStore contentStore;
    private ContentStore contentStoreThrow;

    @Before
    public void setUp() throws ContentStoreException {
        contentStore = createMockContentStore();
        writer = new DuracloudContentWriter(contentStore);

        contentStoreThrow = createThrowingMockContentStore();
        writerError = new DuracloudContentWriter(contentStoreThrow);
        writerErrorThrow = new DuracloudContentWriter(contentStoreThrow, true);
    }

    private ContentStore createMockContentStore() throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);
        EasyMock.expect(contentStore.addContent(EasyMock.isA(String.class),
                                                EasyMock.isA(String.class),
                                                isChunkInputStream(),
                                                EasyMock.anyLong(),
                                                EasyMock.isA(String.class),
                                                (String) EasyMock.isNull(),
                                                (Map) EasyMock.anyObject()))
            .andReturn("")
            .anyTimes();

        contentStore.createSpace(EasyMock.isA(String.class),
                                 (Map) EasyMock.anyObject());
        EasyMock.expectLastCall();

        EasyMock.expect(contentStore.getSpaceAccess(EasyMock.isA(String.class)))
            .andReturn(ContentStore.AccessType.OPEN);
        EasyMock.replay(contentStore);
        return contentStore;
    }

    private ContentStore createThrowingMockContentStore()
        throws ContentStoreException {
        ContentStore contentStoreThrow = EasyMock.createMock(ContentStore.class);
        EasyMock.expect(contentStoreThrow.addContent(EasyMock.isA(String.class),
                                                EasyMock.isA(String.class),
                                                isChunkInputStream(),
                                                EasyMock.anyLong(),
                                                EasyMock.isA(String.class),
                                                (String) EasyMock.isNull(),
                                                (Map) EasyMock.anyObject()))
            .andThrow(new ContentStoreException("Expected addContent Error "))
            .anyTimes();

        contentStoreThrow.createSpace(EasyMock.isA(String.class),
                                 (Map) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(contentStoreThrow.getSpaceAccess(EasyMock.isA(String.class)))
            .andReturn(ContentStore.AccessType.OPEN)
            .anyTimes();
        EasyMock.replay(contentStoreThrow);
        return contentStoreThrow;
    }

    @After
    public void tearDown() {
        contentStore = null;
        contentStoreThrow = null;
        writer = null;
    }

    @Test
    public void testWrite() throws NotFoundException {
        long contentSize = 4000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId";
        String contentId = "test-contentId";

        long maxChunkSize = 1024;
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentStream,
                                                          contentSize,
                                                          maxChunkSize);
        writer.write(spaceId, chunkable);

        EasyMock.verify(contentStore);
    }

    @Test
    public void testErrorOnWrite() throws NotFoundException {
        long contentSize = 4000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId";
        String contentId = "test-contentId";

        long maxChunkSize = 1024;
        ChunkableContent chunkable = new ChunkableContent(contentId,
                                                          contentStream,
                                                          contentSize,
                                                          maxChunkSize);
        // Test add with error, expecting error to be listed in results
        writerError.write(spaceId, chunkable);
        List<AddContentResult> results = writerError.getResults();
        Assert.assertEquals(5, results.size());
        for(int i=0; i<5; i++) {
            AddContentResult result = results.get(i);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.getContentId().startsWith(contentId));
            Assert.assertEquals(AddContentResult.State.ERROR, result.getState());
        }

        // Test add with error, expecting error to result in a thrown exception
        try {
            writerErrorThrow.write(spaceId, chunkable);
            Assert.fail("Exception expected");
        } catch(DuraCloudRuntimeException expected) {
            Assert.assertNotNull(expected);
        }
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
    public void testWriteSingle() throws NotFoundException {
        long contentSize = 1000;
        InputStream contentStream = createContentStream(contentSize);

        String spaceId = "test-spaceId-1";
        String contentId = "test-contentId-1";

        boolean preserveMD5 = true;
        ChunkInputStream chunk = new ChunkInputStream(contentId,
                                                      contentStream,
                                                      contentSize,
                                                      preserveMD5);

        String md5 = writer.writeSingle(spaceId, null, chunk);
        Assert.assertNotNull(md5);

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
