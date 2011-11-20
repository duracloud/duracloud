/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import com.rackspacecloud.client.cloudfiles.*;
import org.apache.http.HttpException;
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

import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.loadProperties;
import static org.duracloud.storage.util.StorageProviderUtil.storeProperties;

/**
 * Provides content storage backed by Rackspace's Cloud Files service.
 *
 * @author Bill Branan
 */
public class RackspaceStorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(RackspaceStorageProvider.class);

    private static String authUrl= "https://auth.api.rackspacecloud.com/v1.0";
    private FilesClient filesClient = null;

    public RackspaceStorageProvider(String username,
                                    String apiAccessKey,
                                    String authUrl) {
        try {
            filesClient = new FilesClient(username, apiAccessKey, authUrl);
            if (!filesClient.login()) {
                throw new Exception("Login to Rackspace failed");
            }
        } catch (Exception e) {
            String err = "Could not connect to Rackspace due to error: "
                    + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    public RackspaceStorageProvider(String username, String apiAccessKey) {
        this(username, apiAccessKey, authUrl);
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
        } catch (FilesException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
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
        String bucketProperties = bucketName + SPACE_PROPERTIES_SUFFIX;

        if(maxResults <= 0) {
            maxResults = StorageProvider.DEFAULT_MAX_RESULTS;
        }

        // Queries for maxResults +1 to account for the possibility of needing
        // to remove the space properties but still maintain a full result
        // set (size == maxResults).
        List<String> spaceContents =
            getCompleteSpaceContents(spaceId, prefix, maxResults + 1, marker);

        if(spaceContents.contains(bucketProperties)) {
            // Remove space properties
            spaceContents.remove(bucketProperties);
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
        } catch (FilesException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (HttpException e) {
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
        throws IOException, FilesException {
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
            } catch (FilesException e) {
                log.error("Error listing objects.", e);
                objectList = null;
                if (retries < retryLimit) {
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
        // Attempt to get content properties, which throws if content does not exist
        getObjectProperties(spaceId, contentId);
    }

    private boolean spaceExists(String spaceId) {
        String containerName = getContainerName(spaceId);
        try {
            return filesClient.containerExists(containerName);
        } catch (IOException e) {
            return false;
        } catch (HttpException e) {
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
        // Note: According to Rackspace support (ticket #13597) there are no
        // dates recorded for containers, so store our own created date        
        Map<String, String> spaceProperties = new HashMap<String, String>();
        Date created = new Date(System.currentTimeMillis());
        spaceProperties.put(PROPERTIES_SPACE_CREATED, formattedDate(created));
        spaceProperties.put(PROPERTIES_SPACE_ACCESS, AccessType.CLOSED.name());
        doSetSpaceProperties(spaceId, spaceProperties);
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
        } catch (FilesException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeSpace(String spaceId) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not delete Rackspace" +
                " container with name " + containerName + " due to error: ");

        String bucketProperties =
            getContainerName(spaceId) + SPACE_PROPERTIES_SUFFIX;
        try {
            deleteContent(spaceId, bucketProperties);
        } catch(NotFoundException e) {
            // Properties has already been removed. Continue deleting space.
        }

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
        } catch (FilesInvalidNameException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (FilesContainerNotEmptyException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Map<String, String> getAllSpaceProperties(String spaceId) {
        log.debug("getAllSpaceProperties(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        // Space properties is stored as a content item
        String containerName = getContainerName(spaceId);
        InputStream is =
                getContent(spaceId, containerName + SPACE_PROPERTIES_SUFFIX);
        Map<String, String> spaceProperties = loadProperties(is);

        FilesContainerInfo containerInfo = getContainerInfo(containerName);

        final int sysPropertiesObjectCount = 1;
        int totalObjectCount = containerInfo.getObjectCount();
        int visibleObjectCount = totalObjectCount - sysPropertiesObjectCount;
        spaceProperties.put(PROPERTIES_SPACE_COUNT,
                          String.valueOf(visibleObjectCount));

        spaceProperties.put(PROPERTIES_SPACE_SIZE, String.valueOf(containerInfo
                .getTotalSize()));

        return spaceProperties;
    }

    private FilesContainerInfo getContainerInfo(String containerName) {
        StringBuilder err = new StringBuilder("Could not retrieve properties " +
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
        } catch (FilesException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetSpaceProperties(String spaceId,
                                        Map<String, String> spaceProperties) {
        log.debug("doSetSpaceProperties(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        // Ensure that space created date is included in the new properties
        Date created = getCreationDate(spaceId, spaceProperties);
        if (created != null) {
            spaceProperties.put(PROPERTIES_SPACE_CREATED, formattedDate(created));
        }

        // Ensure that space access is included in the new properties
        if(!spaceProperties.containsKey(PROPERTIES_SPACE_ACCESS)) {
            String spaceAccess = getSpaceAccess(spaceId).name();
            spaceProperties.put(PROPERTIES_SPACE_ACCESS, spaceAccess);
        }

        String containerName = getContainerName(spaceId);
        ByteArrayInputStream is = storeProperties(spaceProperties);
        addContent(spaceId,
                   containerName + SPACE_PROPERTIES_SUFFIX,
                   "text/xml",
                   null,
                   is.available(),
                   null,
                   is);
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
            created =  RFC822_DATE_FORMAT.parse(dateText);
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
        log.debug("addContent("+ spaceId +", "+ contentId +", "+
            contentMimeType +", "+ contentSize +", "+ contentChecksum +")");

        throwIfSpaceNotExist(spaceId);

        if(contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = DEFAULT_MIMETYPE;
        }        

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PROPERTIES_CONTENT_MIMETYPE, contentMimeType);

        if(userProperties != null) {
            for (String key : userProperties.keySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("[" + key + "|" + userProperties.get(key) + "]");
                }
                properties.put(getSpaceFree(key),
                               userProperties.get(key));
            }
        }

        // Wrap the content in order to be able to retrieve a checksum
        ChecksumInputStream wrappedContent =
            new ChecksumInputStream(content, contentChecksum);

        String providerChecksum = storeStreamedObject(contentId,
                                                      contentMimeType,
                                                      spaceId,
                                                      properties,
                                                      wrappedContent);

        // Compare checksum
        String checksum = wrappedContent.getMD5();
        return compareChecksum(providerChecksum, spaceId, contentId, checksum);
    }

    private String storeStreamedObject(String contentId, String contentMimeType,
                                     String spaceId,
                                     Map<String, String> properties,
                                     ChecksumInputStream wrappedContent) {
        String containerName = getContainerName(spaceId);
        StringBuilder err = new StringBuilder("Could not add content "
                + contentId + " with type " + contentMimeType
                + " to Rackspace container " + containerName
                + " due to error: ");

        try {
            return filesClient.storeStreamedObject(containerName,
                                            wrappedContent,
                                            contentMimeType,
                                            contentId,
                                            properties);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (FilesException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
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

        throwIfContentNotExist(sourceSpaceId, sourceContentId);
        throwIfSpaceNotExist(destSpaceId);

        String md5 = doCopyContent(sourceSpaceId,
                                   sourceContentId,
                                   destSpaceId,
                                   destContentId);

        return StorageProviderUtil.compareChecksum(this,
                                                   sourceSpaceId,
                                                   sourceContentId,
                                                   md5);
    }

    private String doCopyContent(String sourceSpaceId,
                                 String sourceContentId,
                                 String destSpaceId,
                                 String destContentId) {
        StringBuilder err = new StringBuilder("Could not copy content from: ");
        err.append(sourceSpaceId);
        err.append(" / ");
        err.append(sourceContentId);
        err.append(", to: ");
        err.append(destSpaceId);
        err.append(" / ");
        err.append(destContentId);
        err.append(", due to error: ");
        try {
            return filesClient.copyObject(sourceSpaceId,
                                          sourceContentId,
                                          destSpaceId,
                                          destContentId);

        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
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
        } catch (FilesInvalidNameException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (HttpException e) {
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
        StringBuilder err =
            new StringBuilder("Could not delete content " + contentId +
                              " from Rackspace container " + containerName +
                              " due to error: ");
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
        } catch (FilesException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
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
        throwIfContentNotExist(spaceId, contentId);        

        // Remove calculated properties
        contentProperties.remove(PROPERTIES_CONTENT_CHECKSUM);
        contentProperties.remove(PROPERTIES_CONTENT_MODIFIED);
        contentProperties.remove(PROPERTIES_CONTENT_SIZE);

        // Set mimetype
        String contentMimeType =
            contentProperties.remove(PROPERTIES_CONTENT_MIMETYPE);
        if(contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = getObjectProperties(spaceId, contentId).getMimeType();
        }
        // Note: It is not currently possible to set MIME type on a
        // Rackspace object directly, so setting a custom field instead.
        contentProperties.put(PROPERTIES_CONTENT_MIMETYPE, contentMimeType);

        Map<String, String> newContentProperties = new HashMap<String, String>();
        for (String key : contentProperties.keySet()) {
            if (log.isDebugEnabled()) {
                log.debug("[" + key + "|" + contentProperties.get(key) + "]");
            }
            newContentProperties.put(getSpaceFree(key),
                                     contentProperties.get(key));
        }
        
        updateContentProperties(spaceId, contentId, newContentProperties);
    }

    private void updateContentProperties(String spaceId,
                                         String contentId,
                                         Map<String, String> contentProperties) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder("Could not update properties " +
            "for content " + contentId + " in Rackspace container " +
            containerName + " due to error: ");

        try {
            filesClient.updateObjectMetadata(containerName,
                                             contentId,
                                             contentProperties);
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            throwIfContentNotExist(spaceId, contentId);
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (FilesInvalidNameException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (HttpException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId) {
        log.debug("getContentProperties(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        FilesObjectMetaData properties = getObjectProperties(spaceId, contentId);
        if (properties == null) {
            String err = "No properties is available for item " + contentId
                    + " in Rackspace space " + spaceId;
            throw new StorageException(err, RETRY);
        }

        Map<String, String> propertiesMap = properties.getMetaData();

        // Set expected property values

        // MIMETYPE
        // PROPERTIES_CONTENT_MIMETYPE value is set directly by add/update content
        // SIZE
        String contentLength = properties.getContentLength();
        if (contentLength != null) {
            propertiesMap.put(PROPERTIES_CONTENT_SIZE, contentLength);
        }
        // CHECKSUM
        String checksum = properties.getETag();
        if (checksum != null) {
            propertiesMap.put(PROPERTIES_CONTENT_CHECKSUM, checksum);
        }
        // MODIFIED DATE
        String modified = properties.getLastModified();
        if (modified != null) {
            propertiesMap.put(PROPERTIES_CONTENT_MODIFIED, modified);
        }

        // Normalize properties keys to lowercase.
        Map<String, String> resultMap = new HashMap<String, String>();
        Iterator<String> keys = propertiesMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String val = propertiesMap.get(key);
            resultMap.put(getWithSpace(key.toLowerCase()), val);
        }

        return resultMap;
    }

    private FilesObjectMetaData getObjectProperties(String spaceId,
                                                    String contentId) {
        String containerName = getContainerName(spaceId);

        StringBuilder err = new StringBuilder("Could not retrieve properties"
                + " for content " + contentId
                + " from Rackspace container " + containerName
                + " due to error: ");

        try {
            FilesObjectMetaData properties =
                filesClient.getObjectMetaData(containerName, contentId);

            if(properties == null) {
                String errMsg = createNotFoundMsg(spaceId, contentId);
                throw new NotFoundException(errMsg);
            }

            return properties;
        } catch (FilesNotFoundException e) {
            err.append(e.getMessage());
            throw new NotFoundException(err.toString(), e);            
        } catch (FilesAuthorizationException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (IOException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, RETRY);
        } catch (FilesInvalidNameException e) {
            err.append(e.getMessage());
            throw new StorageException(err.toString(), e, NO_RETRY);
        } catch (HttpException e) {
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
