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
import org.jets3t.service.model.cloudfront.S3Origin;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class EnableStreamingTaskRunner extends BaseStreamingTaskRunner  {

    private final Logger log =
        LoggerFactory.getLogger(EnableStreamingTaskRunner.class);

    private static final String TASK_NAME = "enable-streaming";

    public EnableStreamingTaskRunner(S3StorageProvider s3Provider,
                                     AmazonS3Client s3Client,
                                     CloudFrontService cfService) {
        this.s3Provider = s3Provider;
        this.s3Client = s3Client;
        this.cfService = cfService;
    }

    public String getName() {
        return TASK_NAME;
    }

    // Enable streaming
    public String performTask(String taskParameters) {
        String spaceId = getSpaceId(taskParameters);       
        log.info("Performing " + TASK_NAME + " task on space " + spaceId);

        String bucketName = s3Provider.getBucketName(spaceId);

        String domainName = null;
        String distId = null;
        String oaIdentityId = null;
        String results;

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
                    if(!existingDist.isEnabled()) {
                        // Enable the existing distribution
                        cfService.updateStreamingDistributionConfig(distId,
                                                                    null,
                                                                    null,
                                                                    null,
                                                                    true,
                                                                    null);
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
                StreamingDistribution dist =
                    cfService.createStreamingDistribution(origin,
                                                          null,
                                                          null,
                                                          null,
                                                          true,
                                                          null,
                                                          false,
                                                          null);
                domainName = dist.getDomainName();
                distId = dist.getId();
            }

            // Set content ACLs to accept origin access identity
            String aclResults =
                setContentAccessIdentity(spaceId, bucketName, oaIdentityId);

            results = "Enable Streaming Task completed, " +
                      "results: " + aclResults;
        } catch(CloudFrontServiceException e) {
            log.warn("Error encountered running " + TASK_NAME + " task: " +
                     e.getMessage(), e);
            results = "Enable Streaming Task failed due to: " + e.getMessage();
        }

        // Return results
        Map<String, String> returnInfo = new HashMap<String, String>();
        returnInfo.put("domain-name", domainName);
        returnInfo.put("results", results);
        String toReturn = SerializationUtil.serializeMap(returnInfo);
        log.debug("Result of " + TASK_NAME + " task: " + toReturn);
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
     * Adds access permissions to each item in a space for an origin
     * access identity. This allows Cloudfront to access content in S3
     *
     * @return results of the ACL setting activity
     */
    private String setContentAccessIdentity(String spaceId,
                                            String bucketName,
                                            String oaIdentityId)
        throws CloudFrontServiceException {
        // Clean up the origin access id if necessary
        oaIdentityId = StringUtils.removeStart(oaIdentityId,
            CloudFrontService.ORIGIN_ACCESS_IDENTITY_PREFIX);

        // Get the origin access identity to use
        OriginAccessIdentity oaIdentity =
            cfService.getOriginAccessIdentity(oaIdentityId);
        String s3UserId = oaIdentity.getS3CanonicalUserId();
        CanonicalGrantee s3Grantee = new CanonicalGrantee(s3UserId);

        // Get a list of items in the space
        Iterator<String> contentIds = getSpaceContents(spaceId);

        // Attempt to set a new ACL permission allowing read for origin
        // access identity to each content item
        int successfulSet = 0;
        List<String> failedSet = new ArrayList<String>();
        while(contentIds.hasNext()) {
            String contentId = contentIds.next();
            try {
                setACL(bucketName, contentId, s3Grantee);
                successfulSet++;
            } catch(DuraCloudCheckedException e) {
                log.error(e.getMessage());
                failedSet.add(contentId);
            }
        }

        // Build results
        StringBuilder results = new StringBuilder();
        results.append(successfulSet);
        results.append(" files ready for streaming. ");
        if(failedSet.size() > 0) {
            results.append(failedSet.size());
            results.append(" files failed stream setup: ");
            for(String failedId : failedSet) {
                results.append(failedId);
                results.append(", ");
            }
        }
        results.trimToSize();

        return results.toString();
    }

}
