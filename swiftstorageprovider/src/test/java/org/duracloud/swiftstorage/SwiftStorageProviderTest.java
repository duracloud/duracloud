/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duracloud.swiftstorage;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

import com.amazonaws.services.s3.AmazonS3;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Test;

/**
 *
 * @author fostera
 */

public class SwiftStorageProviderTest {

    private AmazonS3 s3Client;

    // In OpenStack, access keys are 32 bytes in length.
    private static final String accessKey = "c09417d8d454dff21664a30f1e734149";
    private static final String secretKey = "secretKey";
    private static final String contentId = "content-id";
    private static final String spaceId = "space-id";

    private void setupS3Client() {
        s3Client = createMock("AmazonS3", AmazonS3.class);
    }

    @Test
    public void testGetStorageProviderType() {
        SwiftStorageProvider provider = new SwiftStorageProvider(accessKey, secretKey, null);
        assertEquals(StorageProviderType.SWIFT_S3, provider.getStorageProviderType());
    }

    @Test
    public void testGetNewBucketName() {
        SwiftStorageProvider provider = new SwiftStorageProvider(accessKey, secretKey, null);
        String bucketName = provider.getNewBucketName(spaceId);
        assertEquals(bucketName, "c09417d8d454dff21664.space-id");
    }
}