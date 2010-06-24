/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.emcstorage;

import com.emc.esu.api.*;
import com.emc.esu.api.rest.EsuRestApi;
import org.apache.commons.lang.StringUtils;
import org.duracloud.common.stream.ChecksumInputStream;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.ContentIterator;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import static org.duracloud.storage.error.StorageException.NO_RETRY;
import static org.duracloud.storage.error.StorageException.RETRY;
import org.duracloud.storage.provider.StorageProviderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides content storage backed by EMC's Storage Utility.
 *
 * @author Andrew Woods
 */
public class EMCStorageProvider extends StorageProviderBase {

    private final Logger log = LoggerFactory.getLogger(EMCStorageProvider.class);

    private static final String SPACE_ROOT_TAG_NAME = "emc-space-root-tag";

    private static final String EMC_CONTENT_SIZE = "size";

    private static final String EMC_CONTENT_MODIFIED_DATE = "mtime";

    private static final String EMC_CREATION_DATE_NAME = "ctime";

    protected static final String ESU_HOST = "accesspoint.emccis.com";

    protected static final int ESU_PORT = 80;

    // All ATMOS_* codes are defined in the emc/esu javadocs
    private static final int ATMOS_NO_OBJECTS_FOUND = 1003;

    private EsuApi emcService = null;
    private final String uid;

    public EMCStorageProvider(String uid, String sharedSecret) {
        this.uid = uid;
        emcService = new EsuRestApi(ESU_HOST, ESU_PORT, uid, sharedSecret);
    }

