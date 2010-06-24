/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import com.rackspacecloud.client.cloudfiles.FilesAuthorizationException;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesContainerInfo;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;
import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.loadMetadata;
import static org.duracloud.storage.util.StorageProviderUtil.storeMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
 * Provides content storage backed by Rackspace's Cloud Files service.
 *
 * @author Bill Branan
 */
public class RackspaceStorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(RackspaceStorageProvider.class);

    private FilesClient filesClient = null;

    public RackspaceStorageProvider(String username, String apiAccessKey) {
        try {
            filesClient = new FilesClient(username, apiAccessKey);
            if (!filesClient.login()) {
                throw new Exception("Login to Rackspace failed");
            }
        } catch (Exception e) {
            String err = "Could not connect to Rackspace due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    public RackspaceStorageProvider(FilesClient filesClient) {
        this.filesClient = filesClient;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpace()");

        List<FilesContainer> containers = listContainers();
        List<String> spaces = new ArrayList<String>();
        for (FilesContainer container : containers) {
            String containerName = container.getName();
            spaces.add(containerName);
        }
        return spaces.iterator();
    }

    private List<FilesContainer> listContainers() {
        StringBuilder err = new StringBuilder("Could not retrieve list of " +
                "Rackspace containers due to error: ");
        try {
            return filesClient.listContainers();
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
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

        String bucketName = getContainerName(spaceId);
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
        String containerName = getContainerName(spaceId);

        List<FilesObject> objects = listObjects(containerName,
                                                prefix,
                                                maxResults,
                                                marker);
        List<String> contentItems = new ArrayList<String>();
        for (FilesObject object : objects) {
            contentItems.add(object.getName());
        }
        return contentItems;
    }

    private List<FilesObject> listObjects(String containerName,
                                          String prefix,
                                          long maxResults,
                                          String marker) {
        StringBuilder err = new StringBuilder("Could not get contents of " +
                "Rackspace container " + containerName + " due to error: ");
        try {
            int limit = new Long(maxResults).intValue();
            if (prefix != null) {
                return listObjectsStartingWith(containerName,
                                               prefix,
                                               limit,
                                               marker);
            } else {
                return filesClient.listObjects(containerName,
                                               null,
                                               limit,
                                               marker);
            }
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /*
     * The listObjectsStartingWith() call has a particularly high failure rate,
     * this method handles the call with a built in retry (up to 10 attempts).
     * TODO: Call listObjectsStartingWith() directly once it no longer fails regularly
     */
    private List<FilesObject> listObjectsStartingWith(String containerName,
                                                      String prefix,
                                                      int limit,
                                                      String marker)
        throws IOException {
        int retryLimit = 10;
        int retries = 0;
        List<FilesObject> objectList = null;
        while(objectList == null) {
            try {
                objectList = filesClient.listObjectsStartingWith(containerName,
                                                                 prefix,
                                                                 null,
                                                                 limit,
                                                                 marker);
            } catch(IOException e) {
                log.error("Error listing objects.", e);
                objectList = null;                
                if(retries < retryLimit) {
                    retries++;
                } else {
                    throw e;
                }
            }
        }
        return objectList;
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
        // Attempt to get content metadata, which throws if content does not exist
        getObjectMetadata(spaceId, contentId);
    }

    private boolean spaceExists(String spaceId) {
        String containerName = getContainerName(spaceId);
        try {
            return filesClient.containerExists(containerName);
        } catch (IOException e) {
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
        // Note: According to Rackspace support (ticket #13597) there are no
        // dates recorded for containers, so store our own created date        
        Map<String, String> spaceMetadata = new HashMap<String, String>();
        Date created = new Date(System.currentTimeMillis());
        spaceMetadata.put(METADATA_SPACE_CREATED, formattedDate(created));
        spaceMetadata.put(METADATA_SPACE_ACCESS, AccessType.CLOSED.name());
        setSpaceMetadata(spaceId, spaceMetadata);
    }

    private String formattedDate(Date created) {
        RFC822_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return RFC822_DATE_FORMAT.format(created);
    }

    private void createContainer(String spaceId) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder("Could not create Rackspace " +
                "container with name " + containerName + " due to error: ");
        try {
            filesClient.createContainer(containerName);
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
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

        String bucketMetadata =
            getContainerName(spaceId) + SPACE_METADATA_SUFFIX;
        try {
            deleteContent(spaceId, bucketMetadata);
        } catch(NotFoundException e) {
            // Metadata has already been removed. Continue deleting space.
        }

        deleteContainer(spaceId);
    }

    private void deleteContainer(String spaceId) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not delete Rackspace" +
                " container with name " + containerName + " due to error: ");

        try {
            filesClient.deleteContainer(containerName);
        } catch (FilesNotFoundException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getSpaceMetadata(String spaceId) {
        log.debug("getSpaceMetadata(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        // Space metadata is stored as a content item
        String containerName = getContainerName(spaceId);
        InputStream is =
                getContent(spaceId, containerName + SPACE_METADATA_SUFFIX);
        Map<String, String> spaceMetadata = loadMetadata(is);

        FilesContainerInfo containerInfo = getContainerInfo(containerName);

        final int sysMetadataObjectCount = 1;
        int totalObjectCount = containerInfo.getObjectCount();
        int visibleObjectCount = totalObjectCount - sysMetadataObjectCount;
        spaceMetadata.put(METADATA_SPACE_COUNT,
                          String.valueOf(visibleObjectCount));

        spaceMetadata.put("space-total-size", String.valueOf(containerInfo
                .getTotalSize()));

        return spaceMetadata;
    }

    private FilesContainerInfo getContainerInfo(String containerName) {
        StringBuilder err = new StringBuilder("Could not retrieve metadata " +
                "from Rackspace container " + containerName + " due to error: ");

        try {
            return filesClient.getContainerInfo(containerName);
        } catch (FilesNotFoundException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
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

        // Ensure that space access is included in the new metadata
        if(!spaceMetadata.containsKey(METADATA_SPACE_ACCESS)) {
            String spaceAccess = getSpaceAccess(spaceId).name();
            spaceMetadata.put(METADATA_SPACE_ACCESS, spaceAccess);
        }

        String containerName = getContainerName(spaceId);
        ByteArrayInputStream is = storeMetadata(spaceMetadata);
        addContent(spaceId,
                   containerName + SPACE_METADATA_SUFFIX,
                   "text/xml",
                   is.available(),
                   null,
                   is);
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
            created =  RFC822_DATE_FORMAT.parse(dateText);
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
        log.debug("addContent("+ spaceId +", "+ contentId +", "+
            contentMimeType +", "+ contentSize +", "+ contentChecksum +")");

        throwIfSpaceNotExist(spaceId);

        if(contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = DEFAULT_MIMETYPE;
        }        

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(METADATA_CONTENT_MIMETYPE, contentMimeType);

        // Wrap the content in order to be able to retrieve a checksum
        ChecksumInputStream wrappedContent =
            new ChecksumInputStream(content, contentChecksum);

        storeStreamedObject(contentId,
                            contentMimeType,
                            spaceId,
                            metadata,
                            wrappedContent);

        // Compare checksum
        String finalChecksum = wrappedContent.getMD5();
        return compareChecksum(this, spaceId, contentId, finalChecksum);
    }

    private void storeStreamedObject(String contentId, String contentMimeType,
                                     String spaceId,
                                     Map<String, String> metadata,
                                     ChecksumInputStream wrappedContent) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not add content "
                + contentId + " with type " + contentMimeType
                + " to Rackspace container " + containerName
                + " due to error: ");

        try {
            filesClient.storeStreamedObject(containerName,
                                            wrappedContent,
                                            contentMimeType,
                                            contentId,
                                            metadata);
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
                + contentId + " from Rackspace container " + containerName
                + " due to error: ");
        try {
            InputStream content =
                filesClient.getObjectAsStream(containerName, contentId);

            if(content == null) {        
                String errMsg = createNotFoundMsg(spaceId, contentId);
                throw new NotFoundException(errMsg);
            }

            return content;
        } catch (FilesNotFoundException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);            
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
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

        deleteObject(contentId, spaceId);
    }

    private void deleteObject(String contentId,
                              String spaceId) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not delete content " + contentId
                    + " from Rackspace container " + containerName
                    + " due to error: ");

        try {
            filesClient.deleteObject(containerName, contentId);
        } catch (FilesNotFoundException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
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

        // Set mimetype
        String contentMimeType =
            contentMetadata.remove(METADATA_CONTENT_MIMETYPE);
        if(contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = getObjectMetadata(spaceId, contentId).getMimeType();
        }
        // Note: It is not currently possible to set MIME type on a
        // Rackspace object directly, so setting a custom field instead.
        contentMetadata.put(METADATA_CONTENT_MIMETYPE, contentMimeType);
        
        updateContentMetadata(spaceId, contentId, contentMetadata);
    }

    private void updateContentMetadata(String spaceId,
                                       String contentId,
                                       Map<String, String> contentMetadata) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder("Could not update metadata " +
            "for content " + contentId + " in Rackspace container " +
            containerName + " due to error: ");

        try {
            filesClient.updateObjectMetadata(containerName,
                                             contentId,
                                             contentMetadata);
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            throwIfContentNotExist(spaceId, contentId);
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId) {
        log.debug("getContentMetadata(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        FilesObjectMetaData metadata = getObjectMetadata(spaceId, contentId);
        if (metadata == null) {
            String err = "No metadata is available for item " + contentId
                    + " in Rackspace space " + spaceId;
            throw new StorageException(err, RETRY);
        }

        Map<String, String> metadataMap = metadata.getMetaData();

        // Set expected metadata values

        // MIMETYPE
        // METADATA_CONTENT_MIMETYPE value is set directly by add/update content
        // SIZE
        String contentLength = metadata.getContentLength();
        if (contentLength != null) {
            metadataMap.put(METADATA_CONTENT_SIZE, contentLength);
        }
        // CHECKSUM
        String checksum = metadata.getETag();
        if (checksum != null) {
            metadataMap.put(METADATA_CONTENT_CHECKSUM, checksum);
        }
        // MODIFIED DATE
        String modified = metadata.getLastModified();
        if (modified != null) {
            metadataMap.put(METADATA_CONTENT_MODIFIED, modified);
        }

        // Normalize metadata keys to lowercase.
        Map<String, String> resultMap = new HashMap<String, String>();
        Iterator<String> keys = metadataMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String val = metadataMap.get(key);
            resultMap.put(key.toLowerCase(), val);
        }

        return resultMap;
    }

    private FilesObjectMetaData getObjectMetadata(String spaceId,
                                                  String contentId) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder("Could not retrieve metadata"
                + " for content " + contentId
                + " from Rackspace container " + containerName
                + " due to error: ");

        try {
            FilesObjectMetaData metadata =
                filesClient.getObjectMetaData(containerName, contentId);

            if(metadata == null) {
                String errMsg = createNotFoundMsg(spaceId, contentId);
                throw new NotFoundException(errMsg);
            }

            return metadata;
        } catch (FilesNotFoundException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);            
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * Converts a provided space ID into a valid Rackspace container name. From
     * Cloud Files Docs: The only restrictions on Container names is that they
     * cannot contain a forward slash (/) character or a question mark (?)
     * character and they must be less than 64 characters in length (after URL
     * encoding).
     *
     * @param spaceId user preferred ID of the space
     * @return spaceId converted to valid Rackspace container name
     */
    protected String getContainerName(String spaceId) {
        String containerName = spaceId;
        containerName = containerName.replaceAll("/", "-");
        containerName = containerName.replaceAll("[?]", "-");
        containerName = containerName.replaceAll("[-]+", "-");
        containerName = FilesClient.sanitizeForURI(containerName);

        if (containerName.length() > 63) {
            containerName = containerName.substring(0, 63);
        }

        return containerName;
    }

}
