/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.TagSet;
import org.apache.commons.lang.StringUtils;
import org.duracloud.common.model.AclType;
import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.ChecksumMismatchException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.duracloud.storage.util.StorageProviderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;

/**
 * Provides content storage backed by Amazon's Simple Storage Service.
 *
 * @author Bill Branan
 */
public class S3StorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(S3StorageProvider.class);

    protected static final int MAX_ITEM_COUNT = 1000;
    private static final StorageClass DEFAULT_STORAGE_CLASS =
        StorageClass.Standard;

    private String accessKeyId = null;
    protected AmazonS3Client s3Client = null;

    public S3StorageProvider(String accessKey, String secretKey) {
        this(S3ProviderUtil.getAmazonS3Client(accessKey, secretKey),
             accessKey,
             null);
    }

    public S3StorageProvider(String accessKey,
                             String secretKey,
                             Map<String, String> options) {
        this(S3ProviderUtil.getAmazonS3Client(accessKey, secretKey),
             accessKey,
             options);
    }

    public S3StorageProvider(AmazonS3Client s3Client,
                             String accessKey,
                             Map<String, String> options) {
        this.accessKeyId = accessKey;
        this.s3Client = s3Client;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpaces()");

        List<String> spaces = new ArrayList<>();
        List<Bucket> buckets = listAllBuckets();
        for (Bucket bucket : buckets) {
            String bucketName = bucket.getName();
            if (isSpace(bucketName)) {
                spaces.add(getSpaceId(bucketName));
            }
        }

        // sort after the bucket prefix has been stripped off
        Collections.sort(spaces);

        return spaces.iterator();
    }

    private List<Bucket> listAllBuckets() {
        try {
            return s3Client.listBuckets();
        }
        catch (AmazonClientException e) {
            String err = "Could not retrieve list of S3 buckets due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaceContents(String spaceId,
                                             String prefix) {
        log.debug("getSpaceContents(" + spaceId + ", " + prefix);

        throwIfSpaceNotExist(spaceId);
        return new ContentIterator(this, spaceId, prefix);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getSpaceContentsChunked(String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker) {
        log.debug("getSpaceContentsChunked(" + spaceId + ", " + prefix + ", " +
                                           maxResults + ", " + marker + ")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        if(maxResults <= 0) {
            maxResults = StorageProvider.DEFAULT_MAX_RESULTS;
        }

        return getCompleteBucketContents(bucketName, prefix, maxResults, marker);
    }

    private List<String> getCompleteBucketContents(String bucketName,
                                                   String prefix,
                                                   long maxResults,
                                                   String marker) {
        List<String> contentItems = new ArrayList<>();

        List<S3ObjectSummary> objects =
            listObjects(bucketName, prefix, maxResults, marker);
        for (S3ObjectSummary object : objects) {
            contentItems.add(object.getKey());
        }
        return contentItems;
    }

    private List<S3ObjectSummary> listObjects(String bucketName,
                                              String prefix,
                                              long maxResults,
                                              String marker) {
        int numResults = new Long(maxResults).intValue();
        ListObjectsRequest request =
            new ListObjectsRequest(bucketName, prefix, marker, null, numResults);
        try {
            ObjectListing objectListing = s3Client.listObjects(request);
            return objectListing.getObjectSummaries();
        } catch (AmazonClientException e) {
            String err = "Could not get contents of S3 bucket " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    protected boolean spaceExists(String spaceId) {
        try {
            getBucketName(spaceId);
            return true;
        } catch(NotFoundException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createSpace(String spaceId) {
        log.debug("createSpace(" + spaceId + ")");
        throwIfSpaceExists(spaceId);

        Bucket bucket = createBucket(spaceId);

        Date created = bucket.getCreationDate();
        if(created == null) {
            created = new Date();
        }

        // Empty ACL set for new space (no permissions set)
        Map<String, AclType> spaceACLs = new HashMap<>();

        // Add space properties
        Map<String, String> spaceProperties = new HashMap<>();
        spaceProperties.put(PROPERTIES_SPACE_CREATED, formattedDate(created));

        try {
            setNewSpaceProperties(spaceId, spaceProperties, spaceACLs);
        } catch(StorageException e) {
            removeSpace(spaceId);
            String err = "Unable to create space due to: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    private Bucket createBucket(String spaceId) {
        String bucketName = getNewBucketName(spaceId);
        try {
            Bucket bucket = s3Client.createBucket(bucketName);

            // Apply lifecycle config to bucket
            StoragePolicy storagePolicy = getStoragePolicy();
            if(null != storagePolicy) {
                setSpaceLifecycle(bucketName, storagePolicy.getBucketLifecycleConfig());
            }

            return bucket;
        } catch (AmazonClientException e) {
            String err = "Could not create S3 bucket with name " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * Defines the storage policy for the primary S3 provider.
     * Subclasses can define different policy choices.
     *
     * @return storage policy to set, or null if no policy should be defined
     */
    protected StoragePolicy getStoragePolicy() {
        return new StoragePolicy(StorageClass.StandardInfrequentAccess, 30);
    }

    /**
     * Sets a lifecycle policy on an S3 bucket based on the given configuration
     *
     * @param bucketName name of the bucket to update
     * @param config bucket lifecycle configuration
     */
    public void setSpaceLifecycle(String bucketName,
                                  BucketLifecycleConfiguration config) {
        boolean success = false;
        int maxLoops = 6;
        for (int loops = 0; !success && loops < maxLoops; loops++) {
            try {
                s3Client.deleteBucketLifecycleConfiguration(bucketName);
                s3Client.setBucketLifecycleConfiguration(bucketName, config);
                success = true;
            } catch (NotFoundException e) {
                success = false;
                wait(loops);
            }
        }

        if (!success) {
            throw new StorageException(
                "Lifecycle policy for bucket " + bucketName +
                " could not be applied. The space cannot be found.");
        }
    }

    protected String getNewBucketName(String spaceId) {
        return S3ProviderUtil.createNewBucketName(accessKeyId, spaceId);
    }

     private String formattedDate(Date date) {
         return DateUtil.convertToString(date.getTime());
    }

    /**
     * {@inheritDoc}
     */
    public void removeSpace(String spaceId) {
        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        try {
            s3Client.deleteBucket(bucketName);
        } catch (AmazonClientException e) {
            String err = "Could not delete S3 bucket with name " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Map<String, String> getAllSpaceProperties(String spaceId) {
        log.debug("getAllSpaceProperties(" + spaceId + ")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        // Retrieve space properties from bucket tags
        Map<String, String> spaceProperties = new HashMap<>();
        BucketTaggingConfiguration tagConfig =
            s3Client.getBucketTaggingConfiguration(bucketName);
        if(null != tagConfig) {
            for(TagSet tagSet : tagConfig.getAllTagSets()) {
                spaceProperties.putAll(tagSet.getAllTags());
            }
        }

        // Handle @ symbol (change from +), to allow for email usernames in ACLs
        spaceProperties = replaceInMapValues(spaceProperties, "+", "@");

        // Add space count
        spaceProperties.put(PROPERTIES_SPACE_COUNT,
                            getSpaceCount(spaceId, MAX_ITEM_COUNT));

        return spaceProperties;
    }

    /*
     * Counts the number of items in a space up to the maxCount. If maxCount
     * is reached or exceeded, the returned string will indicate this with a
     * trailing '+' character (e.g. 1000+).
     *
     * Note that anecdotal evidence shows that this method of counting
     * (using size of chunked calls) is faster in most cases than enumerating
     * the Iteration: StorageProviderUtil.count(getSpaceContents(spaceId, null))
     */
    protected String getSpaceCount(String spaceId, int maxCount) {
        List<String> spaceContentChunk = null;
        long count = 0;

        do {
            String marker = null;
            if (spaceContentChunk != null && spaceContentChunk.size() > 0) {
                marker = spaceContentChunk.get(spaceContentChunk.size() - 1);
            }
            spaceContentChunk = getSpaceContentsChunked(spaceId,
                                                        null,
                                                        MAX_ITEM_COUNT,
                                                        marker);
            count += spaceContentChunk.size();
        } while (spaceContentChunk.size() > 0 && count < maxCount);

        String suffix = "";
        if(count >= maxCount) {
            suffix = "+";
        }
        return String.valueOf(count) + suffix;
    }    

    private String getBucketCreationDate(String bucketName) {
        Date created = null;
        try {
            List<Bucket> buckets = s3Client.listBuckets();
            for(Bucket bucket : buckets) {
                if(bucket.getName().equals(bucketName)) {
                    created = bucket.getCreationDate();
                }
            }
        } catch (AmazonClientException e) {
            String err = "Could not retrieve S3 bucket listing due to error: " +
                         e.getMessage();
            throw new StorageException(err, e, RETRY);
        }

        String formattedDate = null;
        if(created != null) {
            formattedDate = formattedDate(created);
        } else {
            formattedDate = "unknown";
        }
        return formattedDate;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetSpaceProperties(String spaceId,
                                        Map<String, String> spaceProperties) {
        log.debug("setSpaceProperties(" + spaceId + ")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        Map<String, String> originalProperties;
        try {
            originalProperties = getAllSpaceProperties(spaceId);
        } catch(NotFoundException e) {
            // Likely adding a new space, so no existing properties yet.
            originalProperties = new HashMap<>();
        }

        // Set creation date
        String creationDate = originalProperties.get(PROPERTIES_SPACE_CREATED);
        if(creationDate == null) {
            creationDate = spaceProperties.get(PROPERTIES_SPACE_CREATED);
            if(creationDate == null) {
                creationDate = getBucketCreationDate(bucketName);
            }
        }
        spaceProperties.put(PROPERTIES_SPACE_CREATED, creationDate);

        // Handle @ symbol (change to +), to allow for email usernames in ACLs
        spaceProperties = replaceInMapValues(spaceProperties, "@", "+");

        // Store properties
        BucketTaggingConfiguration tagConfig = new BucketTaggingConfiguration()
            .withTagSets(new TagSet(spaceProperties));
        s3Client.setBucketTaggingConfiguration(bucketName, tagConfig);
    }

    /*
     * Performs a replaceAll of one string value for another in all the values
     * of a map.
     */
    private Map<String, String> replaceInMapValues(Map<String, String> map,
                                                   String oldVal,
                                                   String newVal) {
        for(String key : map.keySet()) {
            String value = map.get(key);
            if(value.contains(oldVal)) {
                value = StringUtils.replace(value, oldVal, newVal);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * {@inheritDoc}
     */
    public String addContent(String spaceId,
                             String contentId,
                             String contentMimeType,
                             Map<String, String> userProperties,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) {
        log.debug("addContent("+ spaceId +", "+ contentId +", "+
            contentMimeType +", "+ contentSize +", "+ contentChecksum +")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        // Wrap the content in order to be able to retrieve a checksum
        ChecksumInputStream wrappedContent =
            new ChecksumInputStream(content, contentChecksum);

        userProperties = removeCalculatedProperties(userProperties);

        if(contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = DEFAULT_MIMETYPE;
        }

        ObjectMetadata objMetadata = new ObjectMetadata();
        objMetadata.setContentType(contentMimeType);
        if (contentSize > 0) {
            objMetadata.setContentLength(contentSize);
        }
        if(null != contentChecksum && !contentChecksum.isEmpty()) {
            String encodedChecksum =
                ChecksumUtil.convertToBase64Encoding(contentChecksum);
            objMetadata.setContentMD5(encodedChecksum);
        }

        if(userProperties != null) {
            for (String key : userProperties.keySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("[" + key + "|" + userProperties.get(key) + "]");
                }
                objMetadata.addUserMetadata(getSpaceFree(key), userProperties.get(key));
            }
        }

        PutObjectRequest putRequest = new PutObjectRequest(bucketName,
                                                           contentId,
                                                           wrappedContent,
                                                           objMetadata);
        putRequest.setStorageClass(DEFAULT_STORAGE_CLASS);
        putRequest.setCannedAcl(CannedAccessControlList.Private);

        // Add the object
        String etag;
        try {
            PutObjectResult putResult = s3Client.putObject(putRequest);
            etag = putResult.getETag();
        } catch (AmazonClientException e) {
            if (e instanceof AmazonS3Exception) {
                String errorCode = ((AmazonS3Exception) e).getErrorCode();
                if (errorCode.equals("InvalidDigest") || errorCode.equals("BadDigest")) {
                    String err =
                        "Checksum mismatch detected attempting to add " + "content "
                            + contentId
                            + " to S3 bucket "
                            + bucketName
                            + ". Content was not added.";
                    throw new ChecksumMismatchException(err, e, NO_RETRY);
                }
            }

            etag = doesContentExist(bucketName, contentId);
            if(null == etag) {
                String err = "Could not add content " + contentId +
                             " with type " + contentMimeType +
                             " and size " + contentSize +
                             " to S3 bucket " + bucketName + " due to error: " +
                             e.getMessage();
                throw new StorageException(err, e, NO_RETRY);
            }
        }

        // Compare checksum
        String providerChecksum = getETagValue(etag);

        try {
            String checksum = wrappedContent.getMD5();
            StorageProviderUtil.compareChecksum(providerChecksum,
                                                spaceId,
                                                contentId,
                                                checksum);
        } catch(ChecksumMismatchException e) {
            try { // Clean up
                s3Client.deleteObject(bucketName, contentId);
            } catch (AmazonClientException amazonException) {
                log.debug("Content item {} in space {} failed checksum test " +
                          "and call to delete content item failed as well. " +
                          "The content item was most likely never added to " +
                          "storage.", contentId, spaceId);
            }
        }

        return providerChecksum;
    }

    /*
     * Determines if a content item exists. If so, returns its MD5.
     * If not, returns null.
     */
    protected String doesContentExist(String bucketName, String contentId) {
        int maxAttempts = 5;
        for(int i=0; i<maxAttempts; i++) {
            try {
                ObjectMetadata metadata =
                    s3Client.getObjectMetadata(bucketName, contentId);
                if(null != metadata) {
                  return metadata.getETag();
                }
            } catch(AmazonClientException e) {
                wait(2);
            }
        }
        return null;
    }

    protected void wait(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch(InterruptedException e) {
        }
    }

    @Override
    public String copyContent(String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) {
        log.debug("copyContent({}, {}, {}, {})",
                  new Object[]{sourceSpaceId,
                               sourceContentId,
                               destSpaceId,
                               destContentId});

        // Will throw if source bucket does not exist
        String sourceBucketName = getBucketName(sourceSpaceId);
        // Will throw if destination bucket does not exist
        String destBucketName = getBucketName(destSpaceId);

        throwIfContentNotExist(sourceBucketName, sourceContentId);

        CopyObjectRequest request = new CopyObjectRequest(sourceBucketName,
                                                          sourceContentId,
                                                          destBucketName,
                                                          destContentId);
        request.setStorageClass(DEFAULT_STORAGE_CLASS);
        request.setCannedAccessControlList(CannedAccessControlList.Private);

        CopyObjectResult result = doCopyObject(request);
        return StorageProviderUtil.compareChecksum(this,
                                                   sourceSpaceId,
                                                   sourceContentId,
                                                   result.getETag());
    }

    private CopyObjectResult doCopyObject(CopyObjectRequest request) {
        try {
            return s3Client.copyObject(request);
            
        } catch (Exception e) {
            StringBuilder err = new StringBuilder("Error copying from: ");
            err.append(request.getSourceBucketName());
            err.append(" / ");
            err.append(request.getSourceKey());
            err.append(", to: ");
            err.append(request.getDestinationBucketName());
            err.append(" / ");
            err.append(request.getDestinationKey());
            log.error(err.toString() + "msg: {}", e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContent(String spaceId, String contentId) {
        log.debug("getContent(" + spaceId + ", " + contentId + ")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        try {
             S3Object contentItem = s3Client.getObject(bucketName, contentId);
            return contentItem.getObjectContent();
        } catch (AmazonClientException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not retrieve content " + contentId
                    + " in S3 bucket " + bucketName + " due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContent(String spaceId, String contentId) {
        log.debug("deleteContent(" + spaceId + ", " + contentId + ")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);
        // Note that the s3Client does not throw an exception or indicate if
        // the object to be deleted does not exist. This check is being run
        // up front to fulfill the DuraCloud contract for this method.
        throwIfContentNotExist(bucketName, contentId);

        try {
            s3Client.deleteObject(bucketName, contentId);
        } catch (AmazonClientException e) {
            String err = "Could not delete content " + contentId
                    + " from S3 bucket " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setContentProperties(String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties) {
        log.debug("setContentProperties(" + spaceId + ", " + contentId + ")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        contentProperties = removeCalculatedProperties(contentProperties);
 
        // Determine mimetype, from properties list or existing value
        String mimeType = contentProperties.remove(PROPERTIES_CONTENT_MIMETYPE);
        if (mimeType == null || mimeType.equals("")) {
            Map<String, String> existingMeta =
                getContentProperties(spaceId, contentId);
            String existingMime =
                existingMeta.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE);
            if (existingMime != null) {
                mimeType = existingMime;
            }
        }

        // Collect all object properties
        ObjectMetadata objMetadata = new ObjectMetadata();
        for (String key : contentProperties.keySet()) {
            if (log.isDebugEnabled()) {
                log.debug("[" + key + "|" + contentProperties.get(key) + "]");
            }
            objMetadata.addUserMetadata(getSpaceFree(key), contentProperties.get(key));
        }

        // Set Content-Type
        if (mimeType != null && !mimeType.equals("")) {
            objMetadata.setContentType(mimeType);
        }

        updateObjectProperties(bucketName, contentId, objMetadata);
    }

    @Override
    protected Map<String, String> removeCalculatedProperties(Map<String, String> contentProperties) {
        contentProperties = super.removeCalculatedProperties(contentProperties);
        if(contentProperties != null){
            contentProperties.remove(Headers.CONTENT_LENGTH);
            contentProperties.remove(Headers.CONTENT_TYPE);  // Content-Type is set on ObjectMetadata object
            contentProperties.remove(Headers.LAST_MODIFIED);
            contentProperties.remove(Headers.DATE);
            contentProperties.remove(Headers.ETAG);
            contentProperties.remove(Headers.CONTENT_LENGTH.toLowerCase());
            contentProperties.remove(Headers.CONTENT_TYPE.toLowerCase());
            contentProperties.remove(Headers.LAST_MODIFIED.toLowerCase());
            contentProperties.remove(Headers.DATE.toLowerCase());
            contentProperties.remove(Headers.ETAG.toLowerCase());
        }
        
        return contentProperties;
    }

    private void throwIfContentNotExist(String bucketName, String contentId) {
        try {
             s3Client.getObjectMetadata(bucketName, contentId);
        } catch(AmazonClientException e) {
            String err = "Could not find content item with ID " + contentId +
                " in S3 bucket " + bucketName + ". S3 error: " + e.getMessage();
            throw new NotFoundException(err);
        }
    }

    private ObjectMetadata getObjectDetails(String bucketName,
                                            String contentId,
                                            boolean retry) {
        try {
            return s3Client.getObjectMetadata(bucketName, contentId);
        } catch (AmazonClientException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not get details for content " + contentId
                    + " in S3 bucket " + bucketName + " due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, retry);
        }
    }

    private void updateObjectProperties(String bucketName,
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
            s3Client.copyObject(copyRequest);
            s3Client.setObjectAcl(bucketName, contentId, originalACL);
        } catch (AmazonClientException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not update metadata for content "
                    + contentId + " in S3 bucket " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId) {
        log.debug("getContentProperties(" + spaceId + ", " + contentId + ")");

        // Will throw if bucket does not exist
        String bucketName = getBucketName(spaceId);

        // Get the content item from S3
        ObjectMetadata objMetadata =
            getObjectDetails(bucketName, contentId, RETRY);

        if (objMetadata == null) {
            String err = "No metadata is available for item " + contentId
                    + " in S3 bucket " + bucketName;
            throw new StorageException(err, NO_RETRY);
        }

        Map<String, String> contentProperties = new HashMap<>();

        // Set the user properties
        Map<String, String> userProperties = objMetadata.getUserMetadata();
        for(String metaName : userProperties.keySet()) {
            String metaValue = userProperties.get(metaName);
            contentProperties.put(getWithSpace(metaName), metaValue);
        }

        // Set MIMETYPE
        String contentType = objMetadata.getContentType();
        if (contentType != null) {
            contentProperties.put(PROPERTIES_CONTENT_MIMETYPE, contentType);
            contentProperties.put(Headers.CONTENT_TYPE, contentType);
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

    protected String getETagValue(String etag) {
        String checksum = etag;
        if (checksum != null) {
            if (checksum.indexOf("\"") == 0 &&
                checksum.lastIndexOf("\"") == checksum.length() - 1) {
                // Remove wrapping quotes
                checksum = checksum.substring(1, checksum.length() - 1);
            }
        }
        return checksum;
    }

    /**
     * Gets the name of an existing bucket based on a space ID. If no bucket
     * with this spaceId exists, throws a NotFoundException
     *
     * @param spaceId the space Id to convert into an S3 bucket name
     * @return S3 bucket name of a given DuraCloud space
     * @throws NotFoundException if no bucket matches this spaceID
     */
    public String getBucketName(String spaceId) {
        // Determine if there is an existing bucket that matches this space ID.
        // The bucket name may use any access key ID as the prefix, so there is
        // no way to know the exact bucket name up front.
        List<Bucket> buckets = listAllBuckets();
        for(Bucket bucket : buckets){
            String bucketName = bucket.getName();
            spaceId = spaceId.replace(".",  "[.]");
            if(bucketName.matches("[\\w]{20}[.]"+spaceId)) {
                return bucketName;
            }
        }
        throw new  NotFoundException("No S3 bucket found matching spaceID: " +
                                     spaceId);
    }

    /**
     * Converts a bucket name into what could be passed in as a space ID.
     *
     * @param bucketName name of the S3 bucket
     * @return the DuraCloud space name equivalent to a given S3 bucket Id
     */
    protected String getSpaceId(String bucketName) {
        String spaceId = bucketName;
        if (isSpace(bucketName)) {
            spaceId = spaceId.substring(accessKeyId.length() + 1);
        }
        return spaceId;
    }

    /**
     * Determines if an S3 bucket is a DuraCloud space
     *
     * @param bucketName name of the S3 bucket
     * @return true if the given S3 bucket name is named according to the
     *         DuraCloud space naming conventions, false otherwise
     */
    protected boolean isSpace(String bucketName) {
        boolean isSpace = false;
        // According to AWS docs, the access key (used in DuraCloud as a
        // prefix for uniqueness) is a 20 character alphanumeric sequence.
        if (bucketName.matches("[\\w]{20}[.].*")) {
            isSpace = true;
        }
        return isSpace;
    }

    /**
     * Replaces all spaces with "%20"
     *
     * @param name string with possible space
     * @return converted to string without spaces
     */
    protected String getSpaceFree(String name) {
        return name.replaceAll(" ", "%20");
    }

    /**
     * Converts "%20" back to spaces
     *
     * @param name string
     * @return converted to spaces
     */
    protected String getWithSpace(String name) {
        return name.replaceAll("%20", " ");
    }

}
