/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.openstackstorage;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Module;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.error.ChecksumMismatchException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.duracloud.storage.util.StorageProviderUtil;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.openstack.swift.CopyObjectException;
import org.jclouds.openstack.swift.SwiftApiMetadata;
import org.jclouds.openstack.swift.SwiftClient;
import org.jclouds.openstack.swift.domain.ContainerMetadata;
import org.jclouds.openstack.swift.domain.MutableObjectInfoWithMetadata;
import org.jclouds.openstack.swift.domain.ObjectInfo;
import org.jclouds.openstack.swift.domain.SwiftObject;
import org.jclouds.openstack.swift.options.CreateContainerOptions;
import org.jclouds.openstack.swift.options.ListContainerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;

/**
 * Provides content storage access to OpenStack storage providers.
 *
 * @author Bill Branan
 */
public abstract class OpenStackStorageProvider extends StorageProviderBase {

    private static final Logger log =
        LoggerFactory.getLogger(OpenStackStorageProvider.class);

    private SwiftClient swiftClient = null;

    public OpenStackStorageProvider(String username,
                                    String apiAccessKey,
                                    String authUrl) {
        if (null == authUrl) {
            authUrl = getAuthUrl();
        }

        try {
            String trimmedAuthUrl = // JClouds expects authURL with no version
                    authUrl.substring(0, authUrl.lastIndexOf("/"));

            ListeningExecutorService useExecutor = createThreadPool();
            ListeningExecutorService ioExecutor = createThreadPool();

            Iterable<Module> modules = ImmutableSet.<Module> of(
                new EnterpriseConfigurationModule(useExecutor, ioExecutor));
            Properties properties = new Properties();
            properties.setProperty(Constants.PROPERTY_STRIP_EXPECT_HEADER,
                                   "true");
            swiftClient = ContextBuilder.newBuilder(new SwiftApiMetadata())
                            .endpoint(trimmedAuthUrl)
                            .credentials(username, apiAccessKey)
                            .modules(modules)
                            .overrides(properties)
                            .buildApi(SwiftClient.class);
        } catch (Exception e) {
            String err = "Could not connect to " + getProviderName() +
                    " due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    protected ListeningExecutorService createThreadPool() {
        return MoreExecutors.listeningDecorator(
                    new ThreadPoolExecutor(0,
                                           Integer.MAX_VALUE,
                                           5L,
                                           TimeUnit.SECONDS,
                                           new SynchronousQueue<Runnable>()));
    }

    public OpenStackStorageProvider(String username, String apiAccessKey) {
        this(username, apiAccessKey, null);
    }

    public OpenStackStorageProvider(SwiftClient swiftClient) {
        this.swiftClient = swiftClient;
    }

    public abstract String getAuthUrl();
    public abstract String getProviderName();

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpace()");

        Set<ContainerMetadata> containers =
                swiftClient.listContainers(ListContainerOptions.NONE);
        List<String> spaces = new ArrayList<String>();
        for (ContainerMetadata container : containers) {
            String containerName = container.getName();
            spaces.add(containerName);
        }
        return spaces.iterator();
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

        if(maxResults <= 0) {
            maxResults = StorageProvider.DEFAULT_MAX_RESULTS;
        }

        List<String> spaceContents =
                getCompleteSpaceContents(spaceId, prefix, maxResults, marker);

        return spaceContents;
    }

    private List<String> getCompleteSpaceContents(String spaceId,
                                                  String prefix,
                                                  long maxResults,
                                                  String marker) {
        String containerName = getContainerName(spaceId);

        PageSet<ObjectInfo> objects = listObjects(containerName,
                prefix,
                maxResults,
                marker);
        List<String> contentItems = new ArrayList<String>();
        for (ObjectInfo object : objects) {
            contentItems.add(object.getName());
        }
        return contentItems;
    }

