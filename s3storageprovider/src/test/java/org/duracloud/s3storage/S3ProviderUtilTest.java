/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderUtilTest {

    @Test
    public void testGetBucketName() throws Exception {
        String accessKey = "abc";

        String bucketName = S3ProviderUtil.getBucketName(accessKey, "xyz");
        assertEquals("abc.xyz", bucketName);

        bucketName = S3ProviderUtil.getBucketName(accessKey,
                                                  "x~!@#$%^&*(){}:;'\"<>,?/|z");
        assertEquals("abc.x-z", bucketName);

        bucketName = S3ProviderUtil.getBucketName(accessKey, "x--..y..--z-.");
        assertEquals("abc.x-y.z", bucketName);
    }
}
