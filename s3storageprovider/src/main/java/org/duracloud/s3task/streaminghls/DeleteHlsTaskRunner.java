/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import java.util.List;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.DeleteDistributionRequest;
import com.amazonaws.services.cloudfront.model.Distribution;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigRequest;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigResult;
import com.amazonaws.services.cloudfront.model.GetDistributionRequest;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskResult;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a CloudFront Web Distribution. If the distribution has not yet
 * been disabled, it is disabled first, then deleted
 *
 * @author: Bill Branan
 * Date: Aug 6, 2018
 */
public class DeleteHlsTaskRunner extends BaseHlsTaskRunner {

    private final Logger log = LoggerFactory.getLogger(DeleteHlsTaskRunner.class);

    private static final String TASK_NAME = StorageTaskConstants.DELETE_HLS_TASK_NAME;
    private static final String DEPLOYED = "Deployed";

    public DeleteHlsTaskRunner(StorageProvider s3Provider,
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
        DeleteStreamingTaskParameters taskParams =
            DeleteStreamingTaskParameters.deserialize(taskParameters);

        String spaceId = taskParams.getSpaceId();
        log.info("Performing " + TASK_NAME + " task on space " + spaceId);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);
        DeleteStreamingTaskResult taskResult = new DeleteStreamingTaskResult();

        removeHlsStreamingHostFromSpaceProps(spaceId);
        s3Client.deleteBucketPolicy(bucketName);

        List<DistributionSummary> existingDists = getAllExistingWebDistributions(bucketName);

        if (existingDists != null && existingDists.size() > 0) {
            for (DistributionSummary existingDist : existingDists) {
                String distId = existingDist.getId();
                if (existingDist.isEnabled()) {
                    // Disable the distribution
                    setDistributionState(distId, false);
                }

                // Kick off a thread to wait and complete the delete action
                Runnable deleteWorker = () -> waitAndDelete(distId);
                new Thread(deleteWorker).start();
            }
        } else {
            throw new RuntimeException("No streaming distribution " +
                                       "exists for space " + spaceId);
        }

        taskResult.setResult(TASK_NAME + " task completed successfully");

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    private void waitAndDelete(String distId) {
        // Wait for the distribution to be disabled
        waitForDisabled(distId);

        // Delete the distribution
        GetDistributionConfigResult result =
            cfClient.getDistributionConfig(new GetDistributionConfigRequest(distId));
        cfClient.deleteDistribution(
            new DeleteDistributionRequest().withId(distId)
                                           .withIfMatch(result.getETag()));
    }

    /*
     * Wait for the distribution to be disabled
     * Note that this can take up to 15 min
     */
    private void waitForDisabled(String distId) {
        long maxTime = 1800000; // 30 min
        long start = System.currentTimeMillis();

        boolean deployed = isDeployed(distId);
        while (!deployed) {
            if (System.currentTimeMillis() < start + maxTime) {
                sleep(10000);
                deployed = isDeployed(distId);
            } else {
                String error = "Timeout Reached waiting for distribution to " +
                               "be disabled. Please wait a few minutes and try again.";
                throw new RuntimeException(error);
            }
        }
    }

    private boolean isDeployed(String distId) {
        Distribution dist =
            cfClient.getDistribution(new GetDistributionRequest(distId)).getDistribution();
        return DEPLOYED.equals(dist.getStatus());
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.warn("sleep interrupted");
        }
    }
}
