/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.azurestorage;

import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soyatec.windows.azure.blob.*;
import org.soyatec.windows.azure.blob.io.MemoryStream;
import org.soyatec.windows.azure.error.StorageServerException;
import org.soyatec.windows.azure.util.NameValueCollection;
import org.soyatec.windows.azure.util.TimeSpan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;

/**
 * Provides content storage backed by Azure's Cloud Files service.
 *
 * @author Kristen Cannava
 */
public class AzureStorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(AzureStorageProvider.class);

    protected static final String BLOB_NAMESPACE = "http://blob.core.windows.net/";

    private BlobStorage blobStorage = null;

    public AzureStorageProvider(String username, String apiAccessKey) {


        try {
            blobStorage = BlobStorage.create(
                    URI.create(BLOB_NAMESPACE),
                    false,
                    username,
                    apiAccessKey
            );

            /*
            * Set retry policy for a time interval of 5 seconds.
            */
            blobStorage.setRetryPolicy(RetryPolicies.retryN(1, TimeSpan.fromSeconds(5)));
        } catch (org.soyatec.windows.azure.error.StorageException e) {
            String err = "Could not connect to Azure due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, RETRY);
        } catch (Exception e) {
            String err = "Could not connect to Azure due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    public AzureStorageProvider(BlobStorage blobStorage) {
        this.blobStorage = blobStorage;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpace()");

        List<BlobContainer> containers = listContainers();
        List<String> spaces = new ArrayList<String>();
        for (BlobContainer container : containers) {
            String containerName = container.getContainerName();
            spaces.add(containerName);
        }
        return spaces.iterator();
    }

    private List<BlobContainer> listContainers() {
        StringBuilder err = new StringBuilder("Could not retrieve list of " +
                "Azure containers due to error: ");
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
        Collection<BlobProperties> objects;
        List<String> contentItems = new ArrayList<String>();

        int counter = 0;

        boolean found = false;
        if (marker == null)
            found = true;

        do {

            objects = listObjects(containerName,
                    prefix,
                    maxResults);

            for (BlobProperties object : objects) {
                if (maxResults == counter)
                    break;
                if (found == false && object.getName().equals(marker)) {
                    found = true;
                    continue;
                }
                if (found == true) {
                    contentItems.add(object.getName());
                    counter++;
                }
            }
        } while (found == false);


        return contentItems;
    }

    private Collection<BlobProperties> listObjects(String containerName,
                                                   String prefix,
                                                   long maxResults) {
        StringBuilder err = new StringBuilder("Could not get contents of " +
                "Azure container " + containerName + " due to error: ");
        try {
            int limit = new Long(maxResults).intValue();
            if (prefix == null) {
                prefix = "";
            }

            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);

            // listBlobs does not limit the results
            // maxResults - Specifies the maximum number of blobs to return per call to Azure storage.
            // This does NOT affect list size returned by this function.
            return blobContainer.listBlobs(prefix, false, limit);
        } catch (org.soyatec.windows.azure.error.StorageException e) {
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

        BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);
        if (!blobContainer.doesBlobExist(contentId)) {
            String msg = "Error: Content does not exist: " + contentId;
            throw new NotFoundException(msg);

        }
    }

    private boolean spaceExists(String spaceId) {
        String containerName = getContainerName(spaceId);
        try {
            return blobStorage.doesContainerExist(containerName);
        } catch (org.soyatec.windows.azure.error.StorageException e) {
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
        setSpaceMetadata(spaceId, spaceMetadata);
    }

    private String formattedDate(Date created) {
        RFC822_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return RFC822_DATE_FORMAT.format(created);
    }

    private void createContainer(String spaceId) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder("Could not create Azure " +
                "container with name " + containerName + " due to error: ");
        try {
            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);
            blobContainer.createContainer();
        } catch (org.soyatec.windows.azure.error.StorageException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(String spaceId) {
        log.debug("deleteSpace(" + spaceId + ")");
        throwIfSpaceNotExist(spaceId);

        deleteContainer(spaceId);
    }

    private void deleteContainer(String spaceId) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not delete Azure" +
                " container with name " + containerName + " due to error: ");

        try {
            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);
            blobContainer.deleteContainer();
        } catch (org.soyatec.windows.azure.error.StorageException e) {
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

        BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);

        ContainerProperties containerInfo = getContainerInfo(containerName);
        NameValueCollection values = containerInfo.getMetadata();

        Map<String, String> spaceMetadata = new HashMap<String, String>();

        Iterator<Object> keys = values.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String val = values.getSingleValue(key);
            spaceMetadata.put(key, val);
        }

        int count = -1;
        Collection<BlobProperties> listBlobs
                = blobContainer.listBlobs("", false);

        if (null != listBlobs) {
            count = listBlobs.size();
        }
        spaceMetadata.put(METADATA_SPACE_COUNT,
                String.valueOf(count));

        ContainerAccessControl enumAccess = blobContainer.getContainerAccessControl();
        if (enumAccess != null) {
            if (enumAccess.isPublic())
                spaceMetadata.put(METADATA_SPACE_ACCESS,
                        AccessType.OPEN.name());
            else
                spaceMetadata.put(METADATA_SPACE_ACCESS,
                        AccessType.CLOSED.name());
        }

        return spaceMetadata;
    }

    private ContainerProperties getContainerInfo(String containerName) {
        StringBuilder err = new StringBuilder("Could not retrieve metadata " +
                "from Azure container " + containerName + " due to error: ");

        try {
            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);
            return blobContainer.getContainerProperties();
        } catch (org.soyatec.windows.azure.error.StorageException e) {
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

        // Ensure that space created date is included in the new metadata
        Date created = getCreationDate(spaceId, spaceMetadata);
        if (created != null) {
            spaceMetadata.put(METADATA_SPACE_CREATED, formattedDate(created));
        }

        String containerName = getContainerName(spaceId);

        BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);

        String spaceAccess = spaceMetadata.remove(METADATA_SPACE_ACCESS);
        if (spaceAccess != null) {
            ContainerAccessControl enumAccess = ContainerAccessControl.Private;
            if (spaceAccess.equalsIgnoreCase(AccessType.OPEN.name())) {
                enumAccess = ContainerAccessControl.Public;
            }

            blobContainer.setContainerAccessControl(enumAccess);
        }

        NameValueCollection objMetadataPut = new NameValueCollection();
        objMetadataPut.putAll(spaceMetadata);
        blobContainer.setContainerMetadata(objMetadataPut);
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
                contentMimeType + ", " + contentSize + ", " + contentChecksum + ")");

        throwIfSpaceNotExist(spaceId);

        if (contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = DEFAULT_MIMETYPE;
        }

        // Wrap the content in order to be able to retrieve a checksum
        ChecksumInputStream wrappedContent =
                new ChecksumInputStream(content, contentChecksum);

        storeStreamedObject(contentId,
                contentMimeType,
                spaceId,
                wrappedContent);

        // Compare checksum
        String finalChecksum = wrappedContent.getMD5();
        return compareChecksum(this, spaceId, contentId, finalChecksum);
    }

    private void storeStreamedObject(String contentId, String contentMimeType,
                                     String spaceId,
                                     ChecksumInputStream wrappedContent) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not add content "
                + contentId + " with type " + contentMimeType
                + " to Azure container " + containerName
                + " due to error: ");

        try {
            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);


            /* New Blob Properties */
            BlobProperties blobProperties = new BlobProperties(contentId);

            blobProperties.setContentType(contentMimeType);

            /* Set Blob Contents */
            MemoryStream stream = new MemoryStream(inputStreamToBytes(wrappedContent));
            BlobContents blobContents = new BlobContents(stream);


            if (!blobContainer.createBlob(blobProperties, blobContents, false)) {
                err.append("Failed to create blob");
                throw new StorageException(err.toString(), NO_RETRY);
            }

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

        StringBuilder err = new StringBuilder("Could not retrieve content "
                + contentId + " from Azure container " + containerName
                + " due to error: ");
        try {
            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);
            MemoryStream stream = new MemoryStream();
            BlobContents blobContents = new BlobContents(stream);
            boolean fTransferAsChunks = false;

            BlobProperties blobProperties
                    = blobContainer.getBlob(contentId,
                    blobContents,
                    fTransferAsChunks
            );

            if (null == blobProperties) {
                String errMsg = createNotFoundMsg(spaceId, contentId);
                throw new NotFoundException(errMsg);
            }

            String strBlobNameProp = blobProperties.getName();
            if (!contentId.equals(strBlobNameProp)) {
                throw new StorageException(err.append(String.format("Wrong blob: '%s'!", strBlobNameProp)).toString());
            }

            byte[] bytesResult = stream.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytesResult);

            return inputStream;
        } catch (org.soyatec.windows.azure.error.StorageException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    private String createNotFoundMsg(String spaceId,
                                     String contentId) {
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

    private void deleteObject(String contentId,
                              String spaceId) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not delete content " + contentId
                + " from Azure container " + containerName
                + " due to error: ");

        try {
            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);

            boolean deleted = blobContainer.deleteBlob(contentId);
            if (!deleted)
                throw new NotFoundException(err.toString());
        } catch (org.soyatec.windows.azure.error.StorageException e) {
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

        StringBuilder err = new StringBuilder("Could not update metadata " +
                "for content " + contentId + " in Azure container " +
                containerName + " due to error: ");

        try {
            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);

            BlobProperties blobProperties = blobContainer.getBlobProperties(contentId);

            NameValueCollection metadata = new NameValueCollection();
            metadata.putAll(contentMetadata);

            blobProperties.setMetadata(metadata);

            blobContainer.updateBlobMetadata(blobProperties);
        } catch (org.soyatec.windows.azure.error.StorageException e) {
            throwIfContentNotExist(spaceId, contentId);
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId) {
        log.debug("getContentMetadata(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        BlobProperties metadata = getObjectMetadata(spaceId, contentId);
        if (metadata == null) {
            String err = "No metadata is available for item " + contentId
                    + " in Azure space " + spaceId;
            throw new StorageException(err, RETRY);
        }

        Map<String, String> resultMap = new HashMap<String, String>();

        NameValueCollection metadataMap = metadata.getMetadata();

        if (metadataMap == null)
            metadataMap = new NameValueCollection();

        // Set expected metadata values
        // MIMETYPE
        String mimetype = metadata.getContentType();
        if (mimetype != null) {
            metadataMap.put(METADATA_CONTENT_MIMETYPE, mimetype);
        }
        // SIZE
        long length = metadata.getContentLength();
        String contentLength = Long.toString(length);
        if (contentLength != null) {
            resultMap.put(METADATA_CONTENT_SIZE, contentLength);
        }
        // CHECKSUM
        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String checksum =
                cksumUtil.generateChecksum(getContent(spaceId, contentId));

//        String checksum = metadata.getETag();
        if (checksum != null) {
            resultMap.put(METADATA_CONTENT_CHECKSUM, checksum);

//            byte[] digestBytes = checksum.getBytes();
//
//            StringBuffer hexString = new StringBuffer();
//            for (int i=0; i<digestBytes.length; i++) {
//                String hex=Integer.toHexString(0xff & digestBytes[i]);
//                if(hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
//
//            resultMap.put(METADATA_CONTENT_CHECKSUM, hexString.toString());
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

    private BlobProperties getObjectMetadata(String spaceId,
                                             String contentId) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder("Could not retrieve metadata"
                + " for content " + contentId
                + " from Azure container " + containerName
                + " due to error: ");

        try {

            BlobContainer blobContainer = blobStorage.getBlobContainer(containerName);
            MemoryStream stream = new MemoryStream();
            BlobContents blobContents = new BlobContents(stream);
            boolean fTransferAsChunks = false;

            BlobProperties blobProperties
                    = blobContainer.getBlob(contentId,
                    blobContents,
                    fTransferAsChunks
            );

            if (null == blobProperties) {
                String errMsg = createNotFoundMsg(spaceId, contentId);
                throw new NotFoundException(errMsg);
            }
            return blobProperties;
        } catch (org.soyatec.windows.azure.error.StorageException e) {
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

    public byte[] inputStreamToBytes(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
        return out.toByteArray();
    }
}
