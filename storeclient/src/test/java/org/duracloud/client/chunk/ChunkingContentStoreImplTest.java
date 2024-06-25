/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chunk;


import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.FileChunkerOptions;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.client.HttpHeaders;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @author shake
 */
public class ChunkingContentStoreImplTest {

    private static final String BASE_URL = "http://example.org";
    private static final String STORE_ID = "1";

    private static final String SPACE_ID = "space-id";
    private static final String CONTENT_ID = "content-id";

    private ChunkingContentStoreImpl contentStore;
    private RestHttpHelper restHttpHelper;

    @Before
    public void setUp() throws Exception {
        restHttpHelper = createMock("RestHttpHelper", RestHttpHelper.class);
    }

    private ChunkingContentStoreImpl newChunkingContentStoreImpl() {
        return new ChunkingContentStoreImpl(BASE_URL,
                                            StorageProviderType.AMAZON_S3,
                                            STORE_ID,
                                            true,
                                            restHttpHelper,
                                            -1,
                                            new FileChunkerOptions(1000L));
    }

    /**
     * Test for adding content that isn't chunked
     * Calls expected to be made:
     *   * PUT to add content
     *   * HEAD to check for chunked content
     */
    @Test
    public void addNonChunkedContent() throws Exception {
        contentStore = newChunkingContentStoreImpl();

        final HttpResponse checkChunkResponse = createMock(HttpResponse.class);
        final Capture<Map<String, String>> headersCapture = Capture.newInstance(CaptureType.FIRST);
        final var checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

        final var input = new ByteArrayInputStream("content".getBytes());
        final var checksum = checksumUtil.generateChecksum(input);
        input.reset();

        final var addContentResponse = mockSuccessfulAddContent(headersCapture, CONTENT_ID, checksum);
        mockCheckForChunkedContent(checkChunkResponse);
        replay(restHttpHelper, addContentResponse, checkChunkResponse);
        contentStore.addContent(SPACE_ID, CONTENT_ID, input, 7, "text/plain", checksum, new HashMap<>());
    }

    private HttpResponse mockSuccessfulAddContent(final Capture<Map<String, String>> headersCapture,
                                                  final String contentId,
                                                  final String outputChecksum) throws Exception {
        final HttpResponse response = createMock(HttpResponse.class);
        String fullURL = BASE_URL + "/" + SPACE_ID + "/" + contentId + "?storeID=" + STORE_ID;
        expect(restHttpHelper.put(eq(fullURL),
                                       anyObject(),
                                       anyString(),
                                       anyLong(),
                                       capture(headersCapture)))
                .andReturn(response);
        expect(response.getStatusCode()).andReturn(201);
        expect(response.getResponseHeader(HttpHeaders.CONTENT_MD5))
                .andReturn(new BasicHeader(HttpHeaders.CONTENT_MD5, outputChecksum));
        return response;
    }

    protected void mockCheckForChunkedContent(final HttpResponse response) throws Exception {
        final String xml = emptySpaceXml();
        final String fullURL = BASE_URL + "/" + SPACE_ID +
                               "?prefix=" + CONTENT_ID + ".dura-" +
                               "&maxResults=" + StorageProvider.DEFAULT_MAX_RESULTS + "&storeID=" + STORE_ID;
        expect(restHttpHelper.get(fullURL)).andReturn(response);
        expect(response.getStatusCode()).andReturn(200);
        expect(response.getResponseBody()).andReturn(xml);
        expect(response.getResponseHeaders()).andReturn(new Header[0]);
    }

    /**
     * Test for adding content that will be chunked
     * HTTP calls expected:
     *   * HEAD for each chunk
     *   * PUT for each chunk
     *   * PUT for manifest
     *   * GET for unchunked version
     *   * GET for space contents for chunks
     *   * GET for space contents when completing iteration of the space contents
     *
     * @throws Exception should never occur
     */
    @Test
    public void addChunkableContent() throws Exception {
        contentStore = newChunkingContentStoreImpl();

        final byte[] content = new byte[2000];
        new Random().nextBytes(content);

        final var checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

        final var byteStream = new ByteArrayInputStream(content);
        final var checksum = checksumUtil.generateChecksum(byteStream);

        // Track mocked http calls for replay and verification
        final List<HttpResponse> mocks = new ArrayList<>();

        // HTTP calls for adding chunked content
        final var manifest = mockSuccessfulAddChunkedContent(byteStream, 2000, 1000L, mocks);

        // HTTP calls adding manifest and checking for unchunked content
        mockSuccessfulAddChunkManifest(manifest, mocks);

        // HTTP calls checking for orphaned chunks (assume none)
        mockCleanupOrphanedChunks(manifest, mocks);

        replay(restHttpHelper);
        mocks.forEach(EasyMock::replay);

        byteStream.reset();
        contentStore.addContent(SPACE_ID, CONTENT_ID, byteStream, content.length,
                                "application/octet-stream", checksum, new HashMap<>());

        verify(restHttpHelper);
        mocks.forEach(EasyMock::verify);
    }

