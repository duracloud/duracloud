/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import static org.duracloud.common.error.RetryFlaggableException.RETRY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.duracloud.storage.domain.StorageAccount.OPTS;
import org.duracloud.storage.error.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderUtil {

    private static final Logger log = LoggerFactory.getLogger(S3ProviderUtil.class);

    private static Map<String, AmazonS3> s3Clients = new HashMap<>();
    private static Map<String, AmazonCloudFrontClient> cloudFrontClients = new HashMap<>();

    private S3ProviderUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public static AmazonS3 getAmazonS3Client(String accessKey,
                                             String secretKey,
                                             Map<String, String> options) {
        AmazonS3 client = s3Clients.get(key(accessKey, secretKey, options));
        if (null == client) {
            Region region = null;
            URL endpoint = null;
            String signer = null;
            if (options != null) {
                if (options.get(OPTS.SWIFT_S3_ENDPOINT.name()) != null) {
                    try {
                        endpoint = new URL(options.get(OPTS.SWIFT_S3_ENDPOINT.name()));
                    } catch (MalformedURLException e) {
                        String err = "The provided Swift S3 Endpoint URL is invalid: " + e.getMessage();
                        throw new StorageException(err, e);
                    }
                    if (options.get(OPTS.SWIFT_S3_SIGNER_TYPE.name()) != null) {
                        signer = options.get(OPTS.SWIFT_S3_SIGNER_TYPE.name());
                    }
                } else if (options.get(OPTS.AWS_REGION.name()) != null) {
                    region = com.amazonaws.services.s3.model.Region.fromValue(
                            options.get(OPTS.AWS_REGION.name())).toAWSRegion();
                }
            }
            client = newS3Client(accessKey, secretKey, region, endpoint, signer);
            s3Clients.put(key(accessKey, secretKey, options), client);
        }
        return client;
    }

    private static String key(String accessKey, String secretKey, Map<String, String> options) {
        String optionsHash = "";
        if (null != options && options.size() > 0) {
            optionsHash = Integer.toString(options.hashCode());
        }
        return accessKey + secretKey + optionsHash;
    }

    private static AmazonS3 newS3Client(String accessKey,
                                        String secretKey,
                                        Region region,
                                        URL endpoint,
                                        String signer) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = null;
        String awsRegion = null;
        try {
            if (endpoint == null) {
                if (region != null) {
                    awsRegion = region.getName();
                } else {
                    awsRegion = System.getProperty(OPTS.AWS_REGION.name());
                }
                // Construct a normal client that connects to AWS.
                log.debug("Creating AWS S3 Client.");
                s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(awsRegion)
                    .build();
            } else {
                // Construct a client that will work with S3+Swift
                log.debug("Creating Swift S3 client.");
                String protocol = endpoint.getProtocol();
                String host = endpoint.getAuthority();
                String ref = endpoint.getRef();
                String endpointString = protocol + "://" + host;
                ClientConfiguration clientConfiguration = new ClientConfiguration();
                clientConfiguration.setProtocol(Protocol.valueOf(protocol.toUpperCase()));

                if (signer != null) {
                    clientConfiguration.setSignerOverride(signer);
                }

                if (ref == null) {
                    // Still must be a valid AWS region for certain signer types,
                    // even though it isn't used.
                    awsRegion = "us-east-1";
                } else {
                    awsRegion = ref;
                }

                s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointString, awsRegion))
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(clientConfiguration)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .build();
            }
            return s3Client;
        } catch (AmazonServiceException e) {
            String connType = endpoint == null ? "Amazon" : "Swift";
            String err = "Could not create connection to " + connType + " S3 due "
                + "to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    public static AmazonCloudFrontClient getAmazonCloudFrontClient(String accessKey,
                                                                   String secretKey) {
        AmazonCloudFrontClient client =
            cloudFrontClients.get(key(accessKey, secretKey, null));
        if (null == client) {
            client = newAmazonCloudFrontClient(accessKey, secretKey);
            cloudFrontClients.put(key(accessKey, secretKey, null), client);
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
     * @param s3Url using the s3://bucket/object syntax.
     * @return
     * @throws IOException
     */
    public static Resource getS3ObjectByUrl(String s3Url) throws IOException {
        AmazonS3 client = AmazonS3ClientBuilder.standard().build();
        AmazonS3URI s3Uri = new AmazonS3URI(s3Url);
        S3Object s3Obj = client.getObject(new GetObjectRequest(s3Uri.getBucket(), s3Uri.getKey()));
        s3Obj.getObjectContent();
        Resource resource = new InputStreamResource(s3Obj.getObjectContent());
        return resource;
    }
}
