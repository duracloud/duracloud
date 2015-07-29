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
import org.duracloud.snapshot.dto.task.CompleteRestoreTaskParameters;
import org.duracloud.snapshot.dto.task.CompleteSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.CompleteSnapshotTaskResult;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Completes the restore process by setting bucket-level policies to ensure
 * restored content is removed as it expires.
 *
 * @author Bill Branan
 *         Date: 7/29/15
 */
public class CompleteRestoreTaskRunner implements TaskRunner {

    private Logger log = LoggerFactory
        .getLogger(CompleteRestoreTaskRunner.class);

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3Client s3Client;

    public CompleteRestoreTaskRunner(StorageProvider snapshotProvider,
                                     SnapshotStorageProvider unwrappedSnapshotProvider,
                                     AmazonS3Client s3Client) {
        this.snapshotProvider = snapshotProvider;
        this.unwrappedSnapshotProvider = unwrappedSnapshotProvider;
        this.s3Client = s3Client;
    }

    @Override
    public String getName() {
        return SnapshotConstants.COMPLETE_RESTORE_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        CompleteRestoreTaskParameters taskParams =
            CompleteRestoreTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();
        int daysToExpire = taskParams.getDaysToExpire();
        String bucketName = unwrappedSnapshotProvider.getBucketName(spaceId);

        log.info("Performing Complete Restore Task for spaceID: " + spaceId +
                 ". Restored content will expire in " + daysToExpire + " days");

        // Create bucket deletion policy
        BucketLifecycleConfiguration.Rule expireRule =
            new BucketLifecycleConfiguration.Rule()
                .withId("clear-content-rule")
                .withExpirationInDays(daysToExpire)
                .withStatus(BucketLifecycleConfiguration.ENABLED.toString());

        List<BucketLifecycleConfiguration.Rule> rules = new ArrayList<>();
        rules.add(expireRule);

        BucketLifecycleConfiguration configuration =
            new BucketLifecycleConfiguration().withRules(rules);

        // Set policy on bucket
        s3Client.setBucketLifecycleConfiguration(bucketName, configuration);

        log.info("Complete Restore Task for space " + spaceId +
                 " completed successfully");

        String result = "Complete restore was successful";
        return new CompleteSnapshotTaskResult(result).serialize();
    }

}
