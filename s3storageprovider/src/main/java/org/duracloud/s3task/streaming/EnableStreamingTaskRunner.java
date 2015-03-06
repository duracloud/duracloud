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
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class EnableStreamingTaskRunner extends BaseStreamingTaskRunner  {

    private final Logger log =
        LoggerFactory.getLogger(EnableStreamingTaskRunner.class);

    private static final String TASK_NAME =
        StorageTaskConstants.ENABLE_STREAMING_TASK_NAME;

    public EnableStreamingTaskRunner(StorageProvider s3Provider,
                                     S3StorageProvider unwrappedS3Provider,
                                     AmazonS3Client s3Client,
                                     CloudFrontService cfService,
                                     String cfAccountId) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfService = cfService;
        this.cfAccountId = cfAccountId;
    }

    public String getName() {
        return TASK_NAME;
    }

    // Enable streaming
    public String performTask(String taskParameters) {
        EnableStreamingTaskParameters taskParams =
            EnableStreamingTaskParameters.deserialize(taskParameters);

        String spaceId = taskParams.getSpaceId();
        boolean secure = taskParams.isSecure();

        log.info("Performing " + TASK_NAME + " task on space " + spaceId +
                 ". Secure streaming set to " + secure);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);

        String domainName = null;
        String distId = null;
        String oaIdentityId = null;
        EnableStreamingTaskResult taskResult = new EnableStreamingTaskResult();

        try {
            StreamingDistribution existingDist =
                getExistingDistribution(bucketName);

            if(existingDist != null) { // There is an existing distribution
                distId = existingDist.getId();

                oaIdentityId = getDistributionOriginAccessId(distId);
                if(oaIdentityId != null) {
                    // Currently, a disabled distribution will not return a
                    // valid oaIdentity, so getting to this point indicates that
                    // the distirbution is enabled. The call to enable is being
                    // left here just in case this changes.
                    DistributionConfig distConfig = existingDist.getConfig();
                    if(!distConfig.isEnabled()) {
                        // Enable the existing distribution
                        distConfig.setEnabled(true);
                        cfService.updateDistributionConfig(distId, distConfig);
                    }
                    domainName = existingDist.getDomainName();
                } else {
                    distId = null;
                }
            }

            if(distId == null) { // No existing distribution, need to create
                oaIdentityId = getOriginAccessId();
                S3Origin origin =
                    new S3Origin(cfService.sanitizeS3BucketName(bucketName),
                                 oaIdentityId);
                Origin[] origins = {origin};

                // Set trusted signers to null if this is not a secure distribution
                String[] trustedSigners = secure ? new String[]{cfAccountId} : null;

                StreamingDistributionConfig distConfig =
                    new StreamingDistributionConfig(origins, null, null, null,
                                                    true, null, trustedSigners);
                Distribution dist = cfService.createDistribution(distConfig);

                domainName = dist.getDomainName();
            }

            // Set bucket policy to accept origin access identity
            setBucketAccessPolicy(bucketName, oaIdentityId);

            // Update bucket tags to include streaming host
            Map<String, String> spaceProps =
                s3Provider.getSpaceProperties(spaceId);
            spaceProps.put(STREAMING_HOST_PROP, domainName);
            unwrappedS3Provider.setNewSpaceProperties(spaceId, spaceProps);

            taskResult.setResult("Enable Streaming Task completed successfully");
        } catch(CloudFrontServiceException e) {
            log.warn("Error encountered running " + TASK_NAME + " task: " +
                     e.getMessage(), e);
            taskResult.setResult("Enable Streaming Task failed due to: " +
                                 e.getMessage());
        }

        // Return results
        taskResult.setStreamingHost(domainName);
        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    /*
     * Retrieves an origin access ID, which may be either pre-existing or new
     */
    private String getOriginAccessId() throws CloudFrontServiceException {
        String oaId = getExistingOriginAccessId();
        if(oaId != null) { // Use existing ID
            return oaId;
        } else { // Create a new ID
            // Note that while the comment ("id") is not officially necessary
            // in this call, it fails with a NPE if the comment is not included
            OriginAccessIdentity oaIdentity =
                cfService.createOriginAccessIdentity(null, "id");
            return oaIdentity.getId();
        }
    }

    /*
     * Attempts to get an existing origin access ID
     */
    private String getExistingOriginAccessId()
        throws CloudFrontServiceException {
        List oaiList = cfService.getOriginAccessIdentityList();
        if(oaiList != null && oaiList.size() > 0) {
            OriginAccessIdentity oaID =
                (OriginAccessIdentity)oaiList.iterator().next();
            if(oaID != null) {
                return oaID.getId();
            }
        }
        return null;
    }

    /*
     * Updates the bucket policy to allow GET access to the cloudfront origin
     * access identity. This allows Cloudfront to access content in S3
     *
     * @return results of the ACL setting activity
     */
    private void setBucketAccessPolicy(String bucketName, String oaIdentityId)
        throws CloudFrontServiceException {
        // Clean up the origin access id if necessary
        oaIdentityId = StringUtils.removeStart(oaIdentityId,
            CloudFrontService.ORIGIN_ACCESS_IDENTITY_PREFIX);

        // Get the origin access identity to use
        OriginAccessIdentity oaIdentity =
            cfService.getOriginAccessIdentity(oaIdentityId);
        String s3UserId = oaIdentity.getS3CanonicalUserId();

        StringBuilder policyText = new StringBuilder();
        policyText.append("{\"Version\":\"2012-10-17\",");
	    policyText.append("\"Id\":\"PolicyForCloudFrontPrivateContent\",");
	    policyText.append("\"Statement\":[{");
        policyText.append("\"Sid\":\"Grant CloudFront access to private content\",");
		policyText.append("\"Effect\":\"Allow\",");
		policyText.append("\"Principal\":{\"CanonicalUser\":\"" + s3UserId + "\"},");
        policyText.append("\"Action\":\"s3:GetObject\",");
        policyText.append("\"Resource\":\"arn:aws:s3:::" + bucketName + "/*\"");
        policyText.append("}]}");
        s3Client.setBucketPolicy(bucketName, policyText.toString());
    }

}
