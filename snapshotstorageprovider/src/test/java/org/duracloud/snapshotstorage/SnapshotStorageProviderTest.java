/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshotstorage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bill Branan
 *         Date: 1/28/14
 */
public class SnapshotStorageProviderTest {

    private SnapshotStorageProvider provider;
    private AmazonS3Client s3Client;

    // Must be 20 char alphanum (and lowercase, to match bucket naming pattern)
    private static final String accessKey = "abcdefghijklmnopqrst";
    private static final String spaceId = "space-id";

    @Before
    public void setup() {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        provider = new SnapshotStorageProvider(s3Client, accessKey, null);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(s3Client);
    }

    private void replayMocks() {
        EasyMock.replay(s3Client);
    }

    @Test
    public void testCreateSnapshotProvider() throws Exception {
        List<Bucket> buckets = new ArrayList<>();
        buckets.add(new Bucket(accessKey + "." + spaceId));
        EasyMock.expect(s3Client.listBuckets()).andReturn(buckets).anyTimes();

        replayMocks();

        String bucketName = provider.getBucketName(spaceId);
        Assert.assertEquals(accessKey + "." + spaceId, bucketName);
    }
}
