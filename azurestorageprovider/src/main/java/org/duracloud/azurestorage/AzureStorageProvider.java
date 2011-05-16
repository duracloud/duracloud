/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.azurestorage;

import org.apache.commons.codec.binary.Base64;
import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soyatec.windowsazure.blob.*;
import org.soyatec.windowsazure.blob.internal.*;
import org.soyatec.windowsazure.blob.io.BlobMemoryStream;
import org.soyatec.windowsazure.error.StorageServerException;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.TimeSpan;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;

/**
 * Provides content storage backed by Azure's Cloud Files service.
 *
 * @author Kristen Cannava
 */
public class AzureStorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(AzureStorageProvider.class);

    public static final String BLOB_NAMESPACE = "http://blob.core.windows.net/";

    private BlobStorageClient blobStorage = null;

    public AzureStorageProvider(String username, String apiAccessKey) {


        try {
            blobStorage = BlobStorageClient.create(URI.create(BLOB_NAMESPACE),
                                                   false,
                                                   username,
                                                   apiAccessKey);

            /*
            * Set retry policy for a time interval of 5 seconds.
            */
            blobStorage.setRetryPolicy(RetryPolicies.retryExponentialN(4,
                                                                       TimeSpan.fromSeconds(30)));
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            String err =
                "Could not connect to Azure due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        } catch (Exception e) {
            String err =
                "Could not connect to Azure due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    public AzureStorageProvider(BlobStorageClient blobStorage) {
        this.blobStorage = blobStorage;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpace()");

        List<IBlobContainer> containers = listContainers();
        List<String> spaces = new ArrayList<String>();
        for (IBlobContainer container : containers) {
            String containerName = container.getName();
            spaces.add(containerName);
        }
        return spaces.iterator();
    }

    private List<IBlobContainer> listContainers() {
        StringBuilder err = new StringBuilder(
            "Could not retrieve list of " + "Azure containers due to error: ");
        try {
            return blobStorage.listBlobContainers();
        } catch (StorageServerException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaceContents(String spaceId, String prefix) {
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

        if (maxResults <= 0) {
            maxResults = StorageProvider.DEFAULT_MAX_RESULTS;
        }

        return getCompleteSpaceContents(spaceId, prefix, maxResults, marker);
    }

    private List<String> getCompleteSpaceContents(String spaceId,
                                                  String prefix,
                                                  long maxResults,
                                                  String marker) {
        String containerName = getContainerName(spaceId);
        Iterator<IBlobProperties> objects;
        List<String> contentItems = new ArrayList<String>();

        int counter = 0;

        boolean found = false;
        if (marker == null) {
            found = true;
        }

        do {

            objects = listObjects(containerName, prefix, maxResults);
            
            if(!objects.hasNext())
                break;

            while (objects.hasNext()) {
                IBlobProperties object = objects.next();
                if (maxResults == counter) {
                    break;
                }
                if (found == false && object.getName().equals(marker)) {
                    found = true;
                    continue;
                }
                if (found == true) {
                    contentItems.add(getContentId(object.getName()));
                    counter++;
                }
            }
        } while (found == false);


        return contentItems;
    }

    private Iterator<IBlobProperties> listObjects(String containerName,
                                                  String prefix,
                                                  long maxResults) {
        StringBuilder err = new StringBuilder(
            "Could not get contents of " + "Azure container " + containerName +
                " due to error: ");
        try {
            int limit = new Long(maxResults).intValue();
            if (prefix == null) {
                prefix = "";
            }

            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);

            // listBlobs does not limit the results
            // maxResults - Specifies the maximum number of blobs to return per call to Azure storage.
            // This does NOT affect list size returned by this function.
            return blobContainer.listBlobs(prefix, false, limit);
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        }
    }

    private void throwIfSpaceExists(String spaceId) {
        if (spaceExists(spaceId)) {
            String msg = "Error: Space already exists: " + spaceId;
            throw new StorageException(msg, NO_RETRY);
        }
    }

    protected void throwIfSpaceNotExist(String spaceId) {
        if (!spaceExists(spaceId)) {
            String msg = "Error: Space does not exist: " + spaceId;
            throw new NotFoundException(msg);
        }
    }

    private void throwIfContentNotExist(String spaceId, String contentId) {
        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);

        IBlobContainer blobContainer = blobStorage.getBlobContainer(
            containerName);
        if (!blobContainer.isBlobExist(contentName)) {
            String msg = "Error: Content does not exist: " + contentName;
            throw new NotFoundException(msg);

        }
    }

    private boolean spaceExists(String spaceId) {
        String containerName = getContainerName(spaceId);
        try {
            return blobStorage.isContainerExist(containerName);
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createSpace(String spaceId) {
        log.debug("getCreateSpace(" + spaceId + ")");
        throwIfSpaceExists(spaceId);

        createContainer(spaceId);

        // Add space metadata
        Map<String, String> spaceMetadata = new HashMap<String, String>();
        Date created = new Date(System.currentTimeMillis());
        spaceMetadata.put(METADATA_SPACE_CREATED, formattedDate(created));
        setSpaceMetadataInt(spaceId, spaceMetadata);
    }

    private String formattedDate(Date created) {
        RFC822_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return RFC822_DATE_FORMAT.format(created);
    }

    private void createContainer(String spaceId) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder(
            "Could not create Azure container with name " + containerName +
                " due to error: ");
        try {
            blobStorage.createContainer(containerName);
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeSpace(String spaceId) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder(
            "Could not delete Azure container with name " + containerName +
                " due to error: ");

        try {
            blobStorage.deleteContainer(containerName);
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getSpaceMetadata(String spaceId) {
        log.debug("getSpaceMetadata(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        String containerName = getContainerName(spaceId);

        IBlobContainer blobContainer = blobStorage.getBlobContainer(
            containerName);

        IContainerProperties containerInfo = getContainerInfo(containerName);
        NameValueCollection values = containerInfo.getMetadata();

        Map<String, String> spaceMetadata = new HashMap<String, String>();

        Iterator<Object> keys = values.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String val = values.getSingleValue(key);
            spaceMetadata.put(key, val);
        }

        int count = 0;
        Iterator<IBlobProperties> blobs = blobContainer.listBlobs("", false);

        while (blobs != null && blobs.hasNext()) {
            count++;
            blobs.next();
        }
        spaceMetadata.put(METADATA_SPACE_COUNT, String.valueOf(count));

        ContainerAccessControl enumAccess = blobContainer.getAccessControl();
        if (enumAccess != null) {
            if (enumAccess.isPublic()) {
                spaceMetadata.put(METADATA_SPACE_ACCESS,
                                  AccessType.OPEN.name());
            } else {
                spaceMetadata.put(METADATA_SPACE_ACCESS,
                                  AccessType.CLOSED.name());
            }
        }

        return spaceMetadata;
    }

    private IContainerProperties getContainerInfo(String containerName) {
        StringBuilder err = new StringBuilder(
            "Could not retrieve metadata " + "from Azure container " +
                containerName + " due to error: ");

        try {
            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);
            return blobContainer.getProperties();
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSpaceMetadata(String spaceId,
                                 Map<String, String> spaceMetadata) {
        log.debug("setSpaceMetadata(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        setSpaceMetadataInt(spaceId, spaceMetadata);
    }

    private void setSpaceMetadataInt(String spaceId,
                                 Map<String, String> spaceMetadata) {
        log.debug("setSpaceMetadata(" + spaceId + ")");

        // Ensure that space created date is included in the new metadata
        Date created = getCreationDate(spaceId, spaceMetadata);
        if (created != null) {
            spaceMetadata.put(METADATA_SPACE_CREATED, formattedDate(created));
        }

        String containerName = getContainerName(spaceId);

        IBlobContainer blobContainer = blobStorage.getBlobContainer(
            containerName);

        String spaceAccess = spaceMetadata.remove(METADATA_SPACE_ACCESS);
        if (spaceAccess != null) {
            ContainerAccessControl enumAccess = ContainerAccessControl.Private;
            if (spaceAccess.equalsIgnoreCase(AccessType.OPEN.name())) {
                enumAccess = ContainerAccessControl.Public;
            }

            blobContainer.setAccessControl(enumAccess);
        }

        NameValueCollection objMetadataPut = new NameValueCollection();
        objMetadataPut.putAll(spaceMetadata);
        blobContainer.setMetadata(objMetadataPut);
    }

    private Date getCreationDate(String spaceId,
                                 Map<String, String> spaceMetadata) {
        String dateText;
        if (!spaceMetadata.containsKey(METADATA_SPACE_CREATED)) {
            dateText = getCreationTimestamp(spaceId);
        } else {
            dateText = spaceMetadata.get(METADATA_SPACE_CREATED);
        }

        Date created = null;
        try {
            created = RFC822_DATE_FORMAT.parse(dateText);
        } catch (ParseException e) {
            log.warn("Unable to parse date: '" + dateText + "'");
        }
        return created;
    }

    private String getCreationTimestamp(String spaceId) {
        Map<String, String> spaceMd = getSpaceMetadata(spaceId);
        String creationTime = spaceMd.get(METADATA_SPACE_CREATED);

        if (creationTime == null) {
            StringBuffer msg = new StringBuffer("Error: ");
            msg.append("No ").append(METADATA_SPACE_CREATED).append(" found ");
            msg.append("for spaceId: ").append(spaceId);
            log.error(msg.toString());
            throw new StorageException(msg.toString());
        }

        return creationTime;
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
        log.debug("addContent(" + spaceId + ", " + contentId + ", " +
                     contentMimeType + ", " + contentSize + ", " +
                     contentChecksum + ")");

        throwIfSpaceNotExist(spaceId);

        if (contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = DEFAULT_MIMETYPE;
        }

        // Wrap the content in order to be able to retrieve a checksum
        ChecksumInputStream wrappedContent = new ChecksumInputStream(content,
                                                                     contentChecksum);

        storeStreamedObject(contentId,
                            contentMimeType,
                            spaceId,
                            wrappedContent);

        // Compare checksum
        return wrappedContent.getMD5();
    }

    private void storeStreamedObject(String contentId,
                                     String contentMimeType,
                                     String spaceId,
                                     ChecksumInputStream wrappedContent) {
        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);

        StringBuilder err = new StringBuilder(
            "Could not add content " + contentName + " with type " +
                contentMimeType + " to Azure container " + containerName +
                " due to error: ");

        try {
            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);

            /* New Blob Properties */
            IBlobProperties blobProperties = new BlobProperties(contentName);

            blobProperties.setContentType(contentMimeType);

            /* Set Blob Contents */
            IBlobContents blobContents = new BlobContents(wrappedContent);
            
            byte[] md5 = wrappedContent.getMD5Bytes();
            // Base64 encode md5
            String base64 = new String(Base64.encodeBase64(md5));
            blobProperties.setContentMD5(base64);

            if (blobContainer.isBlobExist(contentName)) {
                blobContainer.updateBlockBlob(blobProperties, blobContents);                
            }
            else
                blobContainer.createBlockBlob(blobProperties, blobContents);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContent(String spaceId, String contentId) {
        log.debug("getContent(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);

        StringBuilder err = new StringBuilder(
            "Could not retrieve content " + contentName +
                " from Azure container " + containerName + " due to error: ");
        try {
            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);

            IBlockBlob blockBlob = blobContainer.getBlockBlobReference(contentName);

            IBlobProperties blobProperties = blockBlob.getProperties();
            if (null == blobProperties) {
                String errMsg = createNotFoundMsg(spaceId, contentName);
                throw new NotFoundException(errMsg);
            }

            String strBlobNameProp = blobProperties.getName();
            if (!contentName.equals(strBlobNameProp)) {
                throw new StorageException(err.append(String.format(
                    "Wrong blob: '%s'!",
                    strBlobNameProp)).toString());
            }

            BlobMemoryStream stream = new BlobMemoryStream();
            blockBlob.getContents(stream);
            byte[] bytesResult = stream.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                bytesResult);

            return inputStream;
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    private String createNotFoundMsg(String spaceId, String contentId) {
        StringBuilder msg = new StringBuilder();
        msg.append("Could not find content item with ID ");
        msg.append(contentId);
        msg.append(" in space ");
        msg.append(spaceId);
        return msg.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContent(String spaceId, String contentId) {
        log.debug("deleteContent(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        throwIfContentNotExist(spaceId, contentId);

        deleteObject(contentId, spaceId);
    }

    private void deleteObject(String contentId, String spaceId) {
        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);
        StringBuilder err = new StringBuilder(
            "Could not delete content " + contentName + " from Azure container " +
                containerName + " due to error: ");

        try {
            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);

            boolean deleted = blobContainer.deleteBlob(contentName);
            if (!deleted) {
                throw new NotFoundException(err.toString());
            }
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
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
        contentMetadata.remove(METADATA_CONTENT_CHECKSUM);
        contentMetadata.remove(METADATA_CONTENT_MODIFIED);
        contentMetadata.remove(METADATA_CONTENT_SIZE);

        updateContentMetadata(spaceId, contentId, contentMetadata);
    }

    private void updateContentMetadata(String spaceId,
                                       String contentId,
                                       Map<String, String> contentMetadata) {
        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);

        StringBuilder err = new StringBuilder(
            "Could not update metadata for content " + contentName +
                " in Azure container " + containerName + " due to error: ");

        try {
            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);

            IBlockBlob blockBlob = blobContainer.getBlockBlobReference(contentName);
            IBlobProperties blobProperties = blockBlob.getProperties();

            NameValueCollection metadata = new NameValueCollection();
            metadata.putAll(contentMetadata);

            blobProperties.setMetadata(metadata);

            blockBlob.setProperties(blobProperties);
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            throwIfContentNotExist(spaceId, contentId);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId) {
        log.debug("getContentMetadata(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        IBlobProperties metadata = getObjectMetadata(spaceId, contentId);
        if (metadata == null) {
            String err = "No metadata is available for item " + contentId +
                " in Azure space " + spaceId;
            throw new StorageException(err, RETRY);
        }

        Map<String, String> resultMap = new HashMap<String, String>();

        NameValueCollection metadataMap = metadata.getMetadata();

        if (metadataMap == null) {
            metadataMap = new NameValueCollection();
        }

        // Set expected metadata values
        // MIMETYPE
        String mimetype = metadata.getContentType();
        if (mimetype != null) {
            metadataMap.put(METADATA_CONTENT_MIMETYPE, mimetype);
        }
        // MD5
        String md5 = metadata.getContentMD5();
        if (md5 != null) {
            byte[] base64Decoded = Base64.decodeBase64(md5.getBytes());
            String newMd5 = ChecksumUtil.checksumBytesToString(base64Decoded);

            metadataMap.put(METADATA_CONTENT_CHECKSUM, newMd5);
        }
        // SIZE
        long length = metadata.getContentLength();
        String contentLength = Long.toString(length);
        if (contentLength != null) {
            resultMap.put(METADATA_CONTENT_SIZE, contentLength);
        }

        // MODIFIED DATE
        Timestamp modified = metadata.getLastModifiedTime();
        if (modified != null) {
            resultMap.put(METADATA_CONTENT_MODIFIED, modified.toString());
        }

        // Normalize metadata keys to lowercase.
        Iterator keys = metadataMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String val = metadataMap.getSingleValue(key);
            resultMap.put(key.toLowerCase(), val);
        }

        return resultMap;
    }

    private IBlobProperties getObjectMetadata(String spaceId,
                                              String contentId) {
        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);

        StringBuilder err = new StringBuilder(
            "Could not retrieve metadata for content " + contentName +
                " from Azure container " + containerName + " due to error: ");

        try {

            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);

            IBlockBlob blockBlob = blobContainer.getBlockBlobReference(contentName);

            IBlobProperties blobProperties = blockBlob.getProperties();

            if (null == blobProperties) {
                String errMsg = createNotFoundMsg(spaceId, contentId);
                throw new NotFoundException(errMsg);
            }
            return blobProperties;
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        }
    }

    /**
     * Converts a provided space ID into a valid Azure container name.
     * The container name must be a valid DNS name, conforming to the following naming rules:
     * Container names must start with a letter or number, and can contain only letters, numbers,
     * and the dash (-) character.
     * Every dash (-) character must be immediately preceded and followed by a letter or number.
     * All letters in a container name must be lowercase.
     * Container names must be from 3 through 63 characters long.
     *
     * @param spaceId user preferred ID of the space
     * @return spaceId converted to valid Azure container name
     */
    protected String getContainerName(String spaceId) {
        String containerName = spaceId;

        containerName = containerName.replaceAll("[^a-zA-Z0-9]", "-");
        containerName = containerName.toLowerCase();

        if (containerName.length() > 63) {
            containerName = containerName.substring(0, 63);
        }

        return containerName;
    }

    /**
     * Converts a provided content ID into a valid Azure content name.
     * The container name must not contain spaces
     *
     * @param contentId user preferred ID of the content
     * @return contentId converted to valid Azure content name
     */
    protected String getContentName(String contentId) {
        return contentId.replaceAll(" ", "%20");
    }

    /**
     * Converts a provided valid Azure content name into a content ID.
     * The container id can contain spaces
     *
     * @param contentName valid Azure content name
     * @return contentName converted to user preferred ID of the content
     */
    protected String getContentId(String contentName) {
        return contentName.replaceAll("%20", " ");
    }
}
