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
import org.duracloud.snapshot.dto.CompleteSnapshotTaskParameters;
import org.duracloud.snapshot.dto.CompleteSnapshotTaskResult;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.TaskRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Completes the snapshot process by cleaning up content that is no longer
 * needed now that the snapshot has been transferred successfully.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class CompleteSnapshotTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "complete-snapshot";
    private static int EXPIRATION_DAYS = 1;

    private SnapshotStorageProvider snapshotProvider;
    private AmazonS3Client s3Client;

    public CompleteSnapshotTaskRunner(SnapshotStorageProvider snapshotProvider,
                                      AmazonS3Client s3Client) {
        this.snapshotProvider = snapshotProvider;
        this.s3Client = s3Client;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        CompleteSnapshotTaskParameters taskParams =
            CompleteSnapshotTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();
        String bucketName = snapshotProvider.getBucketName(spaceId);

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

        return new CompleteSnapshotTaskResult(EXPIRATION_DAYS).serialize();
    }

}
