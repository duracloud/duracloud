/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.GrantAndPermission;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.model.cloudfront.DistributionConfig;
import org.jets3t.service.model.cloudfront.OriginAccessIdentity;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class EnableStreamingTaskRunner extends BaseStreamingTaskRunner  {

    private final Logger log = LoggerFactory.getLogger(EnableStreamingTaskRunner.class);    

    private static final String TASK_NAME = "enable-streaming";

    public EnableStreamingTaskRunner(S3StorageProvider s3Provider,
                                     S3Service s3Service,
                                     CloudFrontService cfService) {
        this.s3Provider = s3Provider;
        this.s3Service = s3Service;
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
                                                                    true);
                    }
                    domainName = existingDist.getDomainName();
                } else {
                    distId = null;
                }
            }

            if(distId == null) { // No existing distribution, need to create
                oaIdentityId = getOriginAccessId();
                String origin = cfService.sanitizeS3BucketName(bucketName);
                StreamingDistribution dist =
                    cfService.createStreamingDistribution(origin,
                                                          null,
                                                          null,
                                                          null,
                                                          true,
                                                          oaIdentityId,
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
     * Attempts to get the origin access ID from an existing streaming
     * distribution
     */
    private String getDistributionOriginAccessId(String distributionId)
        throws CloudFrontServiceException {
        StreamingDistributionConfig config =
            cfService.getStreamingDistributionConfig(distributionId);
        return config.getOriginAccessIdentity();
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
            DistributionConfig.ORIGIN_ACCESS_IDENTITY_PREFIX);

        // Get the origin access identity to use
        OriginAccessIdentity oaIdentity =
            cfService.getOriginAccessIdentity(oaIdentityId);
        String s3UserId = oaIdentity.getS3CanonicalUserId();
        CanonicalGrantee s3Grantee = new CanonicalGrantee(s3UserId);

        // Get a list of items in the space
        Iterator<String> contentIds =
            s3Provider.getSpaceContents(spaceId, null);

        // Attempt to set a new ACL permission allowing read for origin
        // access identity to each content item
        int successfulSet = 0;
        List<String> failedSet = new ArrayList<String>();
        while(contentIds.hasNext()) {
            String contentId = contentIds.next();
            try {
                AccessControlList contentACL =
                    s3Service.getObjectAcl(bucketName, contentId);

                // Determine if grant already exists
                boolean grantExists = checkACL(contentACL,
                                               s3Grantee.getIdentifier(),
                                               Permission.PERMISSION_READ);

                if(!grantExists) {
                    contentACL.grantPermission(s3Grantee,
                                               Permission.PERMISSION_READ);
                    s3Service.putObjectAcl(bucketName, contentId, contentACL);
                }
                successfulSet++;
            } catch(S3ServiceException e) {
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

    /*
     * Determines if the provided ACL includes the given permission for
     * the given grantee
     */
    private boolean checkACL(AccessControlList acl,
                             String granteeId,
                             Permission permission) {
        Set<GrantAndPermission> grants =
            (Set<GrantAndPermission>) acl.getGrants();
        for(GrantAndPermission grant : grants) {
            if(granteeId.equals(grant.getGrantee().getIdentifier())) {
                if(permission.equals(grant.getPermission())) {
                    return true;
                }
            }
        }
        return false;
    }

}
