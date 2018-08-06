/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.AllowedMethods;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentity;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentityConfig;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentitySummary;
import com.amazonaws.services.cloudfront.model.CreateCloudFrontOriginAccessIdentityRequest;
import com.amazonaws.services.cloudfront.model.CreateDistributionRequest;
import com.amazonaws.services.cloudfront.model.DefaultCacheBehavior;
import com.amazonaws.services.cloudfront.model.Distribution;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.ForwardedValues;
import com.amazonaws.services.cloudfront.model.GetCloudFrontOriginAccessIdentityRequest;
import com.amazonaws.services.cloudfront.model.Headers;
import com.amazonaws.services.cloudfront.model.ListCloudFrontOriginAccessIdentitiesRequest;
import com.amazonaws.services.cloudfront.model.Method;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.Origins;
import com.amazonaws.services.cloudfront.model.S3OriginConfig;
import com.amazonaws.services.cloudfront.model.TrustedSigners;
import com.amazonaws.services.cloudfront.model.ViewerProtocolPolicy;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a CloudFront Web Distribution to support Http Live Streaming (HLS) of content
 * in a space. The content is expected to already be in a format supported by HLS.
 *
 * @author: Bill Branan
 * Date: Aug 3, 2018
 */
public class EnableHlsTaskRunner extends BaseHlsTaskRunner {

    private final Logger log = LoggerFactory.getLogger(EnableHlsTaskRunner.class);

    private static final String TASK_NAME = StorageTaskConstants.ENABLE_HLS_TASK_NAME;

    public EnableHlsTaskRunner(StorageProvider s3Provider,
                               S3StorageProvider unwrappedS3Provider,
                               AmazonS3Client s3Client,
                               AmazonCloudFrontClient cfClient,
                               String cfAccountId) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
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
        String oaIdentityId = getOriginAccessId();
        EnableStreamingTaskResult taskResult = new EnableStreamingTaskResult();

        DistributionSummary existingDist = getExistingDistribution(bucketName);

        if (existingDist != null) { // There is an existing distribution
            // Ensure that this is not an attempt to change the security type
            // of this existing distribution
            boolean existingSecure =
                !existingDist.getDefaultCacheBehavior().getTrustedSigners().getItems().isEmpty();
            if ((secure && !existingSecure) || (!secure && existingSecure)) {
                throw new UnsupportedTaskException(TASK_NAME,
                                                   "The space " + spaceId + " is already configured to stream as " +
                                                   (secure ? "OPEN" : "SECURE") +
                                                   " and cannot be updated to stream as " +
                                                   (secure ? "SECURE" : "OPEN") +
                                                   ". To do this, you must first execute the " +
                                                   StorageTaskConstants.DELETE_HLS_TASK_NAME + " task.");
            }

            distId = existingDist.getId();
            if (!existingDist.isEnabled()) { // Distribution is disabled, enable it
                setDistributionState(distId, true);
            }
            domainName = existingDist.getDomainName();
        } else { // No existing distribution, need to create one
            S3OriginConfig origin = new S3OriginConfig()
                .withOriginAccessIdentity(S3_ORIGIN_OAI_PREFIX + oaIdentityId);
            Origins origins = new Origins().withItems(
                new Origin().withDomainName(bucketName + S3_ORIGIN_SUFFIX)
                            .withS3OriginConfig(origin));

            // Only include trusted signers on secure distributions
            TrustedSigners signers = new TrustedSigners();
            if (secure) {
                signers.setItems(Collections.singletonList(cfAccountId));
                signers.setEnabled(true);
                signers.setQuantity(1);
            } else {
                signers.setEnabled(false);
                signers.setQuantity(0);
            }

            DefaultCacheBehavior defaultCacheBehavior = new DefaultCacheBehavior();
            defaultCacheBehavior.setTrustedSigners(signers);
            defaultCacheBehavior.setViewerProtocolPolicy(ViewerProtocolPolicy.RedirectToHttps);

            // Forwarding headers to support CORS, see:
            // https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/header-caching.html#header-caching-web-cors
            defaultCacheBehavior.setAllowedMethods(
                new AllowedMethods().withItems(Method.GET, Method.HEAD, Method.OPTIONS));
            defaultCacheBehavior.setForwardedValues(
                new ForwardedValues().withHeaders(
                    new Headers().withItems("Origin",
                                            "Access-Control-Request-Headers",
                                            "Access-Control-Request-Method")));

            Distribution dist =
                cfClient.createDistribution(
                    new CreateDistributionRequest(
                        new DistributionConfig()
                            .withCallerReference("" + System.currentTimeMillis())
                            .withOrigins(origins)
                            .withEnabled(true)
                            .withComment("HLS streaming for space: " + spaceId)
                            .withDefaultCacheBehavior(defaultCacheBehavior)))
                        .getDistribution();
            domainName = dist.getDomainName();
        }

