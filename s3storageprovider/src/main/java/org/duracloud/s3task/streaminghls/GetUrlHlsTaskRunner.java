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
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.GetHlsUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves a URL for a media file that is streamed through Amazon Cloudfront via HLS
 *
 * @author: Bill Branan
 * Date: Aug 6, 2018
 */
public class GetUrlHlsTaskRunner extends BaseHlsTaskRunner {

    private final Logger log = LoggerFactory.getLogger(GetUrlHlsTaskRunner.class);

    private static final String TASK_NAME = StorageTaskConstants.GET_HLS_URL_TASK_NAME;

    public GetUrlHlsTaskRunner(StorageProvider s3Provider,
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
        GetHlsUrlTaskParameters taskParams = GetHlsUrlTaskParameters.deserialize(taskParameters);

        String spaceId = taskParams.getSpaceId();
        String contentId = taskParams.getContentId();

        log.info("Performing " + TASK_NAME + " task with parameters: spaceId=" + spaceId +
                 ", contentId=" + contentId);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);
        GetUrlTaskResult taskResult = new GetUrlTaskResult();

        // Ensure that streaming service is on
        checkThatStreamingServiceIsEnabled(spaceId, TASK_NAME);

        // Retrieve the existing distribution for the given space
        DistributionSummary existingDist = getExistingDistribution(bucketName);
        if (null == existingDist) {
            throw new UnsupportedTaskException(TASK_NAME,
                                               "The " + TASK_NAME + " task can only be used after a space has " +
                                               "been configured to enable HLS streaming. Use " +
                                               StorageTaskConstants.ENABLE_HLS_TASK_NAME +
                                               " to enable HLS streaming on this space.");
        }
        String domainName = existingDist.getDomainName();

        taskResult.setStreamUrl("https://" + domainName + "/" + contentId);

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }
}
