/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 7/25/14
 */
public class CompleteSnapshotTaskRunnerTest {

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3 s3Client;
    private CompleteSnapshotTaskRunner taskRunner;

    @Before
    public void setup() {
        snapshotProvider = EasyMock.createMock("StorageProvider",
                                               StorageProvider.class);
        unwrappedSnapshotProvider =
            EasyMock.createMock("SnapshotStorageProvider",
                                SnapshotStorageProvider.class);
        s3Client = EasyMock.createMock("AmazonS3", AmazonS3.class);
        taskRunner = new CompleteSnapshotTaskRunner(snapshotProvider,
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
        assertEquals("complete-snapshot", taskRunner.getName());
    }

    @Test
    public void testPerformTask() {
        String spaceId = "space-id";
        String bucketName = "bucket-name";

        EasyMock.expect(unwrappedSnapshotProvider.getBucketName(spaceId))
                .andReturn(bucketName);
        s3Client.deleteBucketLifecycleConfiguration(EasyMock.eq(bucketName));
        EasyMock.expectLastCall().once();

        unwrappedSnapshotProvider.setNewSpaceProperties(EasyMock.eq(spaceId),
                                                        EasyMock.isA(Map.class));
        EasyMock.expectLastCall().once();

        replayMocks();

        taskRunner.performTask("{\"spaceId\":\"" + spaceId + "\"}");
    }

}