    private void mockSuccessfulAddChunkManifest(final ChunksManifest manifest,
                                                final List<HttpResponse> mocks) throws Exception {
        final var checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

        final Capture<Map<String, String>> headersCapture = Capture.newInstance(CaptureType.FIRST);
        final var putManifest = mockSuccessfulAddContent(headersCapture, manifest.getManifestId(),
                                                         checksumUtil.generateChecksum(manifest.getBody()));

        // Mock getSpaceContents for unchunked version
        final var getContent = mockContentNotFound(CONTENT_ID);

        mocks.add(putManifest);
        mocks.add(getContent);
    }

    private void mockCleanupOrphanedChunks(final ChunksManifest manifest, List<HttpResponse> mocks) throws Exception {
        final HttpResponse getChunks = createMock("HttpResponse", HttpResponse.class);
        final String xml = spaceXml(manifest);
        final String fullURL = BASE_URL + "/" + SPACE_ID +
                               "?prefix=" + CONTENT_ID + ".dura-" +
                               "&maxResults=" + StorageProvider.DEFAULT_MAX_RESULTS + "&storeID=" + STORE_ID;

        expect(restHttpHelper.get(fullURL)).andReturn(getChunks);
        expect(getChunks.getStatusCode()).andReturn(200);
        expect(getChunks.getResponseBody()).andReturn(xml);
        expect(getChunks.getResponseHeaders()).andReturn(new Header[0]);
        mocks.add(getChunks);

        // for iterator
        final HttpResponse getEmpty = createMock("HttpResponse", HttpResponse.class);
        String emptySpaceXml = emptySpaceXml();
        String getSpaceURL = BASE_URL + "/" + SPACE_ID +
                             "?prefix=" + CONTENT_ID + ".dura-" +
                             "&maxResults=" + StorageProvider.DEFAULT_MAX_RESULTS +
                             "&marker=" + CONTENT_ID + ".dura-manifest" + "&storeID=" + STORE_ID;
        expect(restHttpHelper.get(getSpaceURL)).andReturn(getEmpty);
        expect(getEmpty.getStatusCode()).andReturn(200);
        expect(getEmpty.getResponseBody()).andReturn(emptySpaceXml);
        expect(getEmpty.getResponseHeaders()).andReturn(new Header[0]);

        mocks.add(getEmpty);
    }

    private ChunksManifest mockSuccessfulAddChunkedContent(final InputStream input,
                                                           final long contentSize,
                                                           final long maxChunkSize,
                                                           final List<HttpResponse> mocks) throws Exception {
        input.reset();

        final var checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        final var chunkable = new ChunkableContent(CONTENT_ID, input, contentSize, maxChunkSize);
        chunkable.setPreserveChunkMD5s(true);
        for (ChunkInputStream chunkInputStream : chunkable) {
            final String chunkId = chunkInputStream.getChunkId();
            final String chunkChecksum = checksumUtil.generateChecksum(chunkInputStream);

            final HttpResponse headResponse = mockContentNotFound(chunkId);
            mocks.add(headResponse);

            final Capture<Map<String, String>> headersCapture = Capture.newInstance(CaptureType.FIRST);
            final var putResponse = mockSuccessfulAddContent(headersCapture, chunkId, chunkChecksum);
            mocks.add(putResponse);
        }

        return chunkable.finalizeManifest();
    }

    private HttpResponse mockContentNotFound(final String contentId) throws Exception {
        final HttpResponse headResponse = createMock("HttpResponse", HttpResponse.class);
        final String url = BASE_URL + "/" + SPACE_ID + "/" + contentId + "?storeID=" + STORE_ID;
        expect(restHttpHelper.head(eq(url))).andReturn(headResponse);
        expect(headResponse.getStatusCode()).andReturn(404);
        expect(headResponse.getResponseBody()).andReturn("not found");
        return headResponse;
    }

    private String emptySpaceXml() {
        return "<space id=\"" + SPACE_ID + "\"></space>";
    }

    private String spaceXml(final ChunksManifest manifest) {
        final String items = manifest.getEntries().stream()
                                     .map(entry -> "<item>" + entry.getChunkId() + "</item>")
                                     .collect(Collectors.joining());
        return "<space id=\"" + SPACE_ID +  "\">" + items +
               "<item>" + manifest.getManifestId() + "</item>" +
               "</space>";
    }

}