/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.duracloud.s3storage.S3ProviderUtil;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.cloudfront.OriginAccessIdentity;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class DisableStreamingTaskRunner extends BaseStreamingTaskRunner {

    private final Logger log = LoggerFactory.getLogger(DisableStreamingTaskRunner.class);    

    private static final String TASK_NAME = "disable-streaming";

    public DisableStreamingTaskRunner(S3StorageProvider s3Provider,
                                      S3Service s3Service,
                                      CloudFrontService cfService) {
        this.s3Provider = s3Provider;
        this.s3Service = s3Service;
        this.cfService = cfService;
    }

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {
        String spaceId = getSpaceId(taskParameters);
        log.info("Performing " + TASK_NAME + " task on space " + spaceId);        

        String bucketName = s3Provider.getBucketName(spaceId);
        String results;

        try {
            // Ensure that there is an existing distribution for the given space
            StreamingDistribution existingDist =
                getExistingDistribution(bucketName);

            String setAclResults;
            if(existingDist != null) {
                setAclResults = resetContentAccess(spaceId, bucketName);
            } else {
                throw new RuntimeException("No streaming distribution " +
                                           "exists for space " + spaceId);
            }

            results = "Disable Streaming Task completed. " + setAclResults;
        } catch(CloudFrontServiceException e) {
            log.warn("Error encountered running " + TASK_NAME + " task: " +
                     e.getMessage(), e);            
            results = "Disable Streaming Task failed due to: " + e.getMessage();
        }

        log.debug("Result of " + TASK_NAME + " task: " + results);        
        return results;
    }

    /*
     * Resets access permissions on each item in a space to private, i.e.
     * access only through DuraCloud and not via CloudFront.
     *
     * @return results of the ACL setting activity
     */
    private String resetContentAccess(String spaceId, String bucketName) {

        // Get a list of items in the space
        Iterator<String> contentIds =
            s3Provider.getSpaceContents(spaceId, null);

        AccessControlList bucketAcl;
        try {
            bucketAcl = s3Service.getBucketAcl(bucketName);
        } catch(S3ServiceException e) {
            throw new RuntimeException("Unable to retrieve ACL for bucket " +
                bucketName + " due to: " + e.getMessage(), e);            
        }

        // Attempt to set private ACL permission
        int successfulSet = 0;
        List<String> failedSet = new ArrayList<String>();
        while(contentIds.hasNext()) {
            String contentId = contentIds.next();

            try {
                s3Service.putObjectAcl(bucketName,
                                       contentId,
                                       bucketAcl);                
                successfulSet++;
            } catch(S3ServiceException e) {
                log.error("Error setting ACL for object " + contentId + ": " +
                          e.getMessage(), e);
                failedSet.add(contentId);
            }
        }

        // Build results
        StringBuilder results = new StringBuilder();
        results.append("Streaming for ");
        results.append(successfulSet);
        results.append(" files disabled. ");
        if(failedSet.size() > 0) {
            results.append(failedSet.size());
            results.append(" files failed and may remain available: ");
            for(String failedId : failedSet) {
                results.append(failedId);
                results.append(", ");
            }
        }
        results.trimToSize();

        return results.toString();
    }

}