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
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.duracloud.storage.error.StorageException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.error.StorageException.RETRY;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderUtil {

    private static Map<String, AmazonS3Client> s3Clients = new HashMap<>();
    private static Map<String, AmazonCloudFrontClient> cloudFrontClients = new HashMap<>();

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

    public static AmazonCloudFrontClient getAmazonCloudFrontClient(String accessKey,
                                                                   String secretKey) {
        AmazonCloudFrontClient client =
            cloudFrontClients.get(key(accessKey, secretKey));
        if (null == client) {
            client = newAmazonCloudFrontClient(accessKey, secretKey);
            cloudFrontClients.put(key(accessKey, secretKey), client);
        }
        return client;
    }

    private static AmazonCloudFrontClient newAmazonCloudFrontClient(String accessKey,
                                                                    String secretKey) {
        BasicAWSCredentials awsCredentials =
            new BasicAWSCredentials(accessKey, secretKey);
        try {
            return new AmazonCloudFrontClient(awsCredentials);
        } catch (AmazonServiceException e) {
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
    public static String createNewBucketName(String accessKeyId,
                                             String spaceId) {
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

    /**
     * 
     * @param s3Url using the s3://bucket/object syntax.
     * @return
     * @throws IOException
     */
    public static Resource getS3ObjectByUrl(String s3Url) throws IOException {
        AmazonS3Client client = new AmazonS3Client();
        AmazonS3URI s3Uri = new AmazonS3URI(s3Url);
        S3Object s3Obj = client.getObject(new GetObjectRequest(s3Uri.getBucket(), s3Uri.getKey()));
        s3Obj.getObjectContent();
        Resource resource =   new InputStreamResource(s3Obj.getObjectContent());
        return resource;
    }
}
