/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.azurestorage;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.soyatec.windowsazure.blob.BlobStorageClient;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.internal.BlobContainer;
import org.soyatec.windowsazure.blob.internal.BlobProperties;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

/**
 * @author: Bill Branan
 * Date: 6/15/11
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( {BlobStorageClient.class, BlobContainer.class} )
public class AzureStorageProviderTest {

     private String spaceId = "space-id";
     private String contentId1 = "content-1";
     private String contentId2 = "content-2";
     private String contentId3 = "content-3";
     private int maxResults = 100;

     private BlobStorageClient blobStorage;
     private BlobContainer blobContainer;

    @Before
    public void setup() {
        blobStorage = createMock(BlobStorageClient.class);
        blobContainer = createMock(BlobContainer.class);
    }

    @After
    public void teardown() {
        verify(blobStorage, blobContainer);
    }

    @Test
    public void testGetSpaceContentsChunked() {
        setUpMocks();

        AzureStorageProvider provider = new AzureStorageProvider(blobStorage);

        List<String> contentIds = provider.getSpaceContentsChunked(spaceId,
                                                                   null,
                                                                   maxResults,
                                                                   null);
        assertNotNull(contentIds);
        assertEquals(3, contentIds.size());
        assertTrue(contentIds.contains(contentId1));
        assertTrue(contentIds.contains(contentId2));
        assertTrue(contentIds.contains(contentId3));
    }

    @Test
    public void testGetSpaceContentsChunkedMarker() {
        setUpMocks();

        AzureStorageProvider provider = new AzureStorageProvider(blobStorage);

        List<String> contentIds = provider.getSpaceContentsChunked(spaceId,
                                                                   null,
                                                                   maxResults,
                                                                   contentId2);
        assertNotNull(contentIds);
        assertEquals(1, contentIds.size());
        assertTrue(contentIds.contains(contentId3));
    }

    @Test
    public void testGetSpaceContentsChunkedInvalidMarker() {
        setUpMocks();

        AzureStorageProvider provider = new AzureStorageProvider(blobStorage);

        List<String> contentIds = provider.getSpaceContentsChunked(spaceId,
                                                                   null,
                                                                   maxResults,
                                                                   "unknown");
        assertNotNull(contentIds);
        assertEquals(0, contentIds.size());
    }

    private void setUpMocks() {
        EasyMock.expect(blobStorage.isContainerExist(spaceId))
            .andReturn(true)
            .times(1);
        EasyMock.expect(blobStorage.getBlobContainer(spaceId))
            .andReturn(blobContainer)
            .times(1);

        List<IBlobProperties> blobProps = new ArrayList<IBlobProperties>();
        blobProps.add(new BlobProperties(contentId1));
        blobProps.add(new BlobProperties(contentId2));
        blobProps.add(new BlobProperties(contentId3));

        EasyMock.expect(blobContainer.listBlobs(EasyMock.isA(String.class),
                                                EasyMock.eq(false),
                                                EasyMock.eq(maxResults)))
            .andReturn(blobProps.iterator())
            .times(1);

        replay(blobStorage, blobContainer);
    }

    @Test
    public void testCopyContent() {
        setUpCopyContentMocks();

        AzureStorageProvider provider = new AzureStorageProvider(blobStorage);

        String srcSpaceId = "spaceId";
        String srcContentId = "contentId";
        String destSpaceId = "destSpaceId";
        String destContentId = "destContentId";
        String md5 = provider.copyContent(srcSpaceId,
                                          srcContentId,
                                          destSpaceId,
                                          destContentId);

        Assert.assertNotNull(md5);
        Assert.assertEquals("no-md5-guarantees", md5);
    }

    private void setUpCopyContentMocks() {
        EasyMock.expect(blobStorage.getBlobContainer(EasyMock.<String>anyObject()))
            .andReturn(blobContainer)
            .times(2);

        EasyMock.expect(blobStorage.isContainerExist(EasyMock.<String>anyObject()))
            .andReturn(true);

        EasyMock.expect(blobContainer.isBlobExist(EasyMock.<String>anyObject()))
            .andReturn(true);

        EasyMock.expect(blobContainer.getName()).andReturn("container-name");
        EasyMock.expect(blobContainer.copyBlob(EasyMock.<String>anyObject(),
                                               EasyMock.<String>anyObject(),
                                               EasyMock.<String>anyObject()))
            .andReturn(false);
        EasyMock.expect(blobContainer.copyBlob(EasyMock.<String>anyObject(),
                                               EasyMock.<String>anyObject(),
                                               EasyMock.<String>anyObject()))
            .andReturn(true);

        replay(blobStorage, blobContainer);
    }

}
