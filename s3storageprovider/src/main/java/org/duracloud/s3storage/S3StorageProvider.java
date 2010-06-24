/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.loadMetadata;
import static org.duracloud.storage.util.StorageProviderUtil.storeMetadata;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Provides content storage backed by Amazon's Simple Storage Service.
 *
 * @author Bill Branan
 */
public class S3StorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(S3StorageProvider.class);

    private String accessKeyId = null;
    private S3Service s3Service = null;

    public S3StorageProvider(String accessKey, String secretKey) {
        this(S3ProviderUtil.getS3Service(accessKey, secretKey), accessKey);
    }

    public S3StorageProvider(S3Service s3Service, String accessKey) {
        this.accessKeyId = accessKey;
        this.s3Service = s3Service;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpaces()");

        List<String> spaces = new ArrayList<String>();
        S3Bucket[] buckets = listAllBuckets();
        for (S3Bucket bucket : buckets) {
            String bucketName = bucket.getName();
            if (isSpace(bucketName)) {
                spaces.add(getSpaceId(bucketName));
            }
        }

        return spaces.iterator();
    }

    private S3Bucket[] listAllBuckets() {
        try {
            return s3Service.listAllBuckets();
        }
        catch (S3ServiceException e) {
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

        throwIfSpaceNotExist(spaceId);

        String bucketName = getBucketName(spaceId);
        String bucketMetadata = bucketName + SPACE_METADATA_SUFFIX;

        if(maxResults <= 0) {
            maxResults = StorageProvider.DEFAULT_MAX_RESULTS;
        }

        // Queries for maxResults +1 to account for the possibility of needing
        // to remove the space metadata but still maintain a full result
        // set (size == maxResults).
        List<String> spaceContents =
            getCompleteSpaceContents(spaceId, prefix, maxResults + 1, marker);

        if(spaceContents.contains(bucketMetadata)) {
            // Remove space metadata
            spaceContents.remove(bucketMetadata);
        } else if(spaceContents.size() > maxResults) {
            // Remove extra content item
            spaceContents.remove(spaceContents.size()-1);
        }

        return spaceContents;
    }

    private List<String> getCompleteSpaceContents(String spaceId,
                                                  String prefix,
                                                  long maxResults,
                                                  String marker) {
        List<String> contentItems = new ArrayList<String>();

        S3Object[] objects = listObjects(spaceId, prefix, maxResults, marker);
        for (S3Object object : objects) {
            contentItems.add(object.getKey());
        }
        return contentItems;
    }

    private S3Object[] listObjects(String spaceId,
                                   String prefix,
                                   long maxResults,
                                   String marker) {
        String bucketName = getBucketName(spaceId);

        try {
            return s3Service.listObjectsChunked(bucketName,
                                                prefix,
                                                null,
                                                maxResults,
                                                marker).getObjects();
        } catch (S3ServiceException e) {
            String err = "Could not get contents of S3 bucket " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    private void throwIfSpaceExists(String spaceId) {
        if (spaceExists(spaceId)) {
            String msg = "Error: Space already exists: " + spaceId;
            throw new StorageException(msg, NO_RETRY);
        }
    }

    protected void throwIfSpaceNotExist(String spaceId) {
        throwIfSpaceNotExist(spaceId, true);
    }

    private void throwIfSpaceNotExist(String spaceId, boolean wait) {
        if (!spaceExists(spaceId)) {
            String msg = "Error: Space does not exist: " + spaceId;
            if(wait) {
                waitForSpaceAvailable(spaceId);
                if (!spaceExists(spaceId)) {
                    throw new NotFoundException(msg);
                }
            } else {
                throw new NotFoundException(msg);
            }
        }
    }

    private boolean spaceExists(String spaceId) {
        String bucketName = getBucketName(spaceId);
        boolean exists = false;
        try {
            exists = s3Service.isBucketAccessible(bucketName);
        } catch (S3ServiceException e) {
            exists = false;
        }
        return exists;
    }

    private void waitForSpaceAvailable(String spaceId) {
        int maxLoops = 6;
        for (int loops = 0;
             !spaceExists(spaceId) && loops < maxLoops;
             loops++) {
            try {
                log.debug("Waiting for space " + spaceId +
                          " to be available, loop " + loops);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createSpace(String spaceId) {
        log.debug("createSpace(" + spaceId + ")");
        throwIfSpaceExists(spaceId);

        S3Bucket bucket = createBucket(spaceId);

        // Add space metadata
        Map<String, String> spaceMetadata = new HashMap<String, String>();
        Date created = bucket.getCreationDate();
        spaceMetadata.put(METADATA_SPACE_CREATED, formattedDate(created));
        spaceMetadata.put(METADATA_SPACE_ACCESS, AccessType.CLOSED.name());

        try {
            setNewSpaceMetadata(spaceId, spaceMetadata);
        } catch(StorageException e) {
            deleteBucket(spaceId);
            String err = "Unable to create space due to: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    private void setNewSpaceMetadata(String spaceId,
                                     Map<String, String> spaceMetadata) {
        boolean success = false;
        int maxLoops = 6;
        for (int loops = 0; !success && loops < maxLoops; loops++) {
            try {
                setSpaceMetadata(spaceId, spaceMetadata);
                success = true;
            } catch (NotFoundException e) {
                success = false;
            }
        }

        if(!success) {
            throw new StorageException("Metadata for space " +
                                       spaceId + " could not be created. " +
                                       "The space cannot be found.");
        }
    }

    private S3Bucket createBucket(String spaceId) {
        String bucketName = getBucketName(spaceId);
        try {
            AccessControlList bucketAcl = AccessControlList.REST_CANNED_PRIVATE;
            S3Bucket bucket = new S3Bucket(bucketName);
            bucket.setAcl(bucketAcl);
            return s3Service.createBucket(bucket);
        } catch (S3ServiceException e) {
            String err = "Could not create S3 bucket with name " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

     private String formattedDate(Date created) {
        RFC822_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return RFC822_DATE_FORMAT.format(created);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(String spaceId) {
        log.debug("deleteSpace(" + spaceId + ")");
        throwIfSpaceNotExist(spaceId);

        Iterator<String> contents = getSpaceContents(spaceId, null);
        while(contents.hasNext()) {
            deleteContent(spaceId, contents.next());
        }

        String bucketMetadata = getBucketName(spaceId) + SPACE_METADATA_SUFFIX;
        try {
            deleteContent(spaceId, bucketMetadata);
        } catch(NotFoundException e) {
            // Metadata has already been removed. Continue deleting space.
        }

        deleteBucket(spaceId);
    }

    private void deleteBucket(String spaceId) {
        String bucketName = getBucketName(spaceId);
        try {
            s3Service.deleteBucket(bucketName);
        } catch (S3ServiceException e) {
            String err = "Could not delete S3 bucket with name " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getSpaceMetadata(String spaceId) {
        log.debug("getSpaceMetadata(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        // Space metadata is stored as a content item
        String bucketName = getBucketName(spaceId);
        InputStream is = getContent(spaceId, bucketName + SPACE_METADATA_SUFFIX);
        Map<String, String> spaceMetadata = loadMetadata(is);

        Date created = getCreationDate(bucketName, spaceMetadata);
        if (created != null) {
            spaceMetadata.put(METADATA_SPACE_CREATED, formattedDate(created));
        }

        spaceMetadata.put(METADATA_SPACE_COUNT, getSpaceCount(spaceId));
        return spaceMetadata;
    }

    /*
     * Counts the number of items in a space.
     *
     * Note that anecdotal evidence shows that this method of counting
     * (using size of chunked calls) is faster in most cases than enumerating
     * the Iteration: StorageProviderUtil.count(getSpaceContents(spaceId, null))
     */
    private String getSpaceCount(String spaceId) {
        List<String> spaceContentChunk = null;
        long count = 0;
        do {
            String marker = null;
            if (spaceContentChunk != null && spaceContentChunk.size() > 0) {
                marker = spaceContentChunk.get(spaceContentChunk.size() - 1);
            }
            spaceContentChunk = getSpaceContentsChunked(spaceId,
                                                        null,
                                                        DEFAULT_MAX_RESULTS,
                                                        marker);
            count += spaceContentChunk.size();
        } while (spaceContentChunk.size() > 0);
        return String.valueOf(count);
    }

    private Date getCreationDate(String bucketName,
                                 Map<String, String> spaceMetadata) {
        Date created = null;
        if (!spaceMetadata.containsKey(METADATA_SPACE_CREATED)) {
            S3Bucket bucket = getBucket(bucketName);
            created = bucket.getCreationDate();
        } else {
            String dateText = spaceMetadata.get(METADATA_SPACE_CREATED);
            try {
                created = RFC822_DATE_FORMAT.parse(dateText);
            } catch (ParseException e) {
                log.warn("Unable to parse date: '" + dateText + "'");
            }
        }
        return created;
    }

    private S3Bucket getBucket(String bucketName) {
        try {
            return s3Service.getBucket(bucketName);
        } catch (S3ServiceException e) {
            String err = "Could not retrieve metadata from S3 bucket "
                    + bucketName + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSpaceMetadata(String spaceId,
                                 Map<String, String> spaceMetadata) {
        log.debug("setSpaceMetadata(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        // Ensure that space access is included in the new metadata
        if(!spaceMetadata.containsKey(METADATA_SPACE_ACCESS)) {
            String spaceAccess = getSpaceAccess(spaceId).name();
            spaceMetadata.put(METADATA_SPACE_ACCESS, spaceAccess);
        }

        String bucketName = getBucketName(spaceId);
        ByteArrayInputStream is = storeMetadata(spaceMetadata);
        addContent(spaceId, bucketName + SPACE_METADATA_SUFFIX, "text/xml",
                   is.available(), null, is);
    }




    /**
     * {@inheritDoc}
     */
    public String addContent(String spaceId,
                             String contentId,
                             String contentMimeType,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) {
        log.debug("addContent("+ spaceId +", "+ contentId +", "+
            contentMimeType +", "+ contentSize +", "+ contentChecksum +")");

        throwIfSpaceNotExist(spaceId);

        // Wrap the content in order to be able to retrieve a checksum
        ChecksumInputStream wrappedContent =
            new ChecksumInputStream(content, contentChecksum);

        if(contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = DEFAULT_MIMETYPE;
        }

        S3Object contentItem = new S3Object(contentId);
        contentItem.setContentType(contentMimeType);
        contentItem.setDataInputStream(wrappedContent);

        if (contentSize > 0) {
            contentItem.setContentLength(contentSize);
        }

        // Set private access control, content must be accessed via DuraCloud
        AccessControlList privateAcl = AccessControlList.REST_CANNED_PRIVATE;
        contentItem.setAcl(privateAcl);

        // Add the object
        putObject(contentItem, spaceId);

        // Compare checksum
        String finalChecksum =
            compareChecksum(this, spaceId, contentId, wrappedContent.getMD5());

        // Set default content metadata values
        Map<String, String> contentMetadata = new HashMap<String, String>();
        contentMetadata.put(METADATA_CONTENT_MIMETYPE, contentMimeType);
        setContentMetadata(spaceId, contentId, contentMetadata);

        return finalChecksum;
    }

    private void putObject(S3Object contentItem,
                           String spaceId) {
        String bucketName = getBucketName(spaceId);
        try {
            s3Service.putObject(bucketName, contentItem);
        } catch (S3ServiceException e) {
            String err = "Could not add content " + contentItem.getKey()
                    + " with type " + contentItem.getContentType()
                    + " and size " + contentItem.getContentLength()
                    + " to S3 bucket " + bucketName + " due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContent(String spaceId, String contentId) {
        log.debug("getContent(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        String bucketName = getBucketName(spaceId);        
        S3Object contentItem = getObject(contentId, bucketName);

        return getDataInputStream(contentItem);
    }

    private S3Object getObject(String contentId,
                               String bucketName) {
        try {
            return s3Service.getObject(new S3Bucket(bucketName), contentId);
        } catch (S3ServiceException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not retrieve content " + contentId
                    + " in S3 bucket " + bucketName + " due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    private InputStream getDataInputStream(S3Object contentItem) {
        try {
            return contentItem.getDataInputStream();
        } catch (S3ServiceException e) {
            String err = "Could not retrieve content " + contentItem.getKey()
                    + " in S3 bucket " + contentItem.getBucketName()
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContent(String spaceId, String contentId) {
        log.debug("deleteContent(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        String bucketName = getBucketName(spaceId);       

        try {
            // See if the content exists to be deleted
            S3Bucket bucket = new S3Bucket();
            bucket.setName(bucketName);
            s3Service.getObjectDetails(bucket, contentId);

            // Delete content
            s3Service.deleteObject(bucketName, contentId);
        } catch (S3ServiceException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not delete content " + contentId
                    + " from S3 bucket " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setContentMetadata(String spaceId,
                                   String contentId,
                                   Map<String, String> contentMetadata) {
        log.debug("setContentMetadata(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        // Remove calculated properties
        contentMetadata.remove(METADATA_CONTENT_MD5);
        contentMetadata.remove(METADATA_CONTENT_CHECKSUM);
        contentMetadata.remove(METADATA_CONTENT_MODIFIED);
        contentMetadata.remove(METADATA_CONTENT_SIZE);
        contentMetadata.remove(S3Object.METADATA_HEADER_CONTENT_LENGTH);
        contentMetadata.remove(S3Object.METADATA_HEADER_LAST_MODIFIED_DATE);
        contentMetadata.remove(S3Object.METADATA_HEADER_DATE);
        contentMetadata.remove(S3Object.METADATA_HEADER_ETAG);

        // Determine mimetype, from metadata list or existing value
        String mimeType = contentMetadata.remove(METADATA_CONTENT_MIMETYPE);
        if (mimeType == null || mimeType.equals("")) {
            Map<String, String> existingMeta =
                getContentMetadata(spaceId, contentId);
            String existingMime =
                existingMeta.get(StorageProvider.METADATA_CONTENT_MIMETYPE);
            if (existingMime != null) {
                mimeType = existingMime;
            }
        }

        if (log.isDebugEnabled()) {
            for (String key : contentMetadata.keySet()) {
                log.debug("[" + key + "|" + contentMetadata.get(key) + "]");
            }
        }

        // Get the object and replace its metadata
        String bucketName = getBucketName(spaceId);
        S3Bucket bucket = new S3Bucket(bucketName);
        S3Object contentItem = getObjectDetails(bucket, contentId, NO_RETRY);
        contentItem.setAcl(getObjectAcl(contentId, bucket));
        contentItem.replaceAllMetadata(contentMetadata);

        // Set Content-Type
        if (mimeType != null && !mimeType.equals("")) {
            contentItem.addMetadata(S3Object.METADATA_HEADER_CONTENT_TYPE,
                                    mimeType);
        }

        updateObjectMetadata(bucketName, contentId, contentItem);
    }

    private void throwIfContentNotExist(String bucketName, String contentId) {
        try {
             s3Service.getObjectDetails(new S3Bucket(bucketName), contentId);
        } catch(S3ServiceException e) {
            String err = "Could not find content item with ID " + contentId +
                " in S3 bucket " + bucketName + ". S3 error: " + e.getMessage();
            throw new NotFoundException(err);
        }
    }

    private S3Object getObjectDetails(S3Bucket bucket, String contentId,
                                      boolean retry) {
        try {
            return s3Service.getObjectDetails(bucket, contentId);
        } catch (S3ServiceException e) {
            throwIfContentNotExist(bucket.getName(), contentId);
            String err = "Could not get details for content " + contentId
                    + " in S3 bucket " + bucket.getName() + " due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, retry);
        }
    }

    private AccessControlList getObjectAcl(String contentId,
                                           S3Bucket bucket) {
        try {
            return s3Service.getObjectAcl(bucket, contentId);
        } catch (S3ServiceException e) {
            String err = "Could not get ACL for content " + contentId
                    + " in S3 bucket " + bucket.getName() + " due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, NO_RETRY);
        }
    }

    private void updateObjectMetadata(String bucketName,
                                      String contentId,
                                      S3Object contentItem) {
        try {
            s3Service.updateObjectMetadata(bucketName, contentItem);
        } catch (S3ServiceException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not update metadata for content "
                    + contentItem.getKey() + " in S3 bucket " + bucketName
                    + " due to error: " + e.getMessage();
            throw new StorageException(err, e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId) {
        log.debug("getContentMetadata(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        // Get the content item from S3
        String bucketName = getBucketName(spaceId);
        S3Object contentItem = getObjectDetails(new S3Bucket(bucketName),
                                                contentId, RETRY);

        if (contentItem == null) {
            String err = "No metadata is available for item " + contentId
                    + " in S3 bucket " + bucketName;
            throw new StorageException(err, NO_RETRY);
        }

        // Load the metadata Map
        Map<String, String> contentMetadata = new HashMap<String, String>();
        Map contentItemMetadata = contentItem.getMetadataMap();
        Iterator metaIterator = contentItemMetadata.keySet().iterator();
        while (metaIterator.hasNext()) {
            String metaName = metaIterator.next().toString();
            Object metaValueObj = contentItemMetadata.get(metaName);
            String metaValue;
            if (metaValueObj instanceof Date) {
                metaValue = formattedDate((Date) metaValueObj);
            } else {
                metaValue = metaValueObj.toString();
            }
            contentMetadata.put(metaName, metaValue);
        }

        // Set MIMETYPE
        String contentType =
                contentMetadata.get(S3Object.METADATA_HEADER_CONTENT_TYPE);
        if (contentType != null) {
            contentMetadata.put(METADATA_CONTENT_MIMETYPE, contentType);
        }

        // Set SIZE
        String contentLength =
                contentMetadata.get(S3Object.METADATA_HEADER_CONTENT_LENGTH);
        if (contentLength != null) {
            contentMetadata.put(METADATA_CONTENT_SIZE, contentLength);
        }

        // Set CHECKSUM
        String checksum = contentMetadata.get(S3Object.METADATA_HEADER_ETAG);
        if (checksum != null) {
            if (checksum.indexOf("\"") == 0
                    && checksum.lastIndexOf("\"") == checksum.length() - 1) {
                // Remove wrapping quotes
                checksum = checksum.substring(1, checksum.length() - 1);
            }
            contentMetadata.put(METADATA_CONTENT_CHECKSUM, checksum);
        }

        // Set MODIFIED
        String modified =
                contentMetadata.get(S3Object.METADATA_HEADER_LAST_MODIFIED_DATE);
        if (modified != null) {
            contentMetadata.put(METADATA_CONTENT_MODIFIED, modified);
        }

        return contentMetadata;
    }

    /**
     * Converts a DuraCloud spaceId into its corresponding Amazon S3 bucket name
     */
    public String getBucketName(String spaceId) {
        return S3ProviderUtil.getBucketName(accessKeyId, spaceId);
    }

    /**
     * Converts a bucket name into what could be passed in as a space ID.
     *
     * @param bucketName
     * @return
     */
    protected String getSpaceId(String bucketName) {
        String spaceId = bucketName;
        if (isSpace(bucketName)) {
            spaceId = spaceId.substring(accessKeyId.length() + 1);
        }
        return spaceId;
    }

    /**
     * Determines if an S3 bucket is a DuraSpace space
     *
     * @param bucketName
     * @return
     */
    protected boolean isSpace(String bucketName) {
        boolean isSpace = false;
        if (bucketName.startsWith(accessKeyId.toLowerCase())) {
            isSpace = true;
        }
        return isSpace;
    }

}
