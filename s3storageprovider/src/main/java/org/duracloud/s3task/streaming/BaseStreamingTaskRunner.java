/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Permission;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.S3Origin;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author: Bill Branan
 * Date: Jun 1, 2010
 */
public abstract class BaseStreamingTaskRunner implements TaskRunner {

    private final Logger log =
        LoggerFactory.getLogger(BaseStreamingTaskRunner.class);

    protected static final int maxRetries = 8;

    protected S3StorageProvider s3Provider;
    protected AmazonS3Client s3Client;
    protected CloudFrontService cfService;

    public abstract String getName();

    public abstract String performTask(String taskParameters);

    /*
     * Extracts the spaceId value from the provided task parameters
     */
    protected String getSpaceId(String taskParameters) {
        if(taskParameters != null && !taskParameters.equals("")) {
            return taskParameters;
        } else {
            throw new RuntimeException("A Space ID must be provided");
        }
    }

    /*
     * Determines if a streaming distribution already exists for a given bucket
     */
    protected StreamingDistribution getExistingDistribution(String bucketName)
        throws CloudFrontServiceException {

        StreamingDistribution[] distributions =
            cfService.listStreamingDistributions();

        if(distributions != null) {
            for(StreamingDistribution dist : distributions) {
                if(isDistFromBucket(bucketName, dist)) {
                    return dist;
                }
            }
        }

        return null;
    }

    private boolean isDistFromBucket(String bucketName,
                                     StreamingDistribution dist) {
        S3Origin origin = (S3Origin)dist.getOrigin();
        return bucketName.equals(origin.getOriginAsBucketName());
    }

    /*
     * Determines if a streaming distribution already exists for a given bucket
     */
    protected List<StreamingDistribution> getAllExistingDistributions(String bucketName)
        throws CloudFrontServiceException {

        StreamingDistribution[] distributions =
            cfService.listStreamingDistributions();

        List<StreamingDistribution> distList =
            new ArrayList<StreamingDistribution>();

        for(StreamingDistribution dist : distributions) {
            if(isDistFromBucket(bucketName, dist)) {
                distList.add(dist);
            }
        }

        return distList;
    }

    /*
     * Get a listing of items in a space
     */
    protected Iterator<String> getSpaceContents(String spaceId) {
        for(int i=0; i<maxRetries; i++) {
            try {
                return s3Provider.getSpaceContents(spaceId, null);
            } catch(Exception e) {
                log.warn("Exception encountered attempting to get contents " +
                         "for streaming space: " + spaceId +
                         ", error message: " + e.getMessage());
                wait(i);
            }
        }
        throw new DuraCloudRuntimeException("Exceeded retries attempting to " +
                                            "get space contents for " + spaceId);
    }

    /*
     * Attempts to get the origin access ID from an existing streaming
     * distribution
     */
    protected String getDistributionOriginAccessId(String distributionId)
        throws CloudFrontServiceException {
        StreamingDistributionConfig config =
            cfService.getStreamingDistributionConfig(distributionId);
        S3Origin origin = (S3Origin)config.getOrigin();
        return origin.getOriginAccessIdentity();
    }

    /*
     * Sets access control for a given content ID in a given bucket to
     * be available to a specific grantee.
     */
    protected void setACL(String bucketName,
                          String contentId,
                          CanonicalGrantee s3Grantee)
        throws DuraCloudCheckedException {
        for(int i=0; i<maxRetries; i++) {
            try {
                AccessControlList contentACL =
                    s3Client.getObjectAcl(bucketName, contentId);

                // Determine if grant already exists
                boolean grantExists = checkACL(contentACL,
                                               s3Grantee.getIdentifier(),
                                               Permission.Read);
                if(!grantExists) {
                    contentACL.grantPermission(s3Grantee,
                                               Permission.Read);
                    s3Client.setObjectAcl(bucketName, contentId, contentACL);
                }
                return;
            } catch(AmazonServiceException e) {
                log.warn("Exception encountered attempting to set streaming " +
                         "ACL for S3 content: " + contentId + " in bucket: " +
                         bucketName + ", error: " + e.getMessage());
                wait(i);
            }
            throw new DuraCloudCheckedException(
                "Unable to set streaming ACL for " + contentId +
                " in bucket " + bucketName);
        }
    }

    /*
     * Determines if the provided ACL includes the given permission for
     * the given grantee
     */
    private boolean checkACL(AccessControlList acl,
                             String granteeId,
                             Permission permission) {
        Set<Grant> grants = acl.getGrants();
        for(Grant grant : grants) {
            if(granteeId.equals(grant.getGrantee().getIdentifier())) {
                if(permission.equals(grant.getPermission())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void wait(int index) {
        try {
            Thread.sleep(1000 * index);
        } catch(InterruptedException e) {
        }
    }

}
