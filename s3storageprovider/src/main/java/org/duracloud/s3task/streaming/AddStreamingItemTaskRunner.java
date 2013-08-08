/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.OriginAccessIdentity;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The purpose of this task is to add an additional item to a pre-existing
 * streaming distribution.
 *
 * @author: Bill Branan
 * Date: Sept 13, 2010
 */
public class AddStreamingItemTaskRunner extends BaseStreamingTaskRunner  {

    private final Logger log =
        LoggerFactory.getLogger(AddStreamingItemTaskRunner.class);

    private static final String TASK_NAME = "add-streaming-item";

    public AddStreamingItemTaskRunner(S3StorageProvider s3Provider,
                                      AmazonS3Client s3Client,
                                      CloudFrontService cfService) {
        this.s3Provider = s3Provider;
        this.s3Client = s3Client;
        this.cfService = cfService;
    }

    public String getName() {
        return TASK_NAME;
    }

    protected class TaskParams {
        private String spaceId;
        private String contentId;

        public String getSpaceId() {
            return spaceId;
        }

        public String getContentId() {
            return contentId;
        }
    }

    /*
     * Extracts the task parameters
     */
    protected TaskParams parseTaskParams(String taskParameters) {
        TaskParams paramters = new TaskParams();
        if(taskParameters != null && !taskParameters.equals("")) {
            String[] paramSplit = taskParameters.split(":");
            if(paramSplit.length >= 2) {
                paramters.spaceId = paramSplit[0];
                paramters.contentId = paramSplit[1];
                return paramters;
            }
        }
        throw new RuntimeException("Both a Space ID and a Content ID must be " +
                                   "provided (with a colon delimeter)");
    }

    // Add streaming item
    public String performTask(String taskParameters) {
        TaskParams taskParams = parseTaskParams(taskParameters);
        String spaceId = taskParams.getSpaceId();
        String contentId = taskParams.getContentId();

        log.info("Performing " + TASK_NAME + " task on " +
                 spaceId + "/" + contentId);

        // Will throw if bucket does not exist
        String bucketName = s3Provider.getBucketName(spaceId);
        String oaIdentityId;
        String results;

        try {
            StreamingDistribution existingDist =
                getExistingDistribution(bucketName);

            if(existingDist != null) { // There is an existing distribution
                String distId = existingDist.getId();

                oaIdentityId = getDistributionOriginAccessId(distId);
                if(null == oaIdentityId) {
                    throw new RuntimeException("No existing origin access " +
                                               "identity for distribution " +
                                               distId + " on space with ID " +
                                               spaceId);
                }
            } else {
                throw new RuntimeException("No existing distribution on " +
                                           "space with ID: " + spaceId);
            }

            // Set content ACLs to accept origin access identity
            setContentAccessIdentity(contentId, bucketName, oaIdentityId);

            results = "Add Streaming Item Task completed successfully.";
        } catch(Exception e) {
            log.warn("Error encountered running " + TASK_NAME + " task: " +
                     e.getMessage(), e);
            results = "Enable Streaming Task failed due to: " + e.getMessage();
        }

        // Return results
        Map<String, String> returnInfo = new HashMap<String, String>();
        returnInfo.put("results", results);
        String toReturn = SerializationUtil.serializeMap(returnInfo);
        log.debug("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    /*
     * Adds access permissions to an item in a space for an origin
     * access identity. This allows Cloudfront to access content in S3
     *
     * @return results of the ACL setting activity
     */
    private void setContentAccessIdentity(String contentId,
                                          String bucketName,
                                          String oaIdentityId)
        throws CloudFrontServiceException, DuraCloudCheckedException {
        // Clean up the origin access id if necessary
        oaIdentityId = StringUtils.removeStart(oaIdentityId,
            CloudFrontService.ORIGIN_ACCESS_IDENTITY_PREFIX);

        // Get the origin access identity to use
        OriginAccessIdentity oaIdentity =
            cfService.getOriginAccessIdentity(oaIdentityId);
        String s3UserId = oaIdentity.getS3CanonicalUserId();
        CanonicalGrantee s3Grantee = new CanonicalGrantee(s3UserId);

        // Attempt to set a new ACL permission allowing read for origin
        // access identity to content item
        setACL(bucketName, contentId, s3Grantee);
    }

}
