/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.util;

import org.duracloud.common.util.DateUtil;
import org.duracloud.storage.error.ChecksumMismatchException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.duracloud.common.util.IOUtil.readStringFromStream;
import static org.duracloud.common.util.SerializationUtil.deserializeMap;
import static org.duracloud.common.util.SerializationUtil.serializeMap;
import static org.duracloud.storage.error.StorageException.NO_RETRY;

/**
 * Provides utility methods for Storage Providers
 *
 * @author Bill Branan
 */
public class StorageProviderUtil {
    
    private static Logger log = LoggerFactory.getLogger(StorageProviderUtil.class);

    /**
     * Loads a stream containing properties and populates a map
     * with the properties name/value pairs.
     *
     * @param is
     * @return
     * @throws StorageException
     */
    public static Map<String, String> loadProperties(InputStream is)
    throws StorageException {
        Map<String, String> propertiesMap = null;
        if(is != null) {
            try {
                String properties = readStringFromStream(is);
                propertiesMap = deserializeMap(properties);
            } catch(Exception e) {
                String err = "Could not read properties " +
                             " due to error: " + e.getMessage();
                throw new StorageException(err, e);
            }
        }

        if(propertiesMap == null) {
            propertiesMap = new HashMap<String, String>();
        }

        return propertiesMap;
    }

    /**
     * Converts properties stored in a Map into a stream for storage purposes.
     *
     * @param propertiesMap
     * @return
     * @throws StorageException
     */
    public static ByteArrayInputStream storeProperties(
        Map<String, String> propertiesMap)
    throws StorageException {
        // Pull out known computed values
        propertiesMap.remove(StorageProvider.PROPERTIES_SPACE_COUNT);

        // Serialize Map
        byte[] properties = null;
        try {
            String serializedProperties = serializeMap(propertiesMap);
            properties = serializedProperties.getBytes("UTF-8");
        } catch (Exception e) {
            String err = "Could not store properties" +
                         " due to error: " + e.getMessage();
            throw new StorageException(err);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(properties);
        return is;
    }

    /**
     * Determines if the checksum for a particular piece of content
     * stored in a StorageProvider matches the expected checksum value.
     *
     * @param provider The StorageProvider where the content was stored
     * @param spaceId The Space in which the content was stored
     * @param contentId The Id of the content
     * @param checksum The content checksum, either provided or computed
     * @returns the validated checksum value from the provider
     * @throws StorageException if the included checksum does not match
     *                          the storage provider generated checksum
     */
    public static String compareChecksum(StorageProvider provider,
                                         String spaceId,
                                         String contentId,
                                         String checksum)
        throws StorageException {
        String providerChecksum =
            provider.getContentProperties(spaceId, contentId).
            get(StorageProvider.PROPERTIES_CONTENT_CHECKSUM);
        return compareChecksum(providerChecksum, spaceId, contentId, checksum);
    }

    /**
     * Determines if two checksum values are equal
     *
     * @param providerChecksum The checksum provided by the StorageProvider
     * @param spaceId The Space in which the content was stored
     * @param contentId The Id of the content
     * @param checksum The content checksum, either provided or computed
     * @returns the validated checksum value from the provider
     * @throws ChecksumMismatchException if the included checksum does not match
     *                                   the storage provider generated checksum
     */
    public static String compareChecksum(String providerChecksum,
                                         String spaceId,
                                         String contentId,
                                         String checksum)
        throws ChecksumMismatchException {
        if(!providerChecksum.equals(checksum)) {
            String err = "Content " + contentId + " was added to space " +
                spaceId + " but the checksum, either provided or computed " +
                "enroute, (" + checksum + ") does not match the checksum " +
                "computed by the storage provider (" + providerChecksum +
                "). This content has been removed, and should be checked and " +
                "retransmitted.";
            throw new ChecksumMismatchException(err, NO_RETRY);
        }
        return providerChecksum;
    }

    /**
     * Determines if a String value is included in a Iterated list.
     * The iteration is only run as far as necessary to determine
     * if the value is included in the underlying list.
     *
     * @param iterator
     * @param value
     * @return
     */
    public static boolean contains(Iterator<String> iterator, String value) {
        if(iterator == null || value == null) {
            return false;
        }
        while(iterator.hasNext()) {
            if(value.equals(iterator.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the number of elements in an iteration.
     *
     * @param iterator
     * @return
     */
    public static long count(Iterator<String> iterator) {
        if(iterator == null) {
            return 0;
        }
        long count = 0;
        while(iterator.hasNext()) {
            ++count;
            iterator.next();
        }
        return count;
    }

    /**
     * Creates a list of all of the items in an iteration.
     * Be wary of using this for Iterations of very long lists.
     *
     * @param iterator
     * @return
     */
    public static List<String> getList(Iterator<String> iterator) {
        List<String> contents = new ArrayList<String>();
        while(iterator.hasNext()) {
            contents.add(iterator.next());
        }
        return contents;
    }
    
    /**
     * Generates a map of all client-side default content properties to be
     * added with new content.
     * 
     * @param absolutePath
     * @param creator
     * @return
     */
    public static Map<String,String> createContentProperties(String absolutePath,
                                                             String creator){

        Map<String, String> props = new HashMap<String, String>();
        if(creator != null && creator.trim().length() > 0){
            props.put(StorageProvider.PROPERTIES_CONTENT_CREATOR, creator);
        }

        props.put(StorageProvider.PROPERTIES_CONTENT_FILE_PATH, absolutePath);
        
        try{
            Path path = FileSystems.getDefault().getPath(absolutePath);
            
            BasicFileAttributes bfa =
                Files.readAttributes(path, BasicFileAttributes.class);
            
            String creationDate =
                DateUtil.convertToStringLong(bfa.creationTime().toMillis());
            props.put(StorageProvider.PROPERTIES_CONTENT_FILE_CREATED,
                      creationDate);

            String lastAccessed =
                DateUtil.convertToStringLong(bfa.lastAccessTime().toMillis());
            props.put(StorageProvider.PROPERTIES_CONTENT_FILE_LAST_ACCESSED,
                      lastAccessed);

            String modified =
                DateUtil.convertToStringLong(bfa.lastModifiedTime().toMillis());
            props.put(StorageProvider.PROPERTIES_CONTENT_FILE_MODIFIED,
                      modified);

        }catch(IOException ex){
            log.error("Failed to read basic file attributes from "
                             + absolutePath + ": " + ex.getMessage(), 
                         ex);
        }        
        
        return props; 
    }
    
    /**
     * Returns a new map with the calculated properties removed. If null, null
     * is returned.
     * 
     * @param contentProperties
     * @return
     */
    public static Map<String, String>
        removeCalculatedProperties(Map<String, String> contentProperties) {
        if (contentProperties != null) {
            contentProperties = new HashMap<>(contentProperties);
            // Remove calculated properties
            contentProperties.remove(StorageProvider.PROPERTIES_CONTENT_MD5);
            contentProperties.remove(StorageProvider.PROPERTIES_CONTENT_CHECKSUM);
            contentProperties.remove(StorageProvider.PROPERTIES_CONTENT_MODIFIED);
            contentProperties.remove(StorageProvider.PROPERTIES_CONTENT_SIZE);
        }
        return contentProperties;

    }
}
