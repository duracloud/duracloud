/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.duracloud.s3storage.S3StorageProvider;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;

import java.util.List;

/**
 * @author: Bill Branan
 * Date: Jun 3, 2010
 */
public class DeleteStreamingTaskRunner extends BaseStreamingTaskRunner {

    private final Logger log = LoggerFactory.getLogger(DeleteStreamingTaskRunner.class);

    private static final String TASK_NAME = "delete-streaming";

    public DeleteStreamingTaskRunner(S3StorageProvider s3Provider,
                                     S3Service s3Service,
                                     CloudFrontService cfService) {
        this.s3Provider = s3Provider;
        this.s3Service = s3Service;
        this.cfService = cfService;
    }

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {
        String spaceId = getSpaceId(taskParameters);
        log.info("Performing " + TASK_NAME + " task on space " + spaceId);        

        String bucketName = s3Provider.getBucketName(spaceId);
        String results;

        try {
            List<StreamingDistribution> existingDists =
                getAllExistingDistributions(bucketName);

            if(existingDists != null && existingDists.size() > 0) {
                for(StreamingDistribution existingDist : existingDists) {
                    String distId = existingDist.getId();
                    if(existingDist.isEnabled()) {
                        // Disable the distribution
                        cfService.disableStreamingDistributionForDeletion(distId);
                        // Wait for the distribution to be disabled
                        waitForDisabled(distId);
                    }
                    // Delete the distribution
                    cfService.deleteStreamingDistribution(distId);
                }
            } else {
                throw new RuntimeException("No streaming distribution " +
                                           "exists for space " + spaceId);
            }

            results = "Delete Streaming Task completed successfully";
        } catch(CloudFrontServiceException e) {
            log.warn("Error encountered running " + TASK_NAME + " task: " +
                     e.getMessage(), e);            
            results = "Delete Streaming Task failed due to: " + e.getMessage();
        }

        log.debug("Result of " + TASK_NAME + " task: " + results);        
        return results;
    }

    /*
     * Wait for the distribution to be disabled
     * Note that this can take up to 15 min
     */
    private void waitForDisabled(String distId)
        throws CloudFrontServiceException {
        long maxTime = 900000; // 15 min
        long start = System.currentTimeMillis();

        StreamingDistribution dist =
            cfService.getStreamingDistributionInfo(distId);

        while(!dist.isDeployed()) {
            if(System.currentTimeMillis() < start + maxTime) {
                sleep(10000);
                dist = cfService.getStreamingDistributionInfo(distId);
            } else {
                String error = "Timeout Reached waiting for distribution to " +
                    "be disabled. Please wait a few minutes and try again.";
                throw new CloudFrontServiceException(error);
            }
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.warn("sleep interrupted");
        }
    }
}
