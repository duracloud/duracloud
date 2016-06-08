/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.EasyMock;
import org.jclouds.openstack.swift.SwiftClient;
import org.jclouds.openstack.swift.domain.MutableObjectInfoWithMetadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * @author Andrew Woods
 *         Date: 8/12/11
 */
public class RackspaceStorageProviderTest {

    private SwiftClient swiftClient;

    @Before
    public void setUp() throws Exception {
        swiftClient = EasyMock.createMock("SwiftClient", SwiftClient.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify( swiftClient);
    }

    @Test
    public void testGetStorageProviderType() {
        EasyMock.replay(swiftClient);

        RackspaceStorageProvider provider =
            new RackspaceStorageProvider("accessKey", "secretKey");
        assertEquals(StorageProviderType.RACKSPACE, provider.getStorageProviderType());
    }

    @Test
    public void testCopyContent() throws Exception {
        String srcSpaceId = "spaceId";
        String srcContentId = "contentId";
        String destSpaceId = "destSpaceId";
        String destContentId = "destContentId";
        String expectedMD5 = "abc123";//"abcdef123456789";

        createFilesClientCopyContent(srcSpaceId,
                                     srcContentId,
                                     destSpaceId,
                                     destContentId,
                                     expectedMD5);
        RackspaceStorageProvider provider =
            new RackspaceStorageProvider(swiftClient);

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

        byte[] expectedMD5Hash = ChecksumUtil.hexStringToByteArray(expectedMD5);
        MutableObjectInfoWithMetadata objectInfoWithMetadata =
                EasyMock.createMock(MutableObjectInfoWithMetadata.class);
        EasyMock.expect(objectInfoWithMetadata.getHash()).andReturn(expectedMD5Hash).times(2);
        EasyMock.expect(objectInfoWithMetadata.getLastModified()).andReturn(new Date());
        EasyMock.expect(objectInfoWithMetadata.getBytes()).andReturn(12L);
        EasyMock.expect(objectInfoWithMetadata.getMetadata()).andReturn(new HashMap<String, String>());
        EasyMock.expect(swiftClient.getObjectInfo(EasyMock.isA(String.class),
                EasyMock.isA(String.class)))
            .andReturn(objectInfoWithMetadata)
            .times(2);

        EasyMock.expect(swiftClient.objectExists(
                EasyMock.isA(String.class), EasyMock.isA(String.class)))
                .andReturn(true);
        EasyMock.expect(swiftClient.containerExists(EasyMock.isA(String.class)))
            .andReturn(true)
            .times(2);

        EasyMock.expect(swiftClient.copyObject(EasyMock.eq(srcSpaceId),
                EasyMock.eq(srcContentId),
                EasyMock.eq(destSpaceId),
                EasyMock.eq(destContentId)))
                .andReturn(true);

        EasyMock.replay(swiftClient, objectInfoWithMetadata);
    }
}
