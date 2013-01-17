/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.jclouds.openstack.swift.SwiftClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 8/12/11
 */
public class RackspaceStorageProviderTest {

    private FilesClient filesClient;
    private SwiftClient swiftClient;

    @Before
    public void setUp() throws Exception {
        filesClient = EasyMock.createMock("FilesClient", FilesClient.class);
        swiftClient = EasyMock.createMock("SwiftClient", SwiftClient.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(filesClient, swiftClient);
    }

    private void replayMocks() {
        EasyMock.replay(filesClient, swiftClient);
    }

    @Test
    public void testCopyContent() throws Exception {
        String srcSpaceId = "spaceId";
        String srcContentId = "contentId";
        String destSpaceId = "destSpaceId";
        String destContentId = "destContentId";
        String expectedMD5 = "md5";
        createFilesClientCopyContent(srcSpaceId,
                                     srcContentId,
                                     destSpaceId,
                                     destContentId,
                                     expectedMD5);
        RackspaceStorageProvider provider =
            new RackspaceStorageProvider(filesClient, swiftClient);

        String md5 = provider.copyContent(srcSpaceId,
                                          srcContentId,
                                          destSpaceId,
                                          destContentId);

        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMD5, md5);
    }

    private void createFilesClientCopyContent(String srcSpaceId,
                                              String srcContentId,
                                              String destSpaceId,
                                              String destContentId,
                                              String expectedMD5)
        throws Exception {
        FilesObjectMetaData metadata = new FilesObjectMetaData("", "", "");

        Map<String, String> props = new HashMap<String, String>();
        props.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, expectedMD5);
        metadata.setMetaData(props);

        EasyMock.expect(filesClient.getObjectMetaData(EasyMock.isA(String.class),
                                                      EasyMock.isA(String.class)))
            .andReturn(metadata)
            .times(2);
        EasyMock.expect(filesClient.containerExists(EasyMock.isA(String.class)))
            .andReturn(true)
            .times(2);

        EasyMock.expect(filesClient.copyObject(EasyMock.eq(srcSpaceId),
                                               EasyMock.eq(srcContentId),
                                               EasyMock.eq(destSpaceId),
                                               EasyMock.eq(destContentId)))
            .andReturn(expectedMD5);

        replayMocks();
    }
}
