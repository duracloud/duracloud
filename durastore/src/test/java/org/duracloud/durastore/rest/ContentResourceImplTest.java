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

import org.duracloud.durastore.error.ResourceException;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.Capture;
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
    private BrokeredStorageProvider storageProvider;
    private BrokeredStorageProvider destStorageProvider;

    @Before
    public void setUp() throws Exception {
        storageProviderFactory =
            EasyMock.createMock("StorageProviderFactory",
                                StorageProviderFactory.class);
        storageProvider = EasyMock.createMock("BrokeredStorageProvider",
                                              BrokeredStorageProvider.class);

        destStorageProvider = EasyMock.createMock("BrokeredDestStorageProvider",
                                                  BrokeredStorageProvider.class);

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
                .andReturn(storageProvider).times(2);

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
    
    @Test
    public void testDuracloud757() throws ResourceException, InvalidIdException{
        
        EasyMock.expect(storageProviderFactory.getStorageProvider(EasyMock.isA(String.class)))
        .andReturn(storageProvider);

        InputStream is = createEmptyInputStream();
        
        String customPropName = "name", customPropValue = "value";
        
        Map<String, String> map = new HashMap<String, String>();
        map.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, "12345");
        map.put(StorageProvider.PROPERTIES_CONTENT_SIZE, "1000");
        map.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE, "text/plain");
        map.put(StorageProvider.PROPERTIES_CONTENT_MODIFIED, "2013-10-01");
        map.put(customPropName, customPropValue);

        
        EasyMock.expect(storageProvider.getContentProperties(EasyMock.isA(String.class),
                                                             EasyMock.isA(String.class)))
                .andReturn(map);
        String mimetype = "application/pdf";
        String checksum = "23456";
        int size = 1001;
        String storeId = "1";
        Map<String, String> props = new HashMap<String, String>();
        props.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, "12345");
        props.put(StorageProvider.PROPERTIES_CONTENT_SIZE, "1000");
        Capture<Map<String,String>> userProps = new Capture<>();
        Capture<String> finalMimeType = new Capture<>();

        EasyMock.expect(this.storageProvider.addContent(EasyMock.isA(String.class),
                                                        EasyMock.isA(String.class),
                                                        EasyMock.capture(finalMimeType),
                                                        EasyMock.capture(userProps),
                                                        EasyMock.anyLong(),
                                                        EasyMock.isA(String.class),
                                                        EasyMock.isA(InputStream.class)))
                .andReturn("testContent");

        replayMocks();
        this.contentResource = new ContentResourceImpl(storageProviderFactory);

        this.contentResource.addContent("testSpace",
                                        "testContent",
                                        is,
                                        mimetype,
                                        props,
                                        size,
                                        checksum,
                                        storeId);

        Map<String,String> p = userProps.getValue();
        Assert.assertTrue(p.containsKey(customPropName));
        Assert.assertEquals(customPropValue, p.get(customPropName));
        Assert.assertEquals(mimetype, finalMimeType.getValue());
        Assert.assertFalse(p.containsKey(StorageProvider.PROPERTIES_CONTENT_CHECKSUM));
        Assert.assertFalse(p.containsKey(StorageProvider.PROPERTIES_CONTENT_SIZE));
    }

    protected InputStream createEmptyInputStream() {
        InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        return is;
    }
}
