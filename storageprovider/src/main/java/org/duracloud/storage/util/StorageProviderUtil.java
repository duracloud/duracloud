/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.util;

import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
     * @throws StorageException if the included checksum does not match
     *                          the storage provider generated checksum
     */
    public static String compareChecksum(String providerChecksum,
                                         String spaceId,
                                         String contentId,
                                         String checksum)
        throws StorageException {
        if(!providerChecksum.equals(checksum)) {
            String err = "Content " + contentId + " was added to space " +
                spaceId + " but the checksum, either provided or computed " +
                "enroute, (" + checksum + ") does not match the checksum " +
                "computed by the storage provider (" + providerChecksum +
                "). This content should be checked or retransmitted.";
            throw new StorageException(err, NO_RETRY);
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

}
