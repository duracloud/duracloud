/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskResult;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Disables a CloudFront Web Distribution associated with a specific space
 *
 * @author: Bill Branan
 * Date: Aug 6, 2018
 */
public class DisableHlsTaskRunner extends BaseHlsTaskRunner {

    private final Logger log = LoggerFactory.getLogger(DisableHlsTaskRunner.class);

    private static final String TASK_NAME = StorageTaskConstants.DISABLE_HLS_TASK_NAME;

    public DisableHlsTaskRunner(StorageProvider s3Provider,
                                S3StorageProvider unwrappedS3Provider,
                                AmazonS3Client s3Client,
                                AmazonCloudFrontClient cfClient) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
    }

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {
        DisableStreamingTaskParameters taskParams =
            DisableStreamingTaskParameters.deserialize(taskParameters);

        String spaceId = taskParams.getSpaceId();
        log.info("Performing " + TASK_NAME + " task on space " + spaceId);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);
        DisableStreamingTaskResult taskResult = new DisableStreamingTaskResult();

        removeHlsStreamingHostFromSpaceProps(spaceId);

        // Ensure that there at least one existing distribution for the given space
        DistributionSummary existingDist = getExistingDistribution(bucketName);

        // Remove the bucket policy, which gives CloudFront access to the space
        if (existingDist != null) {
            s3Client.deleteBucketPolicy(bucketName);
        } else {
            throw new RuntimeException("No streaming distribution exists for space " + spaceId);
        }

        taskResult.setResult(TASK_NAME + " task completed successfully");

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

}