/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class DisableStreamingTaskRunner extends BaseStreamingTaskRunner {

    private final Logger log =
        LoggerFactory.getLogger(DisableStreamingTaskRunner.class);

    public static final String TASK_NAME = "disable-streaming";

    public DisableStreamingTaskRunner(StorageProvider s3Provider,
                                      S3StorageProvider unwrappedS3Provider,
                                      AmazonS3Client s3Client,
                                      CloudFrontService cfService) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfService = cfService;
    }

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {
        String spaceId = getSpaceId(taskParameters);
        log.info("Performing " + TASK_NAME + " task on space " + spaceId);        

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);
        String results;

        removeStreamingHostFromSpaceProps(spaceId);

        try {
            // Ensure that there is an existing distribution for the given space
            StreamingDistribution existingDist =
                getExistingDistribution(bucketName);

            if(existingDist != null) {
                s3Client.deleteBucketPolicy(bucketName);
            } else {
                throw new RuntimeException("No streaming distribution " +
                                           "exists for space " + spaceId);
            }

            results = "Disable Streaming Task completed successfully";
        } catch(CloudFrontServiceException e) {
            log.warn("Error encountered running " + TASK_NAME + " task: " +
                     e.getMessage(), e);            
            results = "Disable Streaming Task failed due to: " + e.getMessage();
        }

        log.debug("Result of " + TASK_NAME + " task: " + results);        
        return results;
    }

}