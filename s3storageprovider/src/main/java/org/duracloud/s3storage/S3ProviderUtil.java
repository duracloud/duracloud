/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.duracloud.storage.error.StorageException;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.error.StorageException.RETRY;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderUtil {

    private static Map<String, S3Service> s3Services = new HashMap<String, S3Service>();
    private static Map<String, CloudFrontService> cloudFrontServices = new HashMap<String, CloudFrontService>();

    public static S3Service getS3Service(String accessKey, String secretKey) {
        S3Service service = s3Services.get(key(accessKey, secretKey));
        if (null == service) {
            service = newS3Service(accessKey, secretKey);
        }

        s3Services.put(key(accessKey, secretKey), service);
        return service;
    }

    private static String key(String accessKey, String secretKey) {
        return accessKey + secretKey;
    }

    private static S3Service newS3Service(String accessKey, String secretKey) {
        S3Service service;
        AWSCredentials awsCredentials = new AWSCredentials(accessKey,
                                                           secretKey);
        try {
            service = new RestS3Service(awsCredentials);
        } catch (S3ServiceException e) {
            String err =
                "Could not create connection to Amazon S3 due " + "to error: " +
                    e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
        return service;
    }

    public static CloudFrontService getCloudFrontService(String accessKey,
                                                         String secretKey) {
        CloudFrontService service = cloudFrontServices.get(key(accessKey,
                                                               secretKey));
        if (null == service) {
            service = newCloudFrontService(accessKey, secretKey);
        }

        cloudFrontServices.put(key(accessKey, secretKey), service);
        return service;
    }

    private static CloudFrontService newCloudFrontService(String accessKey,
                                                          String secretKey) {
        AWSCredentials awsCredentials = new AWSCredentials(accessKey,
                                                           secretKey);
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
