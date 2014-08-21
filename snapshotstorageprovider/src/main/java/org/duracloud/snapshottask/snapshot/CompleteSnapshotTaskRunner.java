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
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Completes the snapshot process by removing unnecessary bucket-level policies
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class CompleteSnapshotTaskRunner implements TaskRunner {

    private Logger log = LoggerFactory
        .getLogger(CompleteSnapshotTaskRunner.class);

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3Client s3Client;

    public CompleteSnapshotTaskRunner(StorageProvider snapshotProvider,
                                      SnapshotStorageProvider unwrappedSnapshotProvider,
                                      AmazonS3Client s3Client) {
        this.snapshotProvider = snapshotProvider;
        this.unwrappedSnapshotProvider = unwrappedSnapshotProvider;
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
        String bucketName = unwrappedSnapshotProvider.getBucketName(spaceId);

        log.info("Performing Complete Snapshot Task for spaceID: " + spaceId);

        // Remove policy on bucket
        s3Client.deleteBucketLifecycleConfiguration(bucketName);

        // Clear space properties (removes snapshot ID and other props)
        unwrappedSnapshotProvider
            .setNewSpaceProperties(spaceId, new HashMap<String, String>());

        log.info("Complete Snapshot Task for space " + spaceId +
                 " completed successfully");

        String result = "Snapshot complete was successful";
        return new CompleteSnapshotTaskResult(result).serialize();
    }

}
