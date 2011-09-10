/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.client.ContentStore;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.retrieval.source.DuraStoreStitchingRetrievalSourceTest.ContentType.BASIC;
import static org.duracloud.retrieval.source.DuraStoreStitchingRetrievalSourceTest.ContentType.CHUNK;
import static org.duracloud.retrieval.source.DuraStoreStitchingRetrievalSourceTest.ContentType.MANIFEST;

/**
 * @author Andrew Woods
 *         Date: 9/6/11
 */
public class DuraStoreStitchingRetrievalSourceTest {

    private DuraStoreStitchingRetrievalSource retrievalSource;

    private ContentStore store;
    private List<String> spaces;
    private final static boolean allSpaces = false;

    private final static String spaceId0 = "space-0";
    private final static String spaceId1 = "space-1";
    private final static String contentIdBase = "content-id-";

    private List<String> contents0;
    private List<String> contents1;

    private List<InputStream> streams;

    @Before
    public void setUp() throws Exception {
        spaces = new ArrayList<String>();
        spaces.add(spaceId0);
        spaces.add(spaceId1);

        contents0 = new ArrayList<String>();
        contents1 = new ArrayList<String>();

        streams = new ArrayList<InputStream>();

        store = EasyMock.createMock("ContentStore", ContentStore.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(store);

        for (InputStream stream : streams) {
            stream.close();
        }
    }

    private void replayMocks() {
        EasyMock.replay(store);
    }

    @Test
    public void testGetNextContentItem() throws Exception {
        List<ContentType> types0 = new ArrayList<ContentType>();
        types0.add(BASIC);
        types0.add(BASIC);
        types0.add(MANIFEST);
        types0.add(CHUNK);
        types0.add(BASIC);
        types0.add(CHUNK);
        types0.add(BASIC);

        List<ContentType> types1 = new ArrayList<ContentType>();
        types1.add(MANIFEST);
        types1.add(BASIC);
        types1.add(BASIC);
        types1.add(CHUNK);
        types1.add(CHUNK);
        types1.add(BASIC);
        types1.add(BASIC);

        createGetNextContentItemMocks(types0, types1);
        replayMocks();

        retrievalSource = new DuraStoreStitchingRetrievalSource(store,
                                                                spaces,
                                                                allSpaces);
        verifyContents(types0, spaceId0);
        verifyContents(types1, spaceId1);

        ContentItem item = retrievalSource.getNextContentItem();
        Assert.assertNull(item);
    }

    private void verifyContents(List<ContentType> types0, String spaceId) {
        ContentItem item;
        int i = 0;
        for (ContentType type : types0) {
            String contentId = getContentId(i);
            switch (type) {
                case CHUNK:
                    // chunks should be filtered out.
                    break;
                case MANIFEST:
                    contentId += ChunksManifest.manifestSuffix;
                    // fall through...
                default:
                    item = retrievalSource.getNextContentItem();

                    Assert.assertNotNull(item);
                    Assert.assertEquals(spaceId, item.getSpaceId());
                    Assert.assertEquals(contentId, item.getContentId());
            }
            i++;
        }
    }

    private String getContentId(int i) {
        return contentIdBase + i;
    }

    private void createGetNextContentItemMocks(List<ContentType> types0,
                                               List<ContentType> types1)
        throws ContentStoreException {
        contents0.addAll(createContents(types0));
        EasyMock.expect(store.getSpaceContents(spaceId0))
            .andReturn(contents0.iterator());

        contents1.addAll(createContents(types1));
        EasyMock.expect(store.getSpaceContents(spaceId1))
            .andReturn(contents1.iterator());
    }

    private List<String> createContents(List<ContentType> types) {
        List<String> contents = new ArrayList<String>();
        for (int i = 0; i < types.size(); ++i) {
            contents.add(types.get(i).getContentId(i));
        }
        return contents;
    }

    @Test
    public void testGetSourceChecksum() throws Exception {
        final String contentId = getContentId(0);
        final String expectedMd5 = createMd5(0);
        createGetSourceChecksumMocks(contentId, expectedMd5);
        replayMocks();

        retrievalSource = new DuraStoreStitchingRetrievalSource(store,
                                                                spaces,
                                                                allSpaces);

        ContentItem item = new ContentItem(spaceId0, contentId);
        String md5 = retrievalSource.getSourceChecksum(item);

        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);
    }

    private void createGetSourceChecksumMocks(String contentId, String md5)
        throws ContentStoreException {
        Map<String, String> props = new HashMap<String, String>();
        props.put(ContentStore.CONTENT_CHECKSUM, md5);

        EasyMock.expect(store.getContentProperties(spaceId0, contentId))
            .andReturn(props);
    }

    @Test
    public void testGetSourceContent() throws Exception {
        List<ContentType> types = new ArrayList<ContentType>();
        types.add(BASIC);
        types.add(BASIC);
        types.add(MANIFEST);
        types.add(CHUNK);
        types.add(BASIC);
        types.add(CHUNK);
        types.add(BASIC);

        createGetSourceContentMocks(types);
        replayMocks();

        retrievalSource = new DuraStoreStitchingRetrievalSource(store,
                                                                spaces,
                                                                allSpaces);
        int i = 0;
        for (ContentType type : types) {
            ContentItem item = new ContentItem(spaceId0, type.getContentId(i));

            // chunks will not be retrieved directly.
            if (CHUNK != type) {
                ContentStream stream = retrievalSource.getSourceContent(item);
                Assert.assertNotNull(stream);

                String md5 = stream.getChecksum();
                Assert.assertNotNull("item: " + item, md5);
                Assert.assertEquals(createMd5(i), md5);

                InputStream inputStream = stream.getStream();
                Assert.assertNotNull(inputStream);

                while (inputStream.read() != -1) {
                    // spin through the content.
                }
            }
            i++;
        }

    }

    private void createGetSourceContentMocks(List<ContentType> types)
        throws ContentStoreException {
        // variable to hold manifest details.
        ChunksManifest manifest = null;
        String manifestId = null;
        List<Integer> chunkIndexes = new ArrayList<Integer>();

        int i = 0;
        for (ContentType type : types) {
            switch (type) {
                case MANIFEST:
                    manifestId = MANIFEST.getContentId(i);
                    manifest = new ChunksManifest(getContentId(i),
                                                  "text/plain",
                                                  99);
                    manifest.setMD5OfSourceContent(createMd5(i));
                    break;

                case CHUNK:
                    chunkIndexes.add(i);
                    // fall through...
                default:
                    Content content = createContent(type, i);
                    EasyMock.expect(store.getContent(spaceId0,
                                                     type.getContentId(i)))
                        .andReturn(content);
            }
            i++;
        }

        // build the manifest now that chunk details are known.
        for (Integer index : chunkIndexes) {
            manifest.addEntry(CHUNK.getContentId(index), createMd5(index), 77);
        }

        String text = ManifestDocumentBinding.createDocumentFrom(manifest);

        Content content = new Content();
        content.setId(manifestId);
        content.setStream(createStream(text));

        EasyMock.expect(store.getContent(spaceId0, manifestId)).andReturn(
            content);
    }

    private String createMd5(int i) {
        return "md5-" + i;
    }

    private Content createContent(ContentType type, int i)
        throws ContentStoreException {
        Map<String, String> props = new HashMap<String, String>();
        props.put(ContentStore.CONTENT_CHECKSUM, createMd5(i));

        Content content = new Content();
        content.setId(type.getContentId(i));
        content.setProperties(props);
        content.setStream(createStream(type, i));
        return content;
    }

    private InputStream createStream(ContentType type, int i)
        throws ContentStoreException {
        if (type == MANIFEST) {
            Assert.fail("manifests not expected: " + i);
        }
        return createStream("hello-" + i);
    }

    private InputStream createStream(String text) {
        InputStream stream = new ByteArrayInputStream(text.getBytes());
        streams.add(stream);
        return stream;
    }

    /**
     * This inner class helps define types of test content items.
     */
    protected enum ContentType {
        BASIC {
            public String getContentId(int i) {
                return contentIdBase + i;
            }
        }, MANIFEST {
            public String getContentId(int i) {
                return contentIdBase + i + ChunksManifest.manifestSuffix;
            }
        }, CHUNK {
            public String getContentId(int i) {
                return contentIdBase + i + ChunksManifest.chunkSuffix + i;
            }
        };

        public abstract String getContentId(int i);
    }
}
