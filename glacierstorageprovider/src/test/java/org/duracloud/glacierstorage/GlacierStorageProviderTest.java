/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glacierstorage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.storage.error.NotFoundException;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: Dec 6, 2012
 */
public class GlacierStorageProviderTest {

    private AmazonS3Client s3Client;

    @Before
    public void setup() {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(s3Client);
    }

    @Test
    public void testCreateSpace() {
        String spaceId = "my-space";

        // Perform the space creation
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
            .andReturn(false);
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
            .andReturn(true)
            .anyTimes();
        EasyMock.expect(s3Client.createBucket(EasyMock.isA(String.class)))
            .andReturn(new Bucket());

        // Add space metadata file
        EasyMock.expect(s3Client.getObject(EasyMock.isA(String.class),
                                           EasyMock.isA(String.class)))
            .andThrow(new NotFoundException(spaceId));
        PutObjectResult putSpaceMetadataResult = new PutObjectResult();
        // This allows the space metadata checksum check to pass
        putSpaceMetadataResult.setETag("d41d8cd98f00b204e9800998ecf8427e");
        EasyMock.expect(s3Client.putObject(EasyMock.isA(PutObjectRequest.class)))
            .andReturn(putSpaceMetadataResult);

        // Space has been created. Now add glacier lifecycle policy.
        Capture<BucketLifecycleConfiguration> lcConfigCap = new Capture<>();
        s3Client.setBucketLifecycleConfiguration(EasyMock.isA(String.class),
                                                 EasyMock.capture(lcConfigCap));
        EasyMock.expectLastCall();
        EasyMock.replay(s3Client);

        // Actually make the call to kick off a space creation
        GlacierStorageProvider provider =
            new GlacierStorageProvider(s3Client, "accessKey");
        provider.createSpace(spaceId);

        // Verify the lifecycle policy
        BucketLifecycleConfiguration lcConfig = lcConfigCap.getValue();
        assertNotNull(lcConfig);
        BucketLifecycleConfiguration.Transition transition =
            lcConfig.getRules().get(0).getTransition();
        assertEquals(transition.getDays(), 0);
        assertEquals(transition.getStorageClass(), StorageClass.Glacier);
    }

}
