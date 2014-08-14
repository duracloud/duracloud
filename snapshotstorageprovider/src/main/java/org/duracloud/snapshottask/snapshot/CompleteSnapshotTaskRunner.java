/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.CompleteSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.CompleteSnapshotTaskResult;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.TaskRunner;

/**
 * Completes the snapshot process by removing unnecessary bucket-level policies
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class CompleteSnapshotTaskRunner implements TaskRunner {

    private SnapshotStorageProvider snapshotProvider;
    private AmazonS3Client s3Client;

    public CompleteSnapshotTaskRunner(SnapshotStorageProvider snapshotProvider,
                                      AmazonS3Client s3Client) {
        this.snapshotProvider = snapshotProvider;
        this.s3Client = s3Client;
    }

    @Override
    public String getName() {
        return SnapshotConstants.COMPLETE_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        CompleteSnapshotTaskParameters taskParams =
            CompleteSnapshotTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();
        String bucketName = snapshotProvider.getBucketName(spaceId);

        // Remove policy on bucket
        s3Client.deleteBucketLifecycleConfiguration(bucketName);

        String result = "Snapshot complete was successful";
        return new CompleteSnapshotTaskResult(result).serialize();
    }

}