    private PageSet<ObjectInfo> listObjects(String containerName,
                                            String prefix,
                                            long maxResults,
                                            String marker) {
        int limit = new Long(maxResults).intValue();
        ListContainerOptions containerOptions = ListContainerOptions.Builder.maxResults(limit);
        if(marker != null) containerOptions.afterMarker(marker);
        if(prefix != null) containerOptions.withPrefix(prefix);
        return swiftClient.listObjects(containerName,
                containerOptions);
    }

    private void throwIfContentNotExist(String spaceId, String contentId) {
        log.debug("throwIfContentNotExist({}, {})", spaceId, contentId);
        String containerName = getContainerName(spaceId);
        boolean exists = false;
        try {
            exists = swiftClient.objectExists(containerName, contentId);
        } catch (ContainerNotFoundException e) {
            log.debug("object does not exist: {}, {}", containerName, contentId);
            String errMsg = createNotFoundMsg(containerName, contentId);
            throw new NotFoundException(errMsg, e);
        }

        if(! exists) {
            log.debug("object does not exist: {}, {}", containerName, contentId);
            String errMsg = createNotFoundMsg(containerName, contentId);
            throw new NotFoundException(errMsg);
        }
        log.debug("object does exist: {}, {}", containerName, contentId);
    }

    protected boolean spaceExists(String spaceId) {
        String containerName = getContainerName(spaceId);
        return swiftClient.containerExists(containerName);
    }

    /**
     * {@inheritDoc}
     */
    public void createSpace(String spaceId) {
        log.debug("getCreateSpace(" + spaceId + ")");
        throwIfSpaceExists(spaceId);

        // Add space properties
        // Note: According to Rackspace support (ticket #13597) there are no
        // dates recorded for containers, so store our own created date
        Map<String, String> spaceProperties = new HashMap<String, String>();
        spaceProperties.put(PROPERTIES_SPACE_CREATED,
                DateUtil.convertToString(System.currentTimeMillis()));

        CreateContainerOptions createContainerOptions =
                CreateContainerOptions.Builder.withMetadata(spaceProperties);

        String containerName = getContainerName(spaceId);
        swiftClient.createContainer(containerName, createContainerOptions);
    }

