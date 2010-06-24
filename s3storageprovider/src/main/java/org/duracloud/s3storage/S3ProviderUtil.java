/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.duracloud.storage.error.StorageException;
import static org.duracloud.storage.error.StorageException.RETRY;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderUtil {

    public static S3Service getS3Service(String accessKey, String secretKey) {
        AWSCredentials awsCredentials = new AWSCredentials(accessKey, secretKey);
        try {
            return new RestS3Service(awsCredentials);
        } catch (S3ServiceException e) {
            String err = "Could not create connection to Amazon S3 due " +
                         "to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    public static CloudFrontService getCloudFrontService(String accessKey,
                                                         String secretKey) {
        AWSCredentials awsCredentials = new AWSCredentials(accessKey, secretKey);
        try {
            return new CloudFrontService(awsCredentials);
        } catch (CloudFrontServiceException e) {
            String err = "Could not create connection to Amazon CloudFront " +
                         "due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * Converts a provided space ID into a valid and unique S3 bucket name.
     *
     * @param spaceId
     * @return
     */
    public static String getBucketName(String accessKeyId, String spaceId) {
        String bucketName = accessKeyId + "." + spaceId;
        bucketName = bucketName.toLowerCase();
        bucketName = bucketName.replaceAll("[^a-z0-9-.]", "-");

        // Remove duplicate separators (. and -)
        while (bucketName.contains("--") || bucketName.contains("..")
                || bucketName.contains("-.") || bucketName.contains(".-")) {
            bucketName = bucketName.replaceAll("[-]+", "-");
            bucketName = bucketName.replaceAll("[.]+", ".");
            bucketName = bucketName.replaceAll("-[.]", "-");
            bucketName = bucketName.replaceAll("[.]-", ".");
        }

        if (bucketName.length() > 63) {
            bucketName = bucketName.substring(0, 63);
        }
        while (bucketName.endsWith("-") || bucketName.endsWith(".")) {
            bucketName = bucketName.substring(0, bucketName.length() - 1);
        }
        return bucketName;
    }  

}
