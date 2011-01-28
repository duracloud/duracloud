/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.duracloud.storage.util.StorageProviderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;
import static org.duracloud.storage.util.StorageProviderUtil.loadMetadata;
import static org.duracloud.storage.util.StorageProviderUtil.storeMetadata;

/**
 * Provides content storage backed by Amazon's Simple Storage Service.
 *
 * @author Bill Branan
 */
public class S3StorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(S3StorageProvider.class);

    protected static final int MAX_ITEM_COUNT = 1000;

    private String accessKeyId = null;
    private AmazonS3Client s3Client = null;

    public S3StorageProvider(String accessKey, String secretKey) {
        this(S3ProviderUtil.getAmazonS3Client(accessKey, secretKey), accessKey);
    }

    public S3StorageProvider(AmazonS3Client s3Client, String accessKey) {
        this.accessKeyId = accessKey;
        this.s3Client = s3Client;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpaces()");

        List<String> spaces = new ArrayList<String>();
        List<Bucket> buckets = listAllBuckets();
        for (Bucket bucket : buckets) {
            String bucketName = bucket.getName();
            if (isSpace(bucketName)) {
                spaces.add(getSpaceId(bucketName));
            }
        }

        return spaces.iterator();
    }

    private List<Bucket> listAllBuckets() {
        try {
            return s3Client.listBuckets();
        }
        catch (AmazonServiceException e) {
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

        List<S3ObjectSummary> objects =
            listObjects(spaceId, prefix, maxResults, marker);
        for (S3ObjectSummary object : objects) {
            contentItems.add(object.getKey());
        }
        return contentItems;
    }

    private List<S3ObjectSummary> listObjects(String spaceId,
                                              String prefix,
                                              long maxResults,
                                              String marker) {
        String bucketName = getBucketName(spaceId);

        int numResults = new Long(maxResults).intValue();
        ListObjectsRequest request =
            new ListObjectsRequest(bucketName, prefix, marker, null, numResults);        
        try {
            ObjectListing objectListing = s3Client.listObjects(request);
            return objectListing.getObjectSummaries();
        } catch (AmazonServiceException e) {
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
        return s3Client.doesBucketExist(bucketName);
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

        Bucket bucket = createBucket(spaceId);

        Date created = bucket.getCreationDate();
        if(created == null) {
            created = new Date();
        }

        // Add space metadata
        Map<String, String> spaceMetadata = new HashMap<String, String>();
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

    private Bucket createBucket(String spaceId) {
        String bucketName = getBucketName(spaceId);
        try {
            return s3Client.createBucket(bucketName);
        } catch (AmazonServiceException e) {
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
            s3Client.deleteBucket(bucketName);
        } catch (AmazonServiceException e) {
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

        spaceMetadata.put(METADATA_SPACE_COUNT,
                          getSpaceCount(spaceId, MAX_ITEM_COUNT));
        return spaceMetadata;
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
                                                        DEFAULT_MAX_RESULTS,
                                                        marker);
            count += spaceContentChunk.size();
        } while (spaceContentChunk.size() > 0 && count < maxCount);

        String suffix = "";
        if(count >= maxCount) {
            suffix = "+";
        }
        return String.valueOf(count) + suffix;
    }    

    private String getSpaceCreationDate(String spaceId) {
        String bucketName = getBucketName(spaceId);
        Date created = null;
        try {
            List<Bucket> buckets = s3Client.listBuckets();
            for(Bucket bucket : buckets) {
                if(bucket.getName().equals(bucketName)) {
                    created = bucket.getCreationDate();
                }
            }
        } catch (AmazonServiceException e) {
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
    public void setSpaceMetadata(String spaceId,
                                 Map<String, String> spaceMetadata) {
        log.debug("setSpaceMetadata(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        Map<String, String> originalMetadata = null;
        try {
            originalMetadata = getSpaceMetadata(spaceId);
        } catch(NotFoundException e) {
            // Likely adding a new space, so no existing metadata yet.
            originalMetadata = new HashMap<String, String>();
        }

        // Set creation date
        String creationDate = originalMetadata.get(METADATA_SPACE_CREATED);
        if(creationDate == null) {
            creationDate = spaceMetadata.get(METADATA_SPACE_CREATED);
            if(creationDate == null) {
                creationDate = getSpaceCreationDate(spaceId);
            }
        }
        spaceMetadata.put(METADATA_SPACE_CREATED, creationDate);

        // Ensure that space access is included in the new metadata
        if(!spaceMetadata.containsKey(METADATA_SPACE_ACCESS)) {
            String spaceAccess = originalMetadata.get(METADATA_SPACE_ACCESS);
            if(spaceAccess == null) {
                spaceAccess = AccessType.CLOSED.name();
            }
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

        ObjectMetadata objMetadata = new ObjectMetadata();
        objMetadata.setContentType(contentMimeType);
        if (contentSize > 0) {
            objMetadata.setContentLength(contentSize);
        }

        String bucketName = getBucketName(spaceId);
        PutObjectRequest putRequest = new PutObjectRequest(bucketName,
                                                           contentId,
                                                           wrappedContent,
                                                           objMetadata);
        putRequest.setStorageClass(StorageClass.ReducedRedundancy);
        putRequest.setCannedAcl(CannedAccessControlList.Private);

        // Add the object
        PutObjectResult putResult;
        try {
            putResult = s3Client.putObject(putRequest);
        } catch (AmazonServiceException e) {
            String err = "Could not add content " + contentId +
                         " with type " + contentMimeType +
                         " and size " + contentSize +
                         " to S3 bucket " + bucketName + " due to error: " +
                         e.getMessage();
            throw new StorageException(err, e, NO_RETRY);
        }

        // Compare checksum
        String providerChecksum = getETagValue(putResult.getETag());
        String checksum = wrappedContent.getMD5();
        return StorageProviderUtil.compareChecksum(providerChecksum,
                                                   spaceId,
                                                   contentId,
                                                   checksum);
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContent(String spaceId, String contentId) {
        log.debug("getContent(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);
        String bucketName = getBucketName(spaceId);

        try {
             S3Object contentItem = s3Client.getObject(bucketName, contentId);
            return contentItem.getObjectContent();
        } catch (AmazonServiceException e) {
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

        String bucketName = getBucketName(spaceId);
        try {
          // Note that the s3Client does not throw an exception or indicate if
          // the object to be deleted does not exist. This check is being run
          // up front to fulfill the DuraCloud contract for this method.
          throwIfContentNotExist(bucketName, contentId);
        } catch (NotFoundException e) {
          throwIfSpaceNotExist(spaceId);
          throw e;
        }

        try {
            s3Client.deleteObject(bucketName, contentId);
        } catch (AmazonServiceException e) {
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
        contentMetadata.remove(Headers.CONTENT_LENGTH);
        contentMetadata.remove(Headers.LAST_MODIFIED);
        contentMetadata.remove(Headers.DATE);
        contentMetadata.remove(Headers.ETAG);
        contentMetadata.remove(Headers.CONTENT_LENGTH.toLowerCase());
        contentMetadata.remove(Headers.LAST_MODIFIED.toLowerCase());
        contentMetadata.remove(Headers.DATE.toLowerCase());
        contentMetadata.remove(Headers.ETAG.toLowerCase());

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

        // Collect all object metadata
        String bucketName = getBucketName(spaceId);
        ObjectMetadata objMetadata = new ObjectMetadata();
        for (String key : contentMetadata.keySet()) {
            if (log.isDebugEnabled()) {
                log.debug("[" + key + "|" + contentMetadata.get(key) + "]");
            }
            objMetadata.addUserMetadata(getSpaceFree(key), contentMetadata.get(key));
        }

        // Set Content-Type
        if (mimeType != null && !mimeType.equals("")) {
            objMetadata.setContentType(mimeType);
        }

        updateObjectMetadata(bucketName, contentId, objMetadata);
    }

    private void throwIfContentNotExist(String bucketName, String contentId) {
        try {
             s3Client.getObjectMetadata(bucketName, contentId);
        } catch(AmazonServiceException e) {
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
        } catch (AmazonServiceException e) {
            throwIfContentNotExist(bucketName, contentId);
            String err = "Could not get details for content " + contentId
                    + " in S3 bucket " + bucketName + " due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, retry);
        }
    }

    private void updateObjectMetadata(String bucketName,
                                      String contentId,
                                      ObjectMetadata objMetadata) {
        try {
            CopyObjectRequest copyRequest = new CopyObjectRequest(bucketName,
                                                                  contentId,
                                                                  bucketName,
                                                                  contentId);
            copyRequest.setCannedAccessControlList(
                CannedAccessControlList.Private);
            copyRequest.setStorageClass(StorageClass.ReducedRedundancy);
            copyRequest.setNewObjectMetadata(objMetadata);
            s3Client.copyObject(copyRequest);
        } catch (AmazonServiceException e) {
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
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId) {
        log.debug("getContentMetadata(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        // Get the content item from S3
        String bucketName = getBucketName(spaceId);
        ObjectMetadata objMetadata =
            getObjectDetails(bucketName, contentId, RETRY);

        if (objMetadata == null) {
            String err = "No metadata is available for item " + contentId
                    + " in S3 bucket " + bucketName;
            throw new StorageException(err, NO_RETRY);
        }

        Map<String, String> contentMetadata = new HashMap<String, String>();

        // Set the user metadata
        Map<String, String> userMetadata = objMetadata.getUserMetadata();
        for(String metaName : userMetadata.keySet()) {
            String metaValue = userMetadata.get(metaName);
            contentMetadata.put(getWithSpace(metaName), metaValue);
        }

        // Set MIMETYPE
        String contentType = objMetadata.getContentType();
        if (contentType != null) {
            contentMetadata.put(METADATA_CONTENT_MIMETYPE, contentType);
            contentMetadata.put(Headers.CONTENT_TYPE, contentType);
        }

        // Set SIZE
        long contentLength = objMetadata.getContentLength();
        if (contentLength > 0) {
            String size = String.valueOf(contentLength);
            contentMetadata.put(METADATA_CONTENT_SIZE, size);
            contentMetadata.put(Headers.CONTENT_LENGTH, size);
        }

        // Set CHECKSUM
        String checksum = objMetadata.getETag();
        if (checksum != null) {
            String eTagValue = getETagValue(checksum);
            contentMetadata.put(METADATA_CONTENT_CHECKSUM, eTagValue);
            contentMetadata.put(METADATA_CONTENT_MD5, eTagValue);
            contentMetadata.put(Headers.ETAG, eTagValue);
        }

        // Set MODIFIED
        Date modified = objMetadata.getLastModified();
        if (modified != null) {
            String modDate = formattedDate(modified);
            contentMetadata.put(METADATA_CONTENT_MODIFIED, modDate);
            contentMetadata.put(Headers.LAST_MODIFIED, modDate);
        }

        return contentMetadata;
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
     * Converts a DuraCloud spaceId into its corresponding Amazon S3 bucket name
     *
     * @param spaceId the space Id to convert into an S3 bucket name
     * @return S3 bucket name of a given DuraCloud space
     */
    public String getBucketName(String spaceId) {
        return S3ProviderUtil.getBucketName(accessKeyId, spaceId);
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
        if (bucketName.startsWith(accessKeyId.toLowerCase())) {
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
