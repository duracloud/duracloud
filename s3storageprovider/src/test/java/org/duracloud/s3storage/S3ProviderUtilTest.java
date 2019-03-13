/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.duracloud.storage.domain.StorageAccount.OPTS;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderUtilTest {

    @Test
    public void testGetBucketName() throws Exception {
        String accessKey = "abc";

        String bucketName = S3ProviderUtil.createNewBucketName(accessKey, "xyz");
        assertEquals("abc.xyz", bucketName);

        bucketName = S3ProviderUtil.createNewBucketName(accessKey,
                                                        "x~!@#$%^&*(){}:;'\"<>,?/|z");
        assertEquals("abc.x-z", bucketName);

        bucketName = S3ProviderUtil.createNewBucketName(accessKey,
                                                        "x--..y..--z-.");
        assertEquals("abc.x-y.z", bucketName);
    }

    @Test
    public void testGetDifferentAmazonS3Clients() {
        String accessKey = "access-key";
        String privateKey = "private-key";

        Map<String, String> optionsA = new HashMap<>();
        optionsA.put(OPTS.AWS_REGION.name(), "us-east-1");

        Map<String, String> optionsB = new HashMap<>();
        optionsB.put(OPTS.AWS_REGION.name(), "us-west-2");

        AmazonS3 s3ClientA = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsA);
        assertEquals("us-east-1", s3ClientA.getRegionName());

        AmazonS3 s3ClientB = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsB);
        assertEquals("us-west-2", s3ClientB.getRegionName());

        assertNotSame(s3ClientA, s3ClientB);
    }

    @Test
    public void testGetSameAmazonS3Clients() {
        String accessKey = "access-key";
        String privateKey = "private-key";

        Map<String, String> optionsA = new HashMap<>();
        optionsA.put(OPTS.AWS_REGION.name(), "us-east-1");

        Map<String, String> optionsB = new HashMap<>();
        optionsB.put(OPTS.AWS_REGION.name(), "us-east-1");

        AmazonS3 s3ClientA = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsA);
        assertEquals("us-east-1", s3ClientA.getRegionName());

        AmazonS3 s3ClientB = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsB);
        assertEquals("us-east-1", s3ClientB.getRegionName());

        assertSame(s3ClientA, s3ClientB);
    }

    @Test
    public void testGetDifferentSwiftS3Clients() {
        String accessKey = "access-key";
        String privateKey = "private-key";

        Map<String, String> optionsA = new HashMap<>();
        optionsA.put(OPTS.SWIFT_S3_ENDPOINT.name(), "https://my.endpoint.com#us-east-1");

        Map<String, String> optionsB = new HashMap<>();
        optionsB.put(OPTS.SWIFT_S3_ENDPOINT.name(), "https://my.other-endpoint.com#us-west-1");

        AmazonS3 s3ClientA = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsA);

        AmazonS3 s3ClientB = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsB);

        assertNotSame(s3ClientA, s3ClientB);
    }

    @Test
    public void testGetSameSwiftS3Clients() {
        String accessKey = "access-key";
        String privateKey = "private-key";

        Map<String, String> optionsA = new HashMap<>();
        optionsA.put(OPTS.SWIFT_S3_ENDPOINT.name(), "https://my.endpoint.com#us-east-1");

        Map<String, String> optionsB = new HashMap<>();
        optionsB.put(OPTS.SWIFT_S3_ENDPOINT.name(), "https://my.endpoint.com#us-east-1");

        AmazonS3 s3ClientA = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsA);

        AmazonS3 s3ClientB = S3ProviderUtil.getAmazonS3Client(accessKey, privateKey, optionsB);

        assertSame(s3ClientA, s3ClientB);
    }
}
