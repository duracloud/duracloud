/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;


import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.GetUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.StreamingDistributionSummary;

/**
 * Retrieves a URL for a media file that is streamed through
 * Amazon Cloudfront. The distribution used must not be secured.
 *
 * @author: Bill Branan
 * Date: 3/23/2015
 */
public class GetUrlTaskRunner extends BaseStreamingTaskRunner  {

    private final Logger log = LoggerFactory.getLogger(GetUrlTaskRunner.class);

    private static final String TASK_NAME =
        StorageTaskConstants.GET_URL_TASK_NAME;

    public GetUrlTaskRunner(StorageProvider s3Provider,
                            S3StorageProvider unwrappedS3Provider,
                            AmazonCloudFrontClient cfClient) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.cfClient = cfClient;
    }

    public String getName() {
        return TASK_NAME;
    }

    // Build URL
    public String performTask(String taskParameters) {
        GetUrlTaskParameters taskParams =
            GetUrlTaskParameters.deserialize(taskParameters);

        String spaceId = taskParams.getSpaceId();
        String contentId = taskParams.getContentId();
        String resourcePrefix = taskParams.getResourcePrefix();

        log.info("Performing " + TASK_NAME + " task with parameters: spaceId="+spaceId+
                 ", contentId="+contentId+", resourcePrefix="+resourcePrefix);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);
        GetUrlTaskResult taskResult = new GetUrlTaskResult();

        // Ensure that streaming service is on
        checkThatStreamingServiceIsEnabled(this.s3Provider, spaceId, TASK_NAME);

        // Retrieve the existing distribution for the given space
        StreamingDistributionSummary existingDist =
            getExistingDistribution(bucketName);
        if(null == existingDist) {
            throw new UnsupportedTaskException(TASK_NAME,
                "The " + TASK_NAME + " task can only be used after a space has " +
                "been configured to enable open streaming. Use " +
                StorageTaskConstants.ENABLE_STREAMING_TASK_NAME +
                " to enable open streaming on this space.");
        }
        String domainName = existingDist.getDomainName();

        // Verify that this is an open distribution
        if(! existingDist.getTrustedSigners().getItems().isEmpty()) {
            throw new UnsupportedTaskException(TASK_NAME,
                "The " + TASK_NAME + " task cannot be used to request a stream " +
                "from a secure distribution. Use " +
                StorageTaskConstants.GET_SIGNED_URL_TASK_NAME + " instead.");
        }
        
        // Create the resource Id, which may or may not require a prefix
        // (such as "mp4:" for an mp4 file) depending on the intended player
        String resourceId = contentId;
        if(null != resourcePrefix && !resourcePrefix.equals("")) {
            resourceId = resourcePrefix + contentId;
        }

        taskResult.setStreamUrl("rtmp://" + domainName + "/cfx/st/" + resourceId);

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }
}
