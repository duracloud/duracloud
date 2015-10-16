/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.model.StreamingDistributionSummary;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskResult;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;

/**
 * Retrieves a signed URL for a media file that is streamed through
 * Amazon Cloudfront via a secure distribution
 *
 * @author: Bill Branan
 * Date: 3/9/2015
 */
public class GetSignedUrlTaskRunner extends BaseStreamingTaskRunner  {

    public static final int DEFAULT_MINUTES_TO_EXPIRE = 480;

    private final Logger log =
        LoggerFactory.getLogger(GetSignedUrlTaskRunner.class);

    private static final String TASK_NAME =
        StorageTaskConstants.GET_SIGNED_URL_TASK_NAME;

    public GetSignedUrlTaskRunner(StorageProvider s3Provider,
                                  S3StorageProvider unwrappedS3Provider,
                                  AmazonCloudFrontClient cfClient,
                                  String cfKeyId,
                                  String cfKeyPath) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.cfClient = cfClient;
        // Certificate identifier, an active trusted signer for the distribution
        this.cfKeyId = cfKeyId;
        // Local file path to signing key in DER format
        this.cfKeyPath = cfKeyPath;
    }

    public String getName() {
        return TASK_NAME;
    }

    // Build secure URL
    public String performTask(String taskParameters) {
        GetSignedUrlTaskParameters taskParams =
            GetSignedUrlTaskParameters.deserialize(taskParameters);

        String spaceId = taskParams.getSpaceId();
        String contentId = taskParams.getContentId();
        String resourcePrefix = taskParams.getResourcePrefix();
        String ipAddress = taskParams.getIpAddress();
        int minutesToExpire = taskParams.getMinutesToExpire();
        if(minutesToExpire <= 0) {
            minutesToExpire = DEFAULT_MINUTES_TO_EXPIRE;
        }

        log.info("Performing " + TASK_NAME + " task with parameters: spaceId="+spaceId+
                 ", contentId="+contentId+", resourcePrefix="+resourcePrefix+
                 ", minutesToExpire="+minutesToExpire+", ipAddress="+ipAddress);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);
        GetSignedUrlTaskResult taskResult = new GetSignedUrlTaskResult();

        // Ensure that streaming service is on
        checkThatStreamingServiceIsEnabled(this.s3Provider, spaceId, TASK_NAME);

        // Retrieve the existing distribution for the given space
        StreamingDistributionSummary existingDist =
            getExistingDistribution(bucketName);
        if (null == existingDist) {
            throw new UnsupportedTaskException(TASK_NAME,
                "The " + TASK_NAME + " task can only be used after a space " +
                "has been configured to enable secure streaming. Use " +
                StorageTaskConstants.ENABLE_STREAMING_TASK_NAME +
                " to enable secure streaming on this space.");
        }
        String domainName = existingDist.getDomainName();

        // Verify that this is a secure distribution
        if (existingDist.getTrustedSigners().getItems().isEmpty()) {
            throw new UnsupportedTaskException(TASK_NAME,
                "The " + TASK_NAME + " task cannot be used to request a " +
                "stream from an open distribution. Use " +
                StorageTaskConstants.GET_URL_TASK_NAME + " instead.");
        }

        // Make sure resourcePrefix is a valid string
        if (null == resourcePrefix) {
            resourcePrefix = "";
        }

        // Define expiration date/time
        Calendar expireCalendar = Calendar.getInstance();
        expireCalendar.add(Calendar.MINUTE, minutesToExpire);

        try {
            String signedUrl =
                CloudFrontUrlSigner.getSignedURLWithCustomPolicy(
                    CloudFrontUrlSigner.Protocol.rtmp,
                    domainName,
                    new File(cfKeyPath),
                    contentId,
                    cfKeyId,
                    expireCalendar.getTime(),
                    null,
                    ipAddress);
            taskResult.setSignedUrl("rtmp://" + domainName + "/cfx/st/" +
                                    resourcePrefix + signedUrl);
        } catch(InvalidKeySpecException | IOException e) {
            throw new RuntimeException("Error encountered attempting to sign URL for" +
                                       " task " + TASK_NAME + ": " + e.getMessage(), e);
        }
        
        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }
}
