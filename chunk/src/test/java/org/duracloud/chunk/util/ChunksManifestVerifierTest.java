package org.duracloud.chunk.util;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.chunk.manifest.ChunksManifestBean.ManifestEntry;
import org.duracloud.chunk.manifest.ChunksManifestBean.ManifestHeader;
import org.duracloud.chunk.util.ChunksManifestVerifier;
import org.duracloud.chunk.util.ChunksManifestVerifier.Results;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class ChunksManifestVerifierTest extends EasyMockSupport {

    @Mock
    private ContentStore contentStore;

    private ChunksManifest manifest;

    private String spaceId = "space-id";

    @Before
    public void setUp() throws Exception {
        ChunksManifestBean bean = new ChunksManifestBean();

        bean.setHeader(new ManifestHeader("content-id", "text/plain", 1000l));
        List<ManifestEntry> entries = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(new ManifestEntry("chunk-" + i, "checksum", i, 10));
        }

        bean.setEntries(entries);
        manifest = new ChunksManifest(bean);
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    @Test
    public void testSuccess() throws ContentStoreException {
        ChunksManifestVerifier verifier =
            new ChunksManifestVerifier(contentStore);
        for (ManifestEntry entry : manifest.getEntries()) {
            expectGetContentProps(spaceId,
                                  entry,
                                  entry.getByteSize(),
                                  entry.getChunkMD5());
        }

        replayAll();
        Results results = verifier.verifyAllChunks(spaceId, manifest);

        assertTrue(results.isSuccess());
        assertResultSizeIsEqual(results);
    }

    protected void expectGetContentProps(String spaceId,
                                         ManifestEntry entry,
                                         long byteSize,
                                         String md5)
                                             throws ContentStoreException {
        expect(contentStore.getContentProperties(spaceId,
                                                 entry.getChunkId())).andReturn(createProperties(byteSize,
                                                                                                 md5));
    }

    protected void assertResultSizeIsEqual(Results results) {
        assertEquals(manifest.getEntries().size(), results.get().size());
    }

    @Test
    public void testFailureDueToChecksumMismatch()
        throws ContentStoreException {
        ChunksManifestVerifier verifier =
            new ChunksManifestVerifier(contentStore);
        
        for (ManifestEntry entry : manifest.getEntries()) {
            expectGetContentProps(spaceId, entry, entry.getByteSize(), "badChecksum");
        }

        replayAll();
        Results results = verifier.verifyAllChunks(spaceId, manifest);

        assertTrue(!results.isSuccess());
        assertResultSizeIsEqual(results);
    }
    
    @Test
    public void testFailureDueToSizeMismatch()
        throws ContentStoreException {
        ChunksManifestVerifier verifier =
            new ChunksManifestVerifier(contentStore);
        
        for (ManifestEntry entry : manifest.getEntries()) {
            long byteSize = entry.getByteSize();
            expectGetContentProps(spaceId, entry, byteSize+1, entry.getChunkMD5());
        }

        replayAll();
        Results results = verifier.verifyAllChunks(spaceId, manifest);

        assertTrue(!results.isSuccess());
        assertResultSizeIsEqual(results);
    }
    
    @Test
    public void testFailureDueToMissingChunk()
        throws ContentStoreException {
        ChunksManifestVerifier verifier =
            new ChunksManifestVerifier(contentStore);
        
        for (ManifestEntry entry : manifest.getEntries()) {
            expect(contentStore.getContentProperties(spaceId,
                                                     entry.getChunkId())).andThrow(new ContentStoreException("chunk not found!"));
        }

        replayAll();
        Results results = verifier.verifyAllChunks(spaceId, manifest);

        assertTrue(!results.isSuccess());
        assertResultSizeIsEqual(results);
    }


    private Map<String, String> createProperties(long byteSize,
                                                 String chunkMD5) {
        Map<String, String> props = new HashMap<>();
        props.put(ContentStore.CONTENT_CHECKSUM, chunkMD5);
        props.put(ContentStore.CONTENT_SIZE, String.valueOf(byteSize));
        return props;
    }

}
