/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.junit.Assert.assertEquals;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 7/29/15
 */
public class CompleteRestoreTaskRunnerTest {

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3 s3Client;
    private CompleteRestoreTaskRunner taskRunner;

    @Before
    public void setup() {
        snapshotProvider = EasyMock.createMock("StorageProvider",
                                               StorageProvider.class);
        unwrappedSnapshotProvider =
            EasyMock.createMock("SnapshotStorageProvider",
                                SnapshotStorageProvider.class);
        s3Client = EasyMock.createMock("AmazonS3", AmazonS3.class);
        taskRunner = new CompleteRestoreTaskRunner(snapshotProvider,
                                                   unwrappedSnapshotProvider,
                                                   s3Client);
    }

    private void replayMocks() {
        EasyMock.replay(snapshotProvider, unwrappedSnapshotProvider, s3Client);
    }

    @After
    public void tearDown() {
        EasyMock.verify(snapshotProvider, unwrappedSnapshotProvider, s3Client);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("complete-restore", taskRunner.getName());
    }

    @Test
    public void testPerformTask() {
        String spaceId = "space-id";
        String bucketName = "bucket-name";
        int daysToExpire = 42;

        EasyMock.expect(unwrappedSnapshotProvider.getBucketName(spaceId))
                .andReturn(bucketName);
        Capture<BucketLifecycleConfiguration> lifecycleConfigCapture =
            Capture.newInstance(CaptureType.FIRST);
        s3Client.setBucketLifecycleConfiguration(EasyMock.eq(bucketName),
                                                 EasyMock.capture(
                                                     lifecycleConfigCapture));
        EasyMock.expectLastCall().once();

        replayMocks();

        taskRunner.performTask("{\"spaceId\":\"" + spaceId + "\"," +
                               "\"daysToExpire\":" + daysToExpire + "}");
        BucketLifecycleConfiguration lifecycleConfig =
            lifecycleConfigCapture.getValue();
        BucketLifecycleConfiguration.Rule rule =
            lifecycleConfig.getRules().get(0);
        assertEquals(daysToExpire, rule.getExpirationInDays());
        assertEquals("clear-content-rule", rule.getId());
        assertEquals("Enabled", rule.getStatus());
    }

}
