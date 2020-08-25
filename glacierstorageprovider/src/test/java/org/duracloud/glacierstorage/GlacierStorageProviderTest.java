/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glacierstorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageStateException;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Dec 6, 2012
 */
public class GlacierStorageProviderTest {

    private AmazonS3 s3Client;

    // Must be 20 char alphanum (and lowercase, to match bucket naming pattern)
    private static final String accessKey = "abcdefghijklmnopqrst";

    private static final String contentId = "content-id";
    private static final String spaceId = "space-id";

    // The exception to be thrown because the content item is in Glacier
    private AmazonS3Exception glacierEx;

    @Before
    public void setup() {
        s3Client = EasyMock.createMock("AmazonS3", AmazonS3.class);
        glacierEx = new AmazonS3Exception("err msg");
        glacierEx.setErrorCode(GlacierStorageProvider.INVALID_OBJECT_STATE);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(s3Client);
    }

    @Test
    public void testGetStorageProviderType() {
        EasyMock.replay(s3Client);

        GlacierStorageProvider provider =
            new GlacierStorageProvider("accessKey", "secretKey");
        assertEquals(StorageProviderType.AMAZON_GLACIER,
                     provider.getStorageProviderType());
    }

    @Test
    public void testCreateSpace() {
        List<Bucket> emptyBuckets = new ArrayList<>();
        EasyMock.expect(s3Client.listBuckets())
                .andReturn(emptyBuckets);
        setListBucketsMock();

        // Perform the space creation
        EasyMock.expect(s3Client.createBucket(EasyMock.isA(String.class)))
                .andReturn(new Bucket());

        // Add space properties
        EasyMock.expect(
            s3Client.getBucketTaggingConfiguration(EasyMock.isA(String.class)))
                .andThrow(new NotFoundException(spaceId));
        s3Client.setBucketTaggingConfiguration(
            EasyMock.isA(String.class),
            EasyMock.isA(BucketTaggingConfiguration.class));
        EasyMock.expectLastCall().once();

        // Space has been created. Now add glacier lifecycle policy.
        s3Client.deleteBucketLifecycleConfiguration(EasyMock.isA(String.class));
        EasyMock.expectLastCall().once();

        Capture<BucketLifecycleConfiguration> lcConfigCap =
            Capture.newInstance(CaptureType.FIRST);
        s3Client.setBucketLifecycleConfiguration(EasyMock.isA(String.class),
                                                 EasyMock.capture(lcConfigCap));
        EasyMock.expectLastCall().once();
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

    private void setListBucketsMock() {
        List<Bucket> buckets = new ArrayList<>();
        buckets.add(new Bucket(accessKey + "." + spaceId));
        buckets.add(new Bucket(accessKey + ".dest-space-id"));
        EasyMock.expect(s3Client.listBuckets())
                .andReturn(buckets)
                .anyTimes();
    }

    @Test
    public void testGetContentFail() {
        setListBucketsMock();

        // Set up the mocks to throw on the getObject() call
        EasyMock.expect(s3Client.getObject(EasyMock.isA(GetObjectRequest.class)))
                .andThrow(glacierEx);

        EasyMock.expect(s3Client.getObjectMetadata(accessKey + "." + spaceId,
                                                   contentId))
                .andReturn(null);

        EasyMock.replay(s3Client);

        GlacierStorageProvider provider =
            new GlacierStorageProvider(s3Client, accessKey);

        try {
            provider.getContent(spaceId, contentId, null);
            fail("StorageStateException expected");
        } catch (StorageStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testSetContentPropertiesFail() {
        setListBucketsMock();

        // Set up the mocks to throw on the setObjectProperties() call
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
        } catch (StorageStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testCopyContentFail() {
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";

        setListBucketsMock();

        // Set up the mocks to throw on the copyContent() call
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
        } catch (StorageStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

}
