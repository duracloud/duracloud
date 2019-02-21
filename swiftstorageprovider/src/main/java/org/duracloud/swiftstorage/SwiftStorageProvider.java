/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.swiftstorage;

import static org.duracloud.common.error.RetryFlaggableException.RETRY;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.apache.commons.lang.StringUtils;
import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides content storage backed by OpenStack Swift with S3 API middleware.
 *
 * @author Andy Foster
 * Date: Feb 25, 2019
 */
public class SwiftStorageProvider extends S3StorageProvider {

    private final Logger log = LoggerFactory.getLogger(SwiftStorageProvider.class);

    public SwiftStorageProvider(String accessKey, String secretKey, Map<String, String> options) {
        super(accessKey, secretKey, options);
    }

    public SwiftStorageProvider(AmazonS3 s3Client, String accessKey) {
        super(s3Client, accessKey, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StorageProviderType getStorageProviderType() {
        return StorageProviderType.SWIFT_S3;
    }

    @Override
    protected Bucket createBucket(String spaceId) {
        String bucketName = getNewBucketName(spaceId);
        try {
            Bucket bucket = s3Client.createBucket(bucketName);

            return bucket;
        } catch (AmazonClientException e) {
            String err = "Could not create S3 bucket with name " + bucketName
                         + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    // Swift access keys are longer than 20 characters, and creating
    // a bucket starting with your access key causes problems.
    @Override
    protected String getNewBucketName(String spaceId) {
        String truncatedKey = truncateKey(accessKeyId);
        return S3ProviderUtil.createNewBucketName(truncatedKey, spaceId);
    }

    @Override
    protected String getSpaceId(String bucketName) {
        String spaceId = bucketName;
        String truncatedKey = truncateKey(accessKeyId);
        if (isSpace(bucketName)) {
            spaceId = spaceId.substring(truncatedKey.length() + 1);
        }
        return spaceId;
    }

    @Override
    protected Map<String, String> getAllSpaceProperties(String spaceId) {
        log.debug("getAllSpaceProperties(" + spaceId + ")");

        // Swift has no concept of tags, so just return an empty map.
        Map<String, String> spaceProperties = new HashMap<>();
        return spaceProperties;
    }

    @Override
    protected void doSetSpaceProperties(String spaceId,
                                        Map<String, String> spaceProperties) {
        log.debug("setSpaceProperties(" + spaceId + ")");

        // Swift has no concept of tags.

    }

    private String truncateKey(String accessKey)  {
        // Convert access key to 20 character string
        return StringUtils.left(accessKey, 20);
    }
}
