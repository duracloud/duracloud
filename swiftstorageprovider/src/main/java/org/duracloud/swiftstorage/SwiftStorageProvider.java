/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.swiftstorage;

import static org.duracloud.common.error.RetryFlaggableException.NO_RETRY;
import static org.duracloud.common.error.RetryFlaggableException.RETRY;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_CHECKSUM;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MD5;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MODIFIED;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_SIZE;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_COUNT;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_CREATED;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.commons.lang.StringUtils;
import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
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

    private final Logger log =
        LoggerFactory.getLogger(SwiftStorageProvider.class);

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

            // Swift has no concept of bucket lifecycle

            return bucket;
        } catch (AmazonClientException e) {
            String err = "Could not create Swift container with name " + bucketName
                         + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    @Override
    public void removeSpace(String spaceId) {
        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);
        String propertiesBucketName = getBucketName(PROPERTIES_BUCKET);

        try {
            s3Client.deleteBucket(bucketName);
        } catch (AmazonClientException e) {
            String err = "Could not delete Swift container with name " + bucketName
                         + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }

        // Space properties are stored as tags with the S3 bucket.
        // So with Swift we need to delete the associated properties object in Swift.
        s3Client.deleteObject(propertiesBucketName, spaceId);
    }

    @Override
    public String createHiddenSpace(String spaceId, int expirationInDays) {
        String bucketName = getHiddenBucketName(spaceId);
        try {
            Bucket bucket = s3Client.createBucket(bucketName);
            return spaceId;
        } catch (AmazonClientException e) {
            String err = "Could not create Swift container with name " + bucketName
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

        // Will throw if bucket does not exist
        String propsBucketName = getBucketName(PROPERTIES_BUCKET);

        Map<String, String> spaceProperties = new HashMap<>();
        String spacePropertiesString;
        try {
            spacePropertiesString = s3Client.getObjectAsString(propsBucketName, spaceId);
            // Remove the {} from the string
            spacePropertiesString =
                spacePropertiesString.substring(1, spacePropertiesString.length() - 1);
            String[] spacePropertiesList = spacePropertiesString.split(", ");
            for (String property : spacePropertiesList) {
                String[] props = property.split("=");
                spaceProperties.put(props[0], props[1]);
            }
        } catch (AmazonS3Exception e) {
            // If no space properties have been set yet, then the object will not exist.
            // But we don't need to create it here, as it gets created when properties are set.
            log.debug(
                "Metadata object for space " + spaceId +
                " was not found in container " + propsBucketName +
                ", probably because this is a new space."
            );
        }

        // Handle @ symbol (change from +), to allow for email usernames in ACLs
        spaceProperties = replaceInMapValues(spaceProperties, "+", "@");

        // Add space count
        spaceProperties.put(PROPERTIES_SPACE_COUNT,
                            getSpaceCount(spaceId, MAX_ITEM_COUNT));
        return spaceProperties;
    }

    @Override
    protected void doSetSpaceProperties(String spaceId,
                                        Map<String, String> spaceProperties) {
        log.debug("setSpaceProperties(" + spaceId + ")");

        Map<String, String> originalProperties;
        try {
            originalProperties = getAllSpaceProperties(spaceId);
        } catch (NotFoundException e) {
            // The metadata bucket does not exist yet, so create it
            createHiddenSpace(PROPERTIES_BUCKET, 0);
            // And set the original properties to a new, empty HashMap
            originalProperties = new HashMap<>();
        }

        // By calling this _after_ we have requested the space properties,
        // we ensure that the metadata bucket exists.
        String metadataBucketName = getBucketName(PROPERTIES_BUCKET);

        // Set creation date
        String creationDate = originalProperties.get(PROPERTIES_SPACE_CREATED);
        if (creationDate == null) {
            creationDate = spaceProperties.get(PROPERTIES_SPACE_CREATED);
            if (creationDate == null) {
                // getCreationDate() does not work properly on Swift
                creationDate = formattedDate(new Date());
            }
        }
        spaceProperties.put(PROPERTIES_SPACE_CREATED, creationDate);

        // Handle @ symbol (change to +), to allow for email usernames in ACLs
        spaceProperties = replaceInMapValues(spaceProperties, "@", "+");

        // Store properties in an object in the hidden metadata bucket
        log.debug(
            "Writing space properties " + spaceProperties.toString() +
            " to object " + spaceId +
            " in Swift container " + metadataBucketName
        );
        s3Client.putObject(metadataBucketName, spaceId, spaceProperties.toString());
    }

    @Override
    protected void updateObjectProperties(String bucketName,
                                          String contentId,
                                          ObjectMetadata objMetadata) {
        try {
            AccessControlList originalACL =
                s3Client.getObjectAcl(bucketName, contentId);
            CopyObjectRequest copyRequest = new CopyObjectRequest(bucketName,
                                                                  contentId,
                                                                  bucketName,
                                                                  contentId);
            copyRequest.setStorageClass(DEFAULT_STORAGE_CLASS);
            copyRequest.setNewObjectMetadata(objMetadata);
            // Setting object ACLs resets an object's ContentType to application/xml!
            // But setting the ACLs before we do the copy request gets around this.
            copyRequest.setAccessControlList(originalACL);
            s3Client.copyObject(copyRequest);
        } catch (AmazonClientException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not update metadata for content " + contentId + " in Swift container " +
                         bucketName + " due to error: " + e.getMessage();
            throw new StorageException(err, e, NO_RETRY);
        }
    }

    @Override
    protected Map<String, String> prepContentProperties(ObjectMetadata objMetadata) {
        Map<String, String> contentProperties = new HashMap<>();

        // Set the user properties
        Map<String, String> userProperties = objMetadata.getUserMetadata();
        for (String metaName : userProperties.keySet()) {
            String metaValue = userProperties.get(metaName);
            if (metaName.trim().equalsIgnoreCase("tags") ||
                metaName.trim().equalsIgnoreCase("tags" + HEADER_KEY_SUFFIX) ||
                metaName.trim().equalsIgnoreCase(PROPERTIES_CONTENT_MIMETYPE) ||
                metaName.trim().equalsIgnoreCase(PROPERTIES_CONTENT_MIMETYPE + HEADER_KEY_SUFFIX)) {
                metaName = metaName.toLowerCase();
            }
            contentProperties.put(getWithSpace(decodeHeaderKey(metaName)), decodeHeaderValue(metaValue));
        }

        // Set the response metadata
        Map<String, Object> responseMeta = objMetadata.getRawMetadata();
        for (String metaName : responseMeta.keySet()) {
            // Don't include Swift response headers
            try {
                if (!isSwiftMetadata(metaName)) {
                    Object metaValue = responseMeta.get(metaName);
                    contentProperties.put(metaName, String.valueOf(metaValue));
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            // Remove User Response headers that are also in RawMetadata
            // Swift metadata are non-standard HTTP headers so DuraCloud views them as "User" metadata
            if (userProperties.keySet()
                              .contains(metaName + HEADER_KEY_SUFFIX) && contentProperties.containsKey(metaName)) {
                contentProperties.remove(metaName);
            }
        }

        // Set MIMETYPE
        String contentType = objMetadata.getContentType();
        if (contentType != null) {
            contentProperties.put(PROPERTIES_CONTENT_MIMETYPE, contentType);
            contentProperties.put(Headers.CONTENT_TYPE, contentType);
        }

        // Set CONTENT_ENCODING
        String encoding = objMetadata.getContentEncoding();
        if (encoding != null) {
            contentProperties.put(Headers.CONTENT_ENCODING, encoding);
        }

        // Set SIZE
        long contentLength = objMetadata.getContentLength();
        if (contentLength >= 0) {
            String size = String.valueOf(contentLength);
            contentProperties.put(PROPERTIES_CONTENT_SIZE, size);
            contentProperties.put(Headers.CONTENT_LENGTH, size);
        }

        // Set CHECKSUM
        String checksum = objMetadata.getETag();
        if (checksum != null) {
            String eTagValue = getETagValue(checksum);
            contentProperties.put(PROPERTIES_CONTENT_CHECKSUM, eTagValue);
            contentProperties.put(PROPERTIES_CONTENT_MD5, eTagValue);
            contentProperties.put(Headers.ETAG, eTagValue);
        }

        // Set MODIFIED
        Date modified = objMetadata.getLastModified();
        if (modified != null) {
            String modDate = formattedDate(modified);
            contentProperties.put(PROPERTIES_CONTENT_MODIFIED, modDate);
            contentProperties.put(Headers.LAST_MODIFIED, modDate);
        }

        return contentProperties;
    }

    private String truncateKey(String accessKey)  {
        // Convert access key to 20 character string
        return StringUtils.left(accessKey, 20);
    }

    /**
     * Return true iff metaName is NOT a standard HTTP Header
     * @param metaName
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private boolean isSwiftMetadata(String metaName) {
        Field[] httpFields = HttpHeaders.class.getFields();
        for (Field f : httpFields) {
            String fieldName = null;
            try {
                fieldName = (String) f.get(httpFields);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (metaName.equalsIgnoreCase(fieldName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add expire header for object in Swift.
     * @param bucketName
     * @param contentId
     * @param seconds
     */
    public ObjectMetadata expireObject(String bucketName, String contentId, Integer seconds) {
        log.debug("Expiring object {} in {} after {} seconds.", contentId, bucketName, seconds);
        ObjectMetadata objMetadata = getObjectDetails(bucketName, contentId, true);
        objMetadata.setHeader("X-Delete-After", seconds);
        updateObjectProperties(bucketName, contentId, objMetadata);

        return objMetadata;
    }

    private ObjectMetadata getObjectDetails(String bucketName, String contentId, boolean retry) {
        try {
            return s3Client.getObjectMetadata(bucketName, contentId);
        } catch (AmazonClientException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not get details for content " + contentId + " in Swift container " + bucketName
                + " due to error: " + e.getMessage();
            throw new StorageException(err, e, retry);
        }
    }

}