/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Bill Branan
 *         Date: 8/14/14
 */
public class CleanupSnapshotTaskRunnerTest {

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3Client s3Client;
    private CleanupSnapshotTaskRunner taskRunner;

    @Before
    public void setup() {
        snapshotProvider = EasyMock.createMock("StorageProvider",
                                               StorageProvider.class);
        unwrappedSnapshotProvider =
            EasyMock.createMock("SnapshotStorageProvider",
                                SnapshotStorageProvider.class);
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        taskRunner = new CleanupSnapshotTaskRunner(snapshotProvider,
                                                   unwrappedSnapshotProvider,
                                                   s3Client);
    }

    private void replayMocks() {
        EasyMock.replay(snapshotProvider, unwrappedSnapshotProvider, s3Client);
    }

    @After
    public void tearDown(){
        EasyMock.verify(snapshotProvider, unwrappedSnapshotProvider, s3Client);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("cleanup-snapshot", taskRunner.getName());
    }

    @Test
    public void testPerformTask() {
        String spaceId = "space-id";
        String bucketName = "bucket-name";

        EasyMock.expect(unwrappedSnapshotProvider.getBucketName(spaceId))
                .andReturn(bucketName);
        Capture<BucketLifecycleConfiguration> lifecycleConfigCapture =
            new Capture<>();
        s3Client.setBucketLifecycleConfiguration(EasyMock.eq(bucketName),
                                                 EasyMock.capture(
                                                     lifecycleConfigCapture));
        EasyMock.expectLastCall();

        replayMocks();

        taskRunner.performTask("{\"spaceId\":\""+spaceId+"\"}");
        BucketLifecycleConfiguration lifecycleConfig =
            lifecycleConfigCapture.getValue();
        BucketLifecycleConfiguration.Rule rule =
            lifecycleConfig.getRules().get(0);
        assertEquals(1, rule.getExpirationInDays());
        assertEquals("clear-content-rule", rule.getId());
        assertEquals("Enabled", rule.getStatus());
    }

}
