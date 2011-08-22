/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.durastore.util.StorageProviderFactory;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 8/17/11
 */
public class ContentResourceImplTest {

    private ContentResource contentResource;
    private StorageProviderFactory storageProviderFactory;
    private StorageProvider storageProvider;


    @Before
    public void setUp() throws Exception {
        storageProviderFactory = EasyMock.createMock("StorageProviderFactory",
                                                     StorageProviderFactory.class);
        storageProvider = EasyMock.createMock("StorageProvider",
                                              StorageProvider.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(storageProviderFactory, storageProvider);
    }

    private void replayMocks() {
        EasyMock.replay(storageProviderFactory, storageProvider);
    }

    @Test
    public void testCopyContent() throws Exception {
        String srcSpaceId = "src-space-id";
        String srcContentId = "src-content-id";
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";
        String storeId = "2";
        String expectedMd5 = "md5";

        createCopyContentMocks(srcSpaceId,
                               srcContentId,
                               destSpaceId,
                               destContentId,
                               storeId,
                               expectedMd5);
        replayMocks();

        contentResource = new ContentResourceImpl(storageProviderFactory);
        String md5 = contentResource.copyContent(srcSpaceId,
                                                 srcContentId,
                                                 destSpaceId,
                                                 destContentId,
                                                 storeId);

        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);
    }

    private void createCopyContentMocks(String srcSpaceId,
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
                                                    destContentId)).andReturn(
            expectedMd5);
    }
}
