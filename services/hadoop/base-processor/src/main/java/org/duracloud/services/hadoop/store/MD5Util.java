/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class is a utility for getting an MD5 for a local file or from a file
 * stored in DuraCloud.
 *
 * @author Andrew Woods
 *         Date: Oct 14, 2010
 */
public class MD5Util {

    private final Logger log = LoggerFactory.getLogger(MD5Util.class);

    private ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

    /**
     * This method returns the MD5 of the arg file.
     *
     * @param file for which MD5 is sought
     * @return MD5 or error message
     */
    public String getMd5(File file) {
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);

        } catch (FileNotFoundException e) {
            log.warn(e.getMessage());
            return "file-not-found";
        }

        String md5 = checksumUtil.generateChecksum(fileStream);
        IOUtils.closeQuietly(fileStream);

        return md5;
    }

    /**
     * This method returns the MD5 of the arg content item.
     *
     * @param store     where content item is hosted
     * @param spaceId   of content item
     * @param contentId of content item
     * @return MD5 or error message
     */
    public String getMd5(ContentStore store, String spaceId, String contentId) {
        Map<String, String> metadata = null;
        try {
            metadata = store.getContentMetadata(spaceId, contentId);

        } catch (ContentStoreException e) {
            log.warn(e.getMessage());
            return "item-not-found";
        }

        String md5 = "item-metadata-not-found";
        if (null != metadata) {
            md5 = findMd5(metadata);
        }

        return md5;
    }

    private String findMd5(Map<String, String> metadata) {
        String md5 = "md5-not-found";

        if (null != metadata.get(StorageProvider.METADATA_CONTENT_CHECKSUM)) {
            md5 = metadata.get(StorageProvider.METADATA_CONTENT_CHECKSUM);

        } else if (null != metadata.get(StorageProvider.METADATA_CONTENT_MD5)) {
            md5 = metadata.get(StorageProvider.METADATA_CONTENT_MD5);
        }

        return md5;
    }
}
