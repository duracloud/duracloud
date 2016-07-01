/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.DeleteStreamingDistributionRequest;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionConfigRequest;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionConfigResult;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionRequest;
import com.amazonaws.services.cloudfront.model.StreamingDistribution;
import com.amazonaws.services.cloudfront.model.StreamingDistributionSummary;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskResult;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: Bill Branan
 * Date: Jun 3, 2010
 */
public class DeleteStreamingTaskRunner extends BaseStreamingTaskRunner {

    private final Logger log = LoggerFactory.getLogger(DeleteStreamingTaskRunner.class);

    private static final String TASK_NAME =
        StorageTaskConstants.DELETE_STREAMING_TASK_NAME;
    private static final String DEPLOYED = "Deployed";

    public DeleteStreamingTaskRunner(StorageProvider s3Provider,
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

        removeStreamingHostFromSpaceProps(spaceId);
        s3Client.deleteBucketPolicy(bucketName);

        List<StreamingDistributionSummary> existingDists =
            getAllExistingDistributions(bucketName);

        if(existingDists != null && existingDists.size() > 0) {
            for(StreamingDistributionSummary existingDist : existingDists) {
                String distId = existingDist.getId();
                if(existingDist.isEnabled()) {
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

        taskResult.setResult("Delete Streaming Task completed successfully");

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    private void waitAndDelete(String distId) {
        // Wait for the distribution to be disabled
        waitForDisabled(distId);

        // Delete the distribution
        GetStreamingDistributionConfigResult result =
            cfClient.getStreamingDistributionConfig(
                new GetStreamingDistributionConfigRequest(distId));
        cfClient.deleteStreamingDistribution(
            new DeleteStreamingDistributionRequest().withId(distId)
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
        while(!deployed) {
            if(System.currentTimeMillis() < start + maxTime) {
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
        StreamingDistribution dist =
            cfClient.getStreamingDistribution(
                new GetStreamingDistributionRequest(distId))
                    .getStreamingDistribution();
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
