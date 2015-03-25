/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.lang.StringUtils;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskResult;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.Distribution;
import org.jets3t.service.model.cloudfront.DistributionConfig;
import org.jets3t.service.model.cloudfront.Origin;
import org.jets3t.service.model.cloudfront.OriginAccessIdentity;
import org.jets3t.service.model.cloudfront.S3Origin;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;
import org.jets3t.service.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Retrieves a signed URL for a media file that is streamed through
 * Amazon Cloudfront via a secure distribution
 *
 * @author: Bill Branan
 * Date: 3/9/2015
 */
public class GetSignedUrlTaskRunner extends BaseStreamingTaskRunner  {

    private final Logger log =
        LoggerFactory.getLogger(GetSignedUrlTaskRunner.class);

    private static final String TASK_NAME =
        StorageTaskConstants.GET_SIGNED_URL_TASK_NAME;

    public GetSignedUrlTaskRunner(StorageProvider s3Provider,
                                  S3StorageProvider unwrappedS3Provider,
                                  CloudFrontService cfService,
                                  String cfKeyId,
                                  String cfKeyPath) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.cfService = cfService;
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
        long dateLessThan = taskParams.getDateLessThan();
        long dateGreaterThan = taskParams.getDateGreaterThan();
        String ipAddress = taskParams.getIpAddress();

        log.info("Performing " + TASK_NAME + " task with parameters: spaceId="+spaceId+
                 ", contentId="+contentId+", resourcePrefix="+resourcePrefix+
                 ", dateLessThan="+dateLessThan+ ", dateGreaterThan="+dateGreaterThan+
                 ", ipAddress="+ipAddress);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);
        GetSignedUrlTaskResult taskResult = new GetSignedUrlTaskResult();

        // Retrieve signing key. This key is generated via the AWS console and converted
        // to DER format. This key file is expected to be local to the DuraStore
        // application. For more info about this key see: http://amzn.to/1F8yPZ7
        byte[] signingKey;
        try {
            signingKey =
                ServiceUtils.readInputStreamToBytes(new FileInputStream(cfKeyPath));
        } catch(IOException e) {
            throw new RuntimeException("Unable to perform " + TASK_NAME +
                                       ". Secure signing key is not available. " +
                                       "Cause: " + e.getMessage());
        }

        try {
            // Retrieve the existing distribution for the given space
            StreamingDistribution existingDist = getExistingDistribution(bucketName);
            String domainName = existingDist.getDomainName();

            // Verify that this is a secure distribution
            if(existingDist.getActiveTrustedSigners().isEmpty()) {
                throw new UnsupportedTaskException(TASK_NAME,
                    "The " + TASK_NAME + " task cannot be used to request a " +
                    "stream from an open distribution. Use " +
                    StorageTaskConstants.GET_URL_TASK_NAME + " instead.");
            }

            // Verify that the date greater than is in the future
            Date verifiedDateGreaterThan = null;
            if(dateGreaterThan > System.currentTimeMillis()) {
                verifiedDateGreaterThan = new Date(dateGreaterThan);
            }

            // Build a policy document to define custom restrictions for the signed URL.
            // Note that the resource path for RTMP streams is simply the name of the
            // stream; that name may need have its file extension omitted, depending on
            // the intended player.
            String policy =
                CloudFrontService.buildPolicyForSignedUrl(contentId,
                                                          new Date(dateLessThan),
                                                          ipAddress,
                                                          verifiedDateGreaterThan);

            // Create the resource Id, which may or may not require a prefix
            // (such as "mp4:" for an mp4 file) depending on the intended player
            String resourceId = contentId;
            if(null != resourcePrefix && !resourcePrefix.equals("")) {
                resourceId = resourcePrefix + contentId;
            }

            // Generate a signed URL using the custom policy document.
            String signedUrl =
                CloudFrontService.signUrl(resourceId, cfKeyId, signingKey, policy);
            taskResult.setSignedUrl("rtmp://" + domainName + "/cfx/st/" + signedUrl);
        } catch(CloudFrontServiceException e) {
            throw new RuntimeException("Error encountered running " + TASK_NAME +
                                       " task: " + e.getMessage(), e);
        }

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }
}
