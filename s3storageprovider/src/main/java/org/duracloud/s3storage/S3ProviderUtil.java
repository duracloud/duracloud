/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.storage.error.StorageException;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.security.AWSCredentials;

import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.error.StorageException.RETRY;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderUtil {

    private static Map<String, AmazonS3Client> s3Clients =
        new HashMap<String, AmazonS3Client>();
    private static Map<String, CloudFrontService> cloudFrontServices =
        new HashMap<String, CloudFrontService>();
    private static Map<String, AmazonElasticMapReduceClient> emrClients =
        new HashMap<String, AmazonElasticMapReduceClient>();

    public static AmazonS3Client getAmazonS3Client(String accessKey,
                                                   String secretKey) {
        AmazonS3Client client = s3Clients.get(key(accessKey, secretKey));
        if (null == client) {
            client = newS3Client(accessKey, secretKey);
            s3Clients.put(key(accessKey, secretKey), client);
        }
        return client;
    }

    private static String key(String accessKey, String secretKey) {
        return accessKey + secretKey;
    }

    private static AmazonS3Client newS3Client(String accessKey,
                                              String secretKey) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,
                                                                     secretKey);
        try {
            return new AmazonS3Client(awsCredentials);
        } catch (AmazonServiceException e) {
            String err = "Could not create connection to Amazon S3 due " +
                         "to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    public static CloudFrontService getCloudFrontService(String accessKey,
                                                         String secretKey) {
        CloudFrontService service = cloudFrontServices.get(key(accessKey,
                                                               secretKey));
        if (null == service) {
            service = newCloudFrontService(accessKey, secretKey);
            cloudFrontServices.put(key(accessKey, secretKey), service);
        }
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

    public static AmazonElasticMapReduceClient getAmazonEMRClient(String accessKey,
                                                                  String secretKey) {
        AmazonElasticMapReduceClient client = emrClients.get(key(accessKey,
                                                                 secretKey));
        if(null == client) {
            client = newAmazonEMRClient(accessKey, secretKey);
            emrClients.put(key(accessKey, secretKey), client);
        }
        return client;
    }

    private static AmazonElasticMapReduceClient newAmazonEMRClient(String accessKey,
                                                                   String secretKey) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,
                                                                     secretKey);
        try {
            return new AmazonElasticMapReduceClient(awsCredentials);
        } catch (AmazonServiceException e) {
            String err = "Could not create connection to Amazon Elastic Map " +
                         "Reduce due to error: " + e.getMessage();
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
