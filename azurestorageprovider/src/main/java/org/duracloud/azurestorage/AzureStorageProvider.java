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
import org.soyatec.windowsazure.blob.BlobStorageClient;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IBlobContents;
import org.soyatec.windowsazure.blob.IBlobProperties;
import org.soyatec.windowsazure.blob.IBlockBlob;
import org.soyatec.windowsazure.blob.IContainerProperties;
import org.soyatec.windowsazure.blob.internal.BlobContents;
import org.soyatec.windowsazure.blob.internal.BlobProperties;
import org.soyatec.windowsazure.blob.internal.RetryPolicies;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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

        // Gets the entire list of objects (maxResults is essentially ignored)
        objects = listObjects(containerName, prefix, maxResults);

        while (objects.hasNext()) {
            IBlobProperties object = objects.next();
            if (maxResults == counter) {
                break;
            }

            String contentId = getContentId(object.getName());
            if (found == false && contentId.equals(marker)) {
                found = true;
                continue;
            }
            if (found == true) {
                contentItems.add(contentId);
                counter++;
            }
        }

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

            // Currently, listBlobs() returns the entire list of items,
            // regardless of how large the list is. (The maxResults parameter
            // only determines the number of calls listBlobs() needs to make
            // to be able to collect the entire list before returning.)
            return blobContainer.listBlobs(prefix, false, limit);
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
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

    protected boolean spaceExists(String spaceId) {
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

        // Add space properties
        Map<String, String> spaceProperties = new HashMap<String, String>();
        Date created = new Date(System.currentTimeMillis());
        spaceProperties.put(PROPERTIES_SPACE_CREATED, formattedDate(created));
        setSpacePropertiesInt(spaceId, spaceProperties);
    }

    private String formattedDate(Date created) {
        ISO8601_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return ISO8601_DATE_FORMAT.format(created);
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
    public Map<String, String> getAllSpaceProperties(String spaceId) {
        log.debug("getSpaceProperties(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        String containerName = getContainerName(spaceId);

        IBlobContainer blobContainer = blobStorage.getBlobContainer(
            containerName);

        IContainerProperties containerInfo = getContainerInfo(containerName);
        NameValueCollection values = containerInfo.getMetadata();

        Map<String, String> spaceProperties = new HashMap<String, String>();

        Iterator<Object> keys = values.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String val = values.getSingleValue(key);
            spaceProperties.put(key, val);
        }

        int count = 0;
        Iterator<IBlobProperties> blobs = blobContainer.listBlobs("", false);

        while (blobs != null && blobs.hasNext()) {
            count++;
            blobs.next();
        }
        spaceProperties.put(PROPERTIES_SPACE_COUNT, String.valueOf(count));

        return spaceProperties;
    }

    private IContainerProperties getContainerInfo(String containerName) {
        StringBuilder err = new StringBuilder(
            "Could not retrieve properties " + "from Azure container " +
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
    protected void doSetSpaceProperties(String spaceId,
                                        Map<String, String> spaceProperties) {
        log.debug("setSpaceProperties(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        setSpacePropertiesInt(spaceId, spaceProperties);
    }

    private void setSpacePropertiesInt(String spaceId,
                                       Map<String, String> spaceProperties) {
        log.debug("setSpaceProperties(" + spaceId + ")");

        // Ensure that space created date is included in the new properties
        Date created = getCreationDate(spaceId, spaceProperties);
        if (created != null) {
            spaceProperties.put(PROPERTIES_SPACE_CREATED,
                                formattedDate(created));
        }

        String containerName = getContainerName(spaceId);

        IBlobContainer blobContainer = blobStorage.getBlobContainer(
            containerName);

        NameValueCollection objPropertiesPut = new NameValueCollection();
        objPropertiesPut.putAll(spaceProperties);
        blobContainer.setMetadata(objPropertiesPut);
    }

    private Date getCreationDate(String spaceId,
                                 Map<String, String> spaceProperties) {
        String dateText;
        if (!spaceProperties.containsKey(PROPERTIES_SPACE_CREATED)) {
            dateText = getCreationTimestamp(spaceId);
        } else {
            dateText = spaceProperties.get(PROPERTIES_SPACE_CREATED);
        }

        Date created = null;
        try {
            created = ISO8601_DATE_FORMAT.parse(dateText);
        } catch (ParseException e) {
            log.warn("Unable to parse date: '" + dateText + "'");
        }
        return created;
    }

    private String getCreationTimestamp(String spaceId) {
        Map<String, String> spaceMd = getAllSpaceProperties(spaceId);
        String creationTime = spaceMd.get(PROPERTIES_SPACE_CREATED);

        if (creationTime == null) {
            StringBuffer msg = new StringBuffer("Error: ");
            msg.append("No ").append(PROPERTIES_SPACE_CREATED).append(" found ");
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
                             Map<String, String> userProperties,
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
                            wrappedContent,
                            userProperties);

        // Compare checksum
        return wrappedContent.getMD5();
    }

    private void storeStreamedObject(String contentId,
                                     String contentMimeType,
                                     String spaceId,
                                     ChecksumInputStream wrappedContent,
                                     Map<String, String> userProperties) {
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

            if(userProperties != null) {
                NameValueCollection properties = new NameValueCollection();
                properties.putAll(userProperties);

                blobProperties.setMetadata(properties);
            }

            /* Set Blob Contents */
            IBlobContents blobContents = new BlobContents(wrappedContent);
            
            byte[] md5 = wrappedContent.getMD5Bytes();
            // Base64 encode md5
            String base64 = new String(Base64.encodeBase64(md5));
            blobProperties.setContentMD5(base64);

            if (blobContainer.isBlobExist(contentName)) {
                blobContainer.updateBlockBlob(blobProperties, blobContents);
            } else {
                blobContainer.createBlockBlob(blobProperties, blobContents);
            }
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        }
    }

    @Override
    public String copyContent(String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) throws StorageException {
        String sourceContainerName = getContainerName(sourceSpaceId);
        String destContainerName = getContainerName(destSpaceId);

        throwIfContentNotExist(sourceContainerName, sourceContentId);
        throwIfSpaceNotExist(destSpaceId);

        IBlobContainer blobContainer = blobStorage.getBlobContainer(
            sourceContainerName);

        doCopyBlob(sourceContentId,
                   destContentId,
                   destContainerName,
                   blobContainer);

        return "no-md5-guarantees";
    }

    private void doCopyBlob(String sourceContentId,
                            String destContentId,
                            String destContainerName,
                            IBlobContainer blobContainer) {
        boolean success = false;
        final int numTries = 5;
        int tries = 0;
        while (!success && tries++ < numTries) {
            try {
                success = blobContainer.copyBlob(destContainerName,
                                                 destContentId,
                                                 sourceContentId);
            } catch (Exception e) {
                log.warn("Exception copying blob: {}", e.getMessage());
            }

            if (!success) {
                log.warn("Error copying blob: {} / {}, {}. Tries left: {}",
                         new Object[]{blobContainer.getName(),
                                      sourceContentId,
                                      tries,
                                      numTries - tries});
            }
        }

        if (!success) {
            StringBuilder err = new StringBuilder("Error copying blob from: ");
            err.append(blobContainer.getName());
            err.append(" / ");
            err.append(sourceContentId);
            err.append(", to: ");
            err.append(destContainerName);
            err.append(" / ");
            err.append(destContentId);
            throw new StorageException(err.toString());
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
    public void setContentProperties(String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties) {
        log.debug("setContentProperties(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        // Remove calculated properties
        contentProperties.remove(PROPERTIES_CONTENT_CHECKSUM);
        contentProperties.remove(PROPERTIES_CONTENT_MODIFIED);
        contentProperties.remove(PROPERTIES_CONTENT_SIZE);

        updateContentProperties(spaceId, contentId, contentProperties);
    }

    private void updateContentProperties(String spaceId,
                                         String contentId,
                                         Map<String, String> contentProperties) {
        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);

        StringBuilder err = new StringBuilder(
            "Could not update properties for content " + contentName +
                " in Azure container " + containerName + " due to error: ");

        try {
            IBlobContainer blobContainer = blobStorage.getBlobContainer(
                containerName);

            IBlockBlob blockBlob = blobContainer.getBlockBlobReference(contentName);
            IBlobProperties blobProperties = blockBlob.getProperties();

            NameValueCollection properties = new NameValueCollection();
            properties.putAll(contentProperties);

            blobProperties.setMetadata(properties);

            blockBlob.setProperties(blobProperties);
        } catch (org.soyatec.windowsazure.error.StorageException e) {
            throwIfContentNotExist(spaceId, contentId);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId) {
        log.debug("getContentProperties(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        IBlobProperties properties = getObjectProperties(spaceId, contentId);
        if (properties == null) {
            String err = "No properties is available for item " + contentId +
                " in Azure space " + spaceId;
            throw new StorageException(err, RETRY);
        }

        Map<String, String> resultMap = new HashMap<String, String>();

        NameValueCollection propertiesMap = properties.getMetadata();

        if (propertiesMap == null) {
            propertiesMap = new NameValueCollection();
        }

        // Set expected property values
        // MIMETYPE
        String mimetype = properties.getContentType();
        if (mimetype != null) {
            propertiesMap.put(PROPERTIES_CONTENT_MIMETYPE, mimetype);
        }
        // MD5
        String md5 = properties.getContentMD5();
        if (md5 != null) {
            byte[] base64Decoded = Base64.decodeBase64(md5.getBytes());
            String newMd5 = ChecksumUtil.checksumBytesToString(base64Decoded);

            propertiesMap.put(PROPERTIES_CONTENT_CHECKSUM, newMd5);
        }
        // SIZE
        long length = properties.getContentLength();
        String contentLength = Long.toString(length);
        if (contentLength != null) {
            resultMap.put(PROPERTIES_CONTENT_SIZE, contentLength);
        }

        // MODIFIED DATE
        Timestamp modified = properties.getLastModifiedTime();
        if (modified != null) {
            resultMap.put(PROPERTIES_CONTENT_MODIFIED, modified.toString());
        }

        // Normalize properties keys to lowercase.
        Iterator keys = propertiesMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String val = propertiesMap.getSingleValue(key);
            resultMap.put(key.toLowerCase(), val);
        }

        return resultMap;
    }

    private IBlobProperties getObjectProperties(String spaceId,
                                                String contentId) {
        String containerName = getContainerName(spaceId);
        String contentName = getContentName(contentId);

        StringBuilder err = new StringBuilder(
            "Could not retrieve properties for content " + contentName +
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