        // Set bucket policy to accept origin access identity
        setBucketAccessPolicy(bucketName, oaIdentityId);

        // Set CORS policy on bucket
        setCorsPolicy(bucketName);

        // Update bucket tags to include streaming host
        Map<String, String> spaceProps =
            s3Provider.getSpaceProperties(spaceId);
        spaceProps.put(HLS_STREAMING_HOST_PROP, domainName);
        spaceProps.put(HLS_STREAMING_TYPE_PROP,
                       secure ? STREAMING_TYPE.SECURE.name() : STREAMING_TYPE.OPEN.name());
        unwrappedS3Provider.setNewSpaceProperties(spaceId, spaceProps);

        taskResult.setResult(TASK_NAME + " task completed successfully");

        // Return results
        taskResult.setStreamingHost(domainName);
        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    /*
     * Retrieves an origin access ID, which may be either pre-existing or new
     */
    private String getOriginAccessId() {
        String oaId = getExistingOriginAccessId();
        if (oaId != null) { // Use existing ID
            return oaId;
        } else { // Create a new ID
            return cfClient.createCloudFrontOriginAccessIdentity(
                new CreateCloudFrontOriginAccessIdentityRequest(
                    new CloudFrontOriginAccessIdentityConfig()
                        .withCallerReference("" + System.currentTimeMillis())
                        .withComment("DuraCloud Origin Access ID")))
                           .getCloudFrontOriginAccessIdentity().getId();
        }
    }

    /*
     * Attempts to get an existing origin access ID
     */
    private String getExistingOriginAccessId() {
        List<CloudFrontOriginAccessIdentitySummary> oaiList =
            cfClient.listCloudFrontOriginAccessIdentities(
                new ListCloudFrontOriginAccessIdentitiesRequest())
                    .getCloudFrontOriginAccessIdentityList().getItems();
        if (oaiList != null && oaiList.size() > 0) {
            return oaiList.iterator().next().getId();
        }
        return null;
    }

    /*
     * Updates the bucket policy to allow GET access to the cloudfront origin
     * access identity. This allows Cloudfront to access content in S3
     *
     * @return results of the ACL setting activity
     */
    private void setBucketAccessPolicy(String bucketName, String oaIdentityId) {
        CloudFrontOriginAccessIdentity cfOAIdentity =
            cfClient.getCloudFrontOriginAccessIdentity(
                new GetCloudFrontOriginAccessIdentityRequest(oaIdentityId))
                    .getCloudFrontOriginAccessIdentity();
        String s3UserId = cfOAIdentity.getS3CanonicalUserId();

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

    /*
     * Sets CORS policy on the bucket. This policy will be served through
     * CloudFront (based on forwarding rules) to allow cross-region requests
     * to be made by JavaScript wanting to stream content from CloudFront
     */
    private void setCorsPolicy(String bucketName) {
        CORSRule corsRule = new CORSRule();
        corsRule.setAllowedOrigins("*");
        corsRule.setAllowedMethods(CORSRule.AllowedMethods.GET, CORSRule.AllowedMethods.HEAD);
        corsRule.setMaxAgeSeconds(3000);
        corsRule.setAllowedHeaders("*");
        BucketCrossOriginConfiguration corsConfig =
            new BucketCrossOriginConfiguration().withRules(corsRule);

        s3Client.setBucketCrossOriginConfiguration(bucketName, corsConfig);
    }

}