    protected void doSetSpaceProperties(String spaceId,
                                        Map<String, String> spaceProperties) {
        log.debug("doSetSpaceProperties(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        // Ensure that space created date is included in the new properties
        String created = getCreationTimestamp(spaceId, spaceProperties);
        if (created != null) {
            spaceProperties.put(PROPERTIES_SPACE_CREATED, created);
        }

        String containerName = getContainerName(spaceId);
        swiftClient.setContainerMetadata(containerName, spaceProperties);
    }

    /**
     * {@inheritDoc}
     */
    public void removeSpace(String spaceId) {
        String containerName = getContainerName(spaceId);
        boolean successful = swiftClient.deleteContainerIfEmpty(containerName);
        if(!successful) {
            StringBuilder err = new StringBuilder(
                    "Could not delete " + getProviderName() + " container with name " +
                            containerName + " due to error: container not empty");
            throw new StorageException(err.toString(), NO_RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Map<String, String> getAllSpaceProperties(String spaceId) {
        log.debug("getAllSpaceProperties(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        String containerName = getContainerName(spaceId);
        ContainerMetadata containerMetadata = swiftClient.getContainerMetadata(containerName);
        Map<String, String> metadata = containerMetadata.getMetadata();

        Map<String, String> spaceProperties = new HashMap<>();
        spaceProperties.putAll(metadata);

        // Add count and size properties
        spaceProperties.put(PROPERTIES_SPACE_COUNT,
                String.valueOf(containerMetadata.getCount()));
        spaceProperties.put(PROPERTIES_SPACE_SIZE,
                String.valueOf(containerMetadata.getBytes()));

        return spaceProperties;
    }

    private String getCreationTimestamp(String spaceId,
                                        Map<String, String> spaceProperties) {
        String creationTime = null;
        if (!spaceProperties.containsKey(PROPERTIES_SPACE_CREATED)) {
            Map<String, String> spaceMd = getAllSpaceProperties(spaceId);
            creationTime = spaceMd.get(PROPERTIES_SPACE_CREATED);
        } else {
            creationTime = spaceProperties.get(PROPERTIES_SPACE_CREATED);
        }

        if (creationTime == null) {
            StringBuffer msg = new StringBuffer("Error: ");
            msg.append("No ").append(PROPERTIES_SPACE_CREATED).append(" found ");
            msg.append("for spaceId: ").append(spaceId);
            log.error(msg.toString());
            creationTime = ISO8601_DATE_FORMAT.format(new Date());
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
            userProperties = removeCalculatedProperties(userProperties);

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

        String containerName = getContainerName(spaceId);

        SwiftObject swiftObject = swiftClient.newSwiftObject();
        MutableObjectInfoWithMetadata objectInfoMetadata = swiftObject.getInfo();
        objectInfoMetadata.setName(contentId);
//        if(contentSize > 0) {   ****************  THIS BROKE THINGS ON SDSC  ********************
//            objectInfoMetadata.setBytes(contentSize);
//        }
        objectInfoMetadata.setContentType(contentMimeType);  // This doesn't seem to do anything, set in metadata!
        objectInfoMetadata.getMetadata().putAll(properties);
        swiftObject.setPayload(wrappedContent);

        String providerChecksum = swiftClient.putObject(containerName, swiftObject);

        // Compare checksum
        try {
            String checksum = wrappedContent.getMD5();
            compareChecksum(providerChecksum, spaceId, contentId, checksum);
        } catch(ChecksumMismatchException e) {
            // Clean up object
            if(swiftClient.objectExists(containerName, contentId)) {
                swiftClient.removeObject(containerName, contentId);
            }
            throw e;
        }

        return providerChecksum;
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

        if(doCopyContent(sourceSpaceId,
                sourceContentId,
                destSpaceId,
                destContentId)) {
            MutableObjectInfoWithMetadata objectInfoWithMetadata =
                    getObjectProperties(destSpaceId, destContentId);
            byte[] hash = objectInfoWithMetadata.getHash();
            String md5 = null;
            if(hash != null) {
                md5 = ChecksumUtil.checksumBytesToString(hash);
            }
            return StorageProviderUtil.compareChecksum(this,
                    sourceSpaceId,
                    sourceContentId,
                    md5);
        } else {
            throw new StorageException("failed to copy object - " +
                    "srcSpaceId: " +sourceSpaceId+ ", " +
                    "sourceContentId: " +sourceContentId+ ", " +
                    "destSpaceId: " +destSpaceId+ ", " +
                    "destContentId: " +destContentId);
        }
    }

    private boolean doCopyContent(String sourceSpaceId,
                                  String sourceContentId,
                                  String destSpaceId,
                                  String destContentId) {
        try {
            return swiftClient.copyObject(sourceSpaceId,
                    sourceContentId,
                    destSpaceId,
                    destContentId);

        } catch (CopyObjectException e) {
            StringBuilder err = new StringBuilder("Could not copy content from: ");
            err.append(sourceSpaceId);
            err.append(" / ");
            err.append(sourceContentId);
            err.append(", to: ");
            err.append(destSpaceId);
            err.append(" / ");
            err.append(destContentId);
            err.append(", due to error: ");
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
        SwiftObject swiftObject = swiftClient.getObject(containerName, contentId);
        if(swiftObject == null) {
            String errMsg = createNotFoundMsg(spaceId, contentId);
            throw new NotFoundException(errMsg);
        }
        InputStream content = swiftObject.getPayload().getInput();
        return content;

    }

    private String createNotFoundMsg(String spaceId,
                                     String contentId) {
        StringBuilder msg = new StringBuilder(getProviderName());
        msg.append(": Could not find content item with ID ");
        msg.append(contentId);
        msg.append(" in space ");
        msg.append(spaceId);
        return msg.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContent(String spaceId, String contentId) {
        log.debug("deleteContent({}, {})", spaceId, contentId);

        throwIfContentNotExist(spaceId, contentId);

        log.debug("after check exist: {}, {}", spaceId, contentId);

        String containerName = getContainerName(spaceId);
        log.debug("before swiftClient.removeObject({}, {})", spaceId, contentId);
        swiftClient.removeObject(containerName, contentId);
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
        contentProperties = removeCalculatedProperties(contentProperties);

        // Set mimetype
        String contentMimeType =
                contentProperties.remove(PROPERTIES_CONTENT_MIMETYPE);
        if(contentMimeType == null || contentMimeType.equals("")) {
            contentMimeType = getContentProperties(spaceId, contentId)
                    .get(PROPERTIES_CONTENT_MIMETYPE);
        }

        Map<String, String> newContentProperties = new HashMap<String, String>();
        for (String key : contentProperties.keySet()) {
            if (log.isDebugEnabled()) {
                log.debug("[" + key + "|" + contentProperties.get(key) + "]");
            }
            newContentProperties.put(getSpaceFree(key),
                    contentProperties.get(key));
        }

        // Set Content-Type
        if (contentMimeType != null && !contentMimeType.equals("")) {
            newContentProperties.put(PROPERTIES_CONTENT_MIMETYPE, contentMimeType);
        }

        String containerName = getContainerName(spaceId);
        log.debug("Calling swiftClient.setObjectInfo for spaceId: {} and contentId: {}",
                spaceId, contentId);
        if(! swiftClient.setObjectInfo(containerName,
                contentId,
                newContentProperties)) {
            String errMsg = createNotFoundMsg(spaceId, contentId);
            throw new StorageException("Error setting content properties");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId) {
        log.debug("getContentProperties(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);

        MutableObjectInfoWithMetadata objectInfoWithMetadata =
                getObjectProperties(spaceId, contentId);
        if (objectInfoWithMetadata == null) {
            String err = "No properties are available for item " + contentId +
                    " in " + getProviderName() + " space " + spaceId;
            throw new StorageException(err, RETRY);
        }

        Map<String, String> propertiesMap = objectInfoWithMetadata.getMetadata();

        // Set expected property values

        // MIMETYPE
        // PROPERTIES_CONTENT_MIMETYPE value is set directly by add/update content
        // SIZE
        Long contentLength = objectInfoWithMetadata.getBytes();
        if (contentLength != null) {
            propertiesMap.put(PROPERTIES_CONTENT_SIZE, contentLength.toString());
        }
        // CHECKSUM
        byte[] hash = objectInfoWithMetadata.getHash();
        if (hash != null) {
            String checksum = ChecksumUtil.checksumBytesToString(hash);
            propertiesMap.put(PROPERTIES_CONTENT_CHECKSUM, checksum);
        }
        // MODIFIED DATE
        Date modified = objectInfoWithMetadata.getLastModified();
        if (modified != null) {
            String formatted = DateUtil.convertToString(modified.getTime());
            propertiesMap.put(PROPERTIES_CONTENT_MODIFIED, formatted);
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

    private MutableObjectInfoWithMetadata getObjectProperties(String spaceId,
                                                              String contentId) {
        String containerName = getContainerName(spaceId);
        MutableObjectInfoWithMetadata objectInfoWithMetadata =
                swiftClient.getObjectInfo(containerName, contentId);

        if(objectInfoWithMetadata == null) {
            String errMsg = createNotFoundMsg(spaceId, contentId);
            throw new NotFoundException(errMsg);
        }

        return objectInfoWithMetadata;
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
        containerName = sanitizeForURI(containerName);

        if (containerName.length() > 63) {
            containerName = containerName.substring(0, 63);
        }

        return containerName;
    }

    /**
     * Encode any unicode characters that will cause us problems.
     *
     * @param str
     * @return The string encoded for a URI
     */
    public static String sanitizeForURI(String str) {
        URLCodec codec = new URLCodec();
        try {
            return codec.encode(str).replaceAll("\\+", "%20");
        } catch (EncoderException ee) {
            log.warn("Error trying to encode string for URI", ee);
            return str;
        }
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