    public EMCStorageProvider(EsuApi esuApi) {
        this.uid = "/probed-emc-uid";
        emcService = esuApi;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaces() {
        log.debug("getSpaces()");

        List<String> spaces = new ArrayList<String>();
        List<Identifier> spaceObjects = getSpaceObjects();
        for (Identifier objId : spaceObjects) {
            spaces.add(getSpaceNameForSpaceObject(objId));
        }

        if (log.isDebugEnabled()) {
            log.warn("Spaces found:");
            for (String space : spaces) {
                log.warn("\t-> " + space);
            }
        }
        return spaces.iterator();
    }

    private List<Identifier> getSpaceObjects() {
        try {
            return emcService.listObjects(spaceRootTag());

        } catch (EsuException e) {
            if (e.getAtmosCode() == ATMOS_NO_OBJECTS_FOUND) {
                return new ArrayList<Identifier>();

            } else {
                String err = "Unable to find any spaces: " + e.getMessage();
                throw new StorageException(err, e, RETRY);
            }
        }
    }

    private String getSpaceNameForSpaceObject(Identifier objId) {
        MetadataTags tags = new MetadataTags();
        tags.addTag(spaceRootTag());

        try {
            // There should only be one element in the filtered userMetadata.
            MetadataList userMetadata = emcService.getUserMetadata(objId, tags);
            return userMetadata.iterator().next().getValue();

        } catch (Exception e) {
            String err =
                "Unable to find spaceRootTag for space: " + e.getMessage();
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
     *
     * Note that EMC does not support chunked directory listings.
     * The first listing call (where marker is null) will include
     * the entire directory listing (which may be larger than
     * maxResults), and any followup call (marker is not null)
     * will be returned an empty list.
     * TODO: Inform EMC of their need to implement chunked directory listings
     */
    public List<String> getSpaceContentsChunked(String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker) {
        log.debug("getSpaceContentsChunked(" + spaceId + ", " + prefix + ", " +
                                           maxResults + ", " + marker + ")");

        throwIfSpaceNotExist(spaceId);

        List<String> contentNames = new ArrayList<String>();
        if(marker == null) {
            List<Identifier> spaceContents = getCompleteSpaceContents(spaceId);
            for (Identifier objId : spaceContents) {
                contentNames.add(getContentNameForContentObject(objId, spaceId));
            }
        }
        return contentNames;
    }

    private List<Identifier> getCompleteSpaceContents(String spaceId) {
        try {
            List<DirectoryEntry> entries = emcService.listDirectory(getSpacePath(
                spaceId));
            return getSpaceContentsIds(entries);

        } catch (EsuException e) {
            String err =
                "Unable to list objs with current space tag: " + spaceId +
                    ", due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    private List<Identifier> getSpaceContentsIds(List<DirectoryEntry> entries) {
        List<Identifier> contentIds = new ArrayList<Identifier>();
        for (DirectoryEntry entry : entries) {
            contentIds.add(entry.getId());
        }
        return contentIds;
    }

    private String getContentNameForContentObject(Identifier objId,
                                                  String spaceId) {
        MetadataTags tags = new MetadataTags();
        String name = null;
        MetadataList userMetadata = emcService.getUserMetadata(objId, tags);
        for (Metadata md : userMetadata) {
            if (spaceId.equals(md.getName())) {
                name = md.getValue();
                break;
            }
        }

        log.debug("content name found for objId: " + name + ", for: " + objId);
        return name;
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
        try {
            Identifier objectPath = getObjectPath(spaceId, contentId);
            emcService.getSystemMetadata(objectPath, null);
        } catch(EsuException e) {
            String err = "Could not find content [" + spaceId + ":" +
                         contentId + "], due to error: " + e.getMessage();
            throw new NotFoundException(err);
        }
    }

    private boolean spaceExists(String spaceId) {
        return objectExists(getSpacePath(spaceId));
    }

    private boolean objectExists(Identifier objId) {
        try {
            emcService.getAcl(objId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createSpace(String spaceId) {
        log.debug("createSpace(" + spaceId + ")");
        throwIfSpaceExists(spaceId);

        MetadataList metadataList = createRequiredRootMetadata(spaceId);
        Identifier objId = createSpaceObject(spaceId, metadataList);

        log.debug("\t...space created with id: " + objId);
    }

    private MetadataList createRequiredRootMetadata(String spaceId) {
        MetadataList metadataList = new MetadataList();
        metadataList.addMetadata(new Metadata(SPACE_ROOT_TAG_NAME,
                                              spaceId,
                                              true));
        metadataList.addMetadata(new Metadata(METADATA_SPACE_ACCESS,
                                              AccessType.CLOSED.name(),
                                              true));
        return metadataList;
    }

    private Identifier createSpaceObject(String spaceId,
                                         MetadataList metadataList) {
        Acl acl = null;
        byte[] data = null;
        String mimeType = null;

        ObjectPath spacePath = getSpacePath(spaceId);
        try {
            // This object only serves the purpose of representing the
            //  existence of a 'space' with id: spaceId.
            return emcService.createObjectOnPath(spacePath,
                                                 acl,
                                                 metadataList,
                                                 data,
                                                 mimeType);
        } catch (EsuException e) {
            String err =
                "Could not create EMC space with spacePath: '" + spacePath +
                    "', due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(String spaceId) {
        log.debug("Deleting space: " + spaceId);
        throwIfSpaceNotExist(spaceId);

        deleteSpaceContents(spaceId);
        deleteObject(getSpacePath(spaceId));
    }

    private void deleteSpaceContents(String spaceId) {
        List<Identifier> contentIds = getCompleteSpaceContents(spaceId);
        for (Identifier objId : contentIds) {
            deleteObject(objId);
        }
    }

    private void deleteObject(Identifier objId) {
        log.debug("deleteObject(" + objId + ")");

        try {
            emcService.deleteObject(objId);

        } catch (Exception e) {
            String err =
                "Unable to delete object: " + objId + ", due to error: " +
                    e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    protected Identifier getRootId(String spaceId) {
        Identifier rootId = null;
        List<Identifier> spaceObjects = getSpaceObjects();
        for (Identifier objId : spaceObjects) {
            if (spaceId.equals(getSpaceNameForSpaceObject(objId))) {
                rootId = objId;
                break;
            }
        }

        if (rootId == null) {
            String err = "ERROR: Unable to find rootId for space: " + spaceId;
            log.debug(err);
            throw new StorageException(err, RETRY);
        }

        log.debug(
            "Found rootId [" + rootId + "] for spaceId [" + spaceId + "]");
        return rootId;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getSpaceMetadata(String spaceId) {
        log.debug("getSpaceMetadata(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        Identifier spacePath = getSpacePath(spaceId);
        Map<String, String> spaceMetadata = getExistingUserMetadata(spacePath);

        // Over-write managed metadata.
        spaceMetadata.put(METADATA_SPACE_CREATED, getCreationDate(spacePath));
        spaceMetadata.put(METADATA_SPACE_COUNT, getContentObjCount(spaceId));

        return spaceMetadata;
    }

    private String getCreationDate(Identifier id) {
        String creationDate = "unknown-creation-date";

        MetadataList sysMetadata = getSystemMetadata(id);
        for (Metadata sysMd : sysMetadata) {
            if (EMC_CREATION_DATE_NAME.equals(sysMd.getName())) {
                creationDate = sysMd.getValue();
                break;
            }
        }
        return creationDate;
    }

    private String getContentObjCount(String spaceId) {
        List<Identifier> contentIds = new ArrayList<Identifier>();
        try {
            contentIds = getCompleteSpaceContents(spaceId);
        } catch (Exception e) {
            log.info("Obj-count not found: " + spaceId + ", " + e.getMessage());
        }
        return String.valueOf(contentIds.size());
    }

    private Acl getAcl(Identifier objId) {
        try {
            return emcService.getAcl(objId);
        } catch (Exception e) {
            String err = "Error finding Acl: " + objId + ", " + e.getMessage();
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

        // Remove volatile metadata.
        Identifier spacePath = getSpacePath(spaceId);
        MetadataTags existingTags = listUserMetadataTags(spacePath);
        MetadataTags disposableTags = getSpaceTagsToRemove(existingTags);
        if (disposableTags.count() > 0) {
            deleteUserMetadata(spacePath, disposableTags);
        }

        // Start with required metadata.
        MetadataList metadatas = createRequiredRootMetadata(spaceId);

        // Do not overwrite space root tag
        spaceMetadata.remove(SPACE_ROOT_TAG_NAME);

        // Start adding arg user metadata.
        final boolean isIndexed = false;
        Set<String> keys = spaceMetadata.keySet();
        for (String key : keys) {
            String val = spaceMetadata.get(key);
            metadatas.addMetadata(new Metadata(key, val, isIndexed));
        }

        // The actual setting.
        setUserMetadata(spacePath, metadatas);
    }

    private MetadataTags listUserMetadataTags(Identifier objId) {
        try {
            return emcService.listUserMetadataTags(objId);
        } catch (Exception e) {
            String err = "Error listing user metadata for :" + objId + ", " +
                e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    private MetadataTags getSpaceTagsToRemove(MetadataTags existingTags) {
        MetadataTags tags = new MetadataTags();
        for (MetadataTag tag : existingTags) {
            String tagName = tag.getName();
            if (!tagName.equals(SPACE_ROOT_TAG_NAME)) {
                tags.addTag(tag);
            }
        }
        return tags;
    }

    private void deleteUserMetadata(Identifier objId,
                                    MetadataTags disposableTags) {
        try {
            emcService.deleteUserMetadata(objId, disposableTags);
        } catch (Exception e) {
            String err = "Error deleting user metadata for :" + objId + ", " +
                e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    private void setUserMetadata(Identifier objId, MetadataList metadatas) {
        try {
            emcService.setUserMetadata(objId, metadatas);
        } catch (Exception e) {
            String err =
                "Error setting user metadata: " + objId + ", " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String addContent(String spaceId,
                             String contentId,
                             String mimeType,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) {
        try {
            return doAddContent(spaceId,
                                contentId,
                                mimeType,
                                contentSize,
                                contentChecksum,
                                content);
        } catch (NotFoundException e) {
            throw e;
        } catch (StorageException e) {
            throw new StorageException(e, NO_RETRY);
        }
    }

    private String doAddContent(String spaceId,
                                String contentId,
                                String mimeType,
                                long contentSize,
                                String contentChecksum,
                                InputStream content) {
        log.debug("addContent("+ spaceId +", "+ contentId +", "+
            mimeType +", "+ contentSize +", "+ contentChecksum +")");

        throwIfSpaceNotExist(spaceId);

        // Set access control to mirror the bucket
        Acl acl = getAcl(getSpacePath(spaceId));

        MetadataList metadataList = createRequiredContentMetadata(spaceId,
                                                                  contentId,
                                                                  mimeType);

        // Determine if object already exists.
        ObjectPath objectPath = getObjectPath(spaceId, contentId);

       // Wrap the content in order to be able to retrieve a checksum
        ChecksumInputStream wrappedContent =
            new ChecksumInputStream(content, contentChecksum);

        UploadHelper helper = new UploadHelper(emcService);
        boolean closeStream = true;

        // Update existing object.
        if (objectExists(objectPath)) {
            helper.updateObject(objectPath,
                                wrappedContent,
                                acl,
                                metadataList,
                                closeStream);
        }
        // Add new object.
        else {
            helper.createObjectOnPath(objectPath,
                                      wrappedContent,
                                      acl,
                                      metadataList,
                                      closeStream);
        }

        // Compare checksum
        String finalChecksum = wrappedContent.getMD5();
        return compareChecksum(this, spaceId, contentId, finalChecksum);
    }

    private MetadataList createRequiredContentMetadata(String spaceId,
                                                       String contentId,
                                                       String mimeType) {
        boolean isIndexed = true;
        MetadataList metadataList = new MetadataList();
        metadataList.addMetadata(new Metadata(spaceId, contentId, isIndexed));

        if (mimeType != null) {
            metadataList.addMetadata(new Metadata(METADATA_CONTENT_MIMETYPE,
                                                  mimeType,
                                                  isIndexed));
        } else {
            metadataList.addMetadata(new Metadata(METADATA_CONTENT_MIMETYPE,
                                                  DEFAULT_MIMETYPE,
                                                  isIndexed));
        }
        return metadataList;
    }

    protected ObjectPath getObjectPath(String spaceId, String contentId) {
        return new ObjectPath(getSpaceName(spaceId) + "/" + contentId);
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContent(String spaceId, String contentId) {
        log.debug("getContent(" + spaceId + ", " + contentId + ")");
        throwIfSpaceNotExist(spaceId);

        return doGetContent(spaceId,
                            contentId,
                            getObjectPath(spaceId, contentId));
    }

    private InputStream doGetContent(String spaceId,
                                     String contentId,
                                     Identifier contentObjId) {
        log.debug("doGetContent(" + contentObjId + ")");

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = null;
        boolean close = false;

        DownloadHelper helper = new DownloadHelper(emcService, buffer);
        try {
            helper.readObject(contentObjId, outStream, close);
        } catch(EsuException e) {
            throwIfContentNotExist(spaceId, contentId);
            String err = "Error retrieving content [" + spaceId + ":" +
                         contentId + "], due to error: " + e.getMessage();
            throw new StorageException(err, e, RETRY);
        }

        while (!helper.isComplete() && !helper.isFailed()) {
            log.debug("blocking...");
        }

        return new ByteArrayInputStream(outStream.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContent(String spaceId, String contentId) {
        log.debug("Deleting content: " + spaceId + ", " + contentId);
        throwIfSpaceNotExist(spaceId);

        try {
            emcService.deleteObject(getObjectPath(spaceId, contentId));
        } catch (Exception e) {
            throwIfContentNotExist(spaceId, contentId);
            String err =
                "Error deleting: [" + spaceId + ":" + contentId + "], " +
                    "due to error: " + e.getMessage();
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

        // Determine mimetype
        String contentMimeType =
            contentMetadata.remove(METADATA_CONTENT_MIMETYPE);
        if(contentMimeType == null || contentMimeType.equals("")) {
            Map<String, String> contentMeta =
                getContentMetadata(spaceId, contentId);
            contentMimeType = contentMeta.get(METADATA_CONTENT_MIMETYPE);
        }

        // Remove existing user metadata.
        Identifier objectPath = getObjectPath(spaceId, contentId);
        MetadataTags existingTags = listUserMetadataTags(objectPath);
        deleteUserMetadata(objectPath, existingTags);

        // Start with required metadata.
        MetadataList metadatas = createRequiredContentMetadata(spaceId,
                                                               contentId,
                                                               contentMimeType);

        // Start adding arg user metadata.
        final boolean isIndexed = false;
        Set<String> keys = contentMetadata.keySet();
        for (String key : keys) {
            String val = contentMetadata.get(key);
            metadatas.addMetadata(new Metadata(key, val, isIndexed));
        }

        try {
            setUserMetadata(objectPath, metadatas);
        } catch(StorageException e) {
            throwIfContentNotExist(spaceId, contentId);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentMetadata(String spaceId,
                                                  String contentId) {
        log.debug("getContentMetadata(" + spaceId + ", " + contentId + ")");

        throwIfSpaceNotExist(spaceId);
        throwIfContentNotExist(spaceId, contentId);

        Identifier objectPath = getObjectPath(spaceId, contentId);

        if (log.isDebugEnabled()) {
            for (Metadata md : emcService.getSystemMetadata(objectPath, null)) {
                log.debug("System-metadata: " + md.toString());
            }
            for (Metadata md : emcService.getUserMetadata(objectPath, null)) {
                log.debug("User-metadata:" + md.toString());
            }
        }

        Map<String, String> metadata = getExistingUserMetadata(objectPath);
        metadata.putAll(generateManagedContentMetadata(spaceId,
                                                       contentId,
                                                       objectPath));

        // Normalize metadata keys to lowercase.
        Map<String, String> resultMap = new HashMap<String, String>();
        Set<String> keys = metadata.keySet();
        for (String key : keys) {
            String val = metadata.get(key);
            resultMap.put(key.toLowerCase(), val);
        }

        return resultMap;
    }

    private Map<String, String> getExistingUserMetadata(Identifier objId) {
        Map<String, String> metadata = new HashMap<String, String>();
        MetadataList existingMetadata = null;
        try {
            existingMetadata = emcService.getUserMetadata(objId, null);
        } catch (Exception e) {
            log.warn("Unable to get userMetadata: " + e.getMessage());
        }

        if (existingMetadata != null) {
            for (Metadata md : existingMetadata) {
                metadata.put(md.getName(), md.getValue());
            }
        }
        return metadata;
    }

    private Map<String, String> generateManagedContentMetadata(String spaceId,
                                                               String contentId,
                                                               Identifier objId) {
        MetadataList sysMd = getSystemMetadata(objId);

        // Content size
        String size = null;
        Metadata foundSize = sysMd.getMetadata(EMC_CONTENT_SIZE);
        if (foundSize != null) {
            size = foundSize.getValue();
        }

        // Modified date
        String modifiedDate = null;
        Metadata foundDate = sysMd.getMetadata(EMC_CONTENT_MODIFIED_DATE);
        if (foundDate != null) {
            modifiedDate = foundDate.getValue();
        }

        // Checksum
        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String cksum =
            cksumUtil.generateChecksum(doGetContent(spaceId, contentId, objId));

        Map<String, String> metadata = new HashMap<String, String>();
        if (StringUtils.isNotBlank(size)) {
            metadata.put(METADATA_CONTENT_SIZE, size);
        }
        if (StringUtils.isNotBlank(cksum)) {
            metadata.put(METADATA_CONTENT_CHECKSUM, cksum);
        }
        if (StringUtils.isNotBlank(modifiedDate)) {
            metadata.put(METADATA_CONTENT_MODIFIED, modifiedDate);
        }
        return metadata;
    }

    private MetadataList getSystemMetadata(Identifier objId) {
        try {
            return emcService.getSystemMetadata(objId, null);
        } catch (Exception e) {
            String err = "Error getting system metadata for " + objId + ", " +
                "due to error: " + e.getMessage();
            throw new StorageException(err, RETRY);
        }
    }

    private MetadataTag spaceRootTag() {
        return new MetadataTag(SPACE_ROOT_TAG_NAME, true);
    }

    private ObjectPath getSpacePath(String spaceId) {
        String spaceName = getSpaceName(spaceId);
        return new ObjectPath(spaceName + "/");
    }

    private String getSpaceName(String spaceId) {
        int indexOfSep = uid.indexOf('/');
        int index = indexOfSep == -1 ? 0 : indexOfSep;
        String uniquePrefix = uid.substring(index, uid.length());

        String spaceName = uniquePrefix + "." + spaceId;
        spaceName = spaceName.toLowerCase();
        spaceName = spaceName.replaceAll("[^a-z0-9-./]", "-");

        // Remove duplicate separators (. and -)
        while (spaceName.contains("--") || spaceName.contains("..") ||
            spaceName.contains("-.") || spaceName.contains(".-")) {
            spaceName = spaceName.replaceAll("[-]+", "-");
            spaceName = spaceName.replaceAll("[.]+", ".");
            spaceName = spaceName.replaceAll("-[.]", "-");
            spaceName = spaceName.replaceAll("[.]-", ".");
        }

        if (spaceName.length() > 63) {
            spaceName = spaceName.substring(0, 63);
        }
        while (spaceName.endsWith("-") || spaceName.endsWith(".")) {
            spaceName = spaceName.substring(0, spaceName.length() - 1);
        }

        log.debug("spaceName: '" + spaceName + "'");
        return spaceName;
    }

}