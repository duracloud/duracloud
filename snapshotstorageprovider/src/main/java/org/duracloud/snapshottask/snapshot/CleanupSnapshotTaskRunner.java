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
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.CleanupSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.CleanupSnapshotTaskResult;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Cleans up the snapshot by removing content that is no longer
 * needed now that the snapshot has been transferred successfully.
 *
 *
 * @author Bill Branan
 *         Date: 8/14/14
 */
public class CleanupSnapshotTaskRunner implements TaskRunner {

    private Logger log =
        LoggerFactory.getLogger(CleanupSnapshotTaskRunner.class);

    private static int EXPIRATION_DAYS = 1;

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3Client s3Client;

    public CleanupSnapshotTaskRunner(StorageProvider snapshotProvider,
                                     SnapshotStorageProvider unwrappedSnapshotProvider,
                                     AmazonS3Client s3Client) {
        this.snapshotProvider = snapshotProvider;
        this.unwrappedSnapshotProvider = unwrappedSnapshotProvider;
        this.s3Client = s3Client;
    }

    @Override
    public String getName() {
        return SnapshotConstants.CLEANUP_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        CleanupSnapshotTaskParameters taskParams =
            CleanupSnapshotTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();
        String bucketName = unwrappedSnapshotProvider.getBucketName(spaceId);

        log.info("Performing Cleanup Snapshot Task for spaceID: " + spaceId);

        // Create bucket deletion policy
        BucketLifecycleConfiguration.Rule expireRule =
            new BucketLifecycleConfiguration.Rule()
                .withId("clear-content-rule")
                .withExpirationInDays(EXPIRATION_DAYS)
                .withStatus(BucketLifecycleConfiguration.ENABLED.toString());

        List<BucketLifecycleConfiguration.Rule> rules = new ArrayList<>();
        rules.add(expireRule);

        BucketLifecycleConfiguration configuration =
            new BucketLifecycleConfiguration().withRules(rules);

        // Set policy on bucket
        s3Client.setBucketLifecycleConfiguration(bucketName, configuration);

        log.info("Cleanup Snapshot Task for space " + spaceId +
                 " completed successfully");

        return new CleanupSnapshotTaskResult(EXPIRATION_DAYS).serialize();
    }

}
