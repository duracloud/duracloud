/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glacierstorage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageStateException;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Dec 6, 2012
 */
public class GlacierStorageProviderTest {

    private AmazonS3Client s3Client;
    private String accessKey = "access-key";
    private String contentId = "content-id";
    private String spaceId = "space-id";

    // The exception to be thrown because the content item is in Glacier
    private AmazonS3Exception glacierEx;

    @Before
    public void setup() {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        glacierEx = new AmazonS3Exception("err msg");
        glacierEx.setErrorCode(GlacierStorageProvider.INVALID_OBJECT_STATE);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(s3Client);
    }

    @Test
    public void testCreateSpace() {
        // Perform the space creation
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
            .andReturn(false);
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
            .andReturn(true)
            .anyTimes();
        EasyMock.expect(s3Client.createBucket(EasyMock.isA(String.class)))
            .andReturn(new Bucket());

        // Add space properties
        EasyMock.expect(
            s3Client.getBucketTaggingConfiguration(EasyMock.isA(String.class)))
                .andThrow(new NotFoundException(spaceId));
        s3Client.setBucketTaggingConfiguration(
            EasyMock.isA(String.class),
            EasyMock.isA(BucketTaggingConfiguration.class));
        EasyMock.expectLastCall();

        // Space has been created. Now add glacier lifecycle policy.
        Capture<BucketLifecycleConfiguration> lcConfigCap = new Capture<>();
        s3Client.setBucketLifecycleConfiguration(EasyMock.isA(String.class),
                                                 EasyMock.capture(lcConfigCap));
        EasyMock.expectLastCall();
        EasyMock.replay(s3Client);

        // Actually make the call to kick off a space creation
        GlacierStorageProvider provider =
            new GlacierStorageProvider(s3Client, accessKey);
        provider.createSpace(spaceId);

        // Verify the lifecycle policy
        BucketLifecycleConfiguration lcConfig = lcConfigCap.getValue();
        assertNotNull(lcConfig);
        BucketLifecycleConfiguration.Transition transition =
            lcConfig.getRules().get(0).getTransition();
        assertEquals(transition.getDays(), 0);
        assertEquals(transition.getStorageClass(), StorageClass.Glacier);
    }

    @Test
    public void testGetContentFail() {
        // Set up the mocks to throw on the getObject() call
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
                .andReturn(true);

        EasyMock.expect(s3Client.getObject(accessKey + "." + spaceId, contentId))
                .andThrow(glacierEx);

        EasyMock.expect(s3Client.getObjectMetadata(accessKey + "." + spaceId,
                                                   contentId))
                .andReturn(null);

        EasyMock.replay(s3Client);

        GlacierStorageProvider provider =
            new GlacierStorageProvider(s3Client, accessKey);

        try {
            provider.getContent(spaceId, contentId);
            fail("StorageStateException expected");
        } catch(StorageStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testSetContentPropertiesFail() {
        // Set up the mocks to throw on the setObjectProperties() call
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
                .andReturn(true)
                .times(2);

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setUserMetadata(new HashMap<String, String>());
        EasyMock.expect(s3Client.getObjectMetadata(accessKey + "." + spaceId,
                                                   contentId))
                .andReturn(objMeta)
                .times(2);

        EasyMock.expect(
            s3Client.getObjectAcl(accessKey + "." + spaceId, contentId))
                .andReturn(new AccessControlList());

        EasyMock.expect(
            s3Client.copyObject(EasyMock.isA(CopyObjectRequest.class)))
                .andThrow(glacierEx);

        EasyMock.replay(s3Client);

        GlacierStorageProvider provider =
            new GlacierStorageProvider(s3Client, accessKey);

        try {
            Map<String, String> props = new HashMap<>();
            provider.setContentProperties(spaceId, contentId, props);
            fail("StorageStateException expected");
        } catch(StorageStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testCopyContentFail() {
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";

        // Set up the mocks to throw on the copyContent() call
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
                .andReturn(true);

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setUserMetadata(new HashMap<String, String>());
        EasyMock.expect(s3Client.getObjectMetadata(accessKey + "." + spaceId,
                                                   contentId))
                .andReturn(objMeta);

        EasyMock.expect(
            s3Client.copyObject(EasyMock.isA(CopyObjectRequest.class)))
                .andThrow(glacierEx);

        EasyMock.replay(s3Client);

        GlacierStorageProvider provider =
            new GlacierStorageProvider(s3Client, accessKey);

        try {
            provider.copyContent(spaceId, contentId, destSpaceId, destContentId);
            fail("StorageStateException expected");
        } catch(StorageStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

}
