/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.storage.util.StorageProviderFactory;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods Date: 8/17/11
 */
public class ContentResourceImplTest {

    private ContentResource contentResource;
    private StorageProviderFactory storageProviderFactory;
    private StorageProvider storageProvider;
    private StorageProvider destStorageProvider;

    @Before
    public void setUp() throws Exception {
        storageProviderFactory =
            EasyMock.createMock("StorageProviderFactory",
                                StorageProviderFactory.class);
        storageProvider =
            EasyMock.createMock("StorageProvider", StorageProvider.class);

        destStorageProvider =
            EasyMock.createMock("DestStorageProvider", StorageProvider.class);

    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(storageProviderFactory,
                        storageProvider,
                        destStorageProvider);
    }

    private void replayMocks() {
        EasyMock.replay(storageProviderFactory,
                        storageProvider,
                        destStorageProvider);
    }

    @Test
    public void testLocalCopyContent() throws Exception {
        String srcSpaceId = "src-space-id";
        String srcContentId = "src-content-id";
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";
        String storeId = "2";
        String expectedMd5 = "md5";

        createLocalCopyContentMocks(srcSpaceId,
                                    srcContentId,
                                    destSpaceId,
                                    destContentId,
                                    storeId,
                                    expectedMd5);
        replayMocks();

        contentResource = new ContentResourceImpl(storageProviderFactory);
        String md5 =
            contentResource.copyContent(storeId,
                                        srcSpaceId,
                                        srcContentId,
                                        storeId,
                                        destSpaceId,
                                        destContentId);

        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);
    }

    private void createLocalCopyContentMocks(String srcSpaceId,
                                             String srcContentId,
                                             String destSpaceId,
                                             String destContentId,
                                             String storeId,
                                             String expectedMd5) {

        EasyMock.expect(storageProviderFactory.getStorageProvider(storeId))
                .andReturn(storageProvider);

        EasyMock.expect(storageProvider.copyContent(srcSpaceId,
                                                    srcContentId,
                                                    destSpaceId,
                                                    destContentId))
                .andReturn(expectedMd5);
    }

    @Test
    public void testInterProviderCopyContent() throws Exception {
        String srcStoreId = "0";
        String srcSpaceId = "src-space-id";
        String srcContentId = "src-content-id";
        String destStoreId = "1";
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";
        String expectedMd5 = "md5";

        createInterProviderCopyContentMocks(srcStoreId,
                                            srcSpaceId,
                                            srcContentId,
                                            destStoreId,
                                            destSpaceId,
                                            destContentId,
                                            expectedMd5);
        replayMocks();

        contentResource = new ContentResourceImpl(storageProviderFactory);
        String md5 =
            contentResource.copyContent(srcStoreId,
                                        srcSpaceId,
                                        srcContentId,
                                        destStoreId,
                                        destSpaceId,
                                        destContentId);

        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);
    }

    private void createInterProviderCopyContentMocks(String srcStoreId,
                                                     String srcSpaceId,
                                                     String srcContentId,
                                                     String destStoreId,
                                                     String destSpaceId,
                                                     String destContentId,
                                                     String expectedMd5) {

        EasyMock.expect(storageProviderFactory.getStorageProvider(srcStoreId))
                .andReturn(storageProvider);

        InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                // TODO Auto-generated method stub
                return 0;
            }
        };

        EasyMock.expect(storageProvider.getContent(srcSpaceId, srcContentId))
                .andReturn(is);

        Map<String, String> map = new HashMap<String, String>();
        map.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, "12345");
        map.put(StorageProvider.PROPERTIES_CONTENT_SIZE, "1000");
        map.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE, "text/plain");

        EasyMock.expect(storageProvider.getContentProperties(srcSpaceId,
                                                             srcContentId))
                .andReturn(map);

        EasyMock.expect(storageProviderFactory.getStorageProvider(destStoreId))
                .andReturn(destStorageProvider);

        EasyMock.expect(destStorageProvider.addContent(destSpaceId,
                                                       destContentId,
                                                       map.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE),
                                                       map,
                                                       1000,
                                                       map.get(StorageProvider.PROPERTIES_CONTENT_CHECKSUM),
                                                       is))
                .andReturn(expectedMd5);
    }
}
