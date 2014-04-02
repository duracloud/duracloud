/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.IOUtil;
import org.duracloud.duradmin.domain.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Bernstein
 */
public class ContentItemUploadTask implements Comparable, ProgressListener {
    Logger log = LoggerFactory.getLogger(ContentItemUploadTask.class);

    private ContentItem contentItem;
    private ContentStore contentStore;
    private long totalBytes = 0;
    private long bytesRead = 0;

    private String username;
    private Date startDate = null;
    private InputStream stream = null;

    public ContentItemUploadTask(ContentItem contentItem,
                                 ContentStore contentStore,
                                 InputStream stream,
                                 String username) throws Exception {
        this.stream = stream;
        this.contentItem = contentItem;
        this.contentStore = contentStore;
        this.username = username;
        this.totalBytes = -1;
        log.info("new task created for {} by {}", contentItem, username);
    }

    public void execute() throws Exception {
        File tmpFile = null;
        InputStream tmpStream = null;
        try {
            log.info("executing file upload: {}", contentItem);
            startDate = new Date();

            tmpFile = IOUtil.writeStreamToFile(this.stream);
            tmpStream = IOUtil.getFileStream(tmpFile);

            contentStore.addContent(contentItem.getSpaceId(),
                                    contentItem.getContentId(),
                                    tmpStream,
                                    tmpFile.length(),
                                    contentItem.getContentMimetype(),
                                    null,
                                    null);
            log.info("file upload completed successfully: {}", contentItem);

        } catch (Exception ex) {
            log.error(
                "failed to upload content item: {}, bytesRead={}, totalBytes={},  message: {}",
                new Object[]{contentItem,
                             this.bytesRead,
                             this.totalBytes,
                             ex.getMessage()});

                ex.printStackTrace();
                throw ex;

        } finally {
            FileUtils.deleteQuietly(tmpFile);
            IOUtils.closeQuietly(tmpStream);
        }
    }

    public void update(long pBytesRead, long pContentLength, int pItems) {
        bytesRead = pBytesRead;
        totalBytes = pContentLength;
        log.debug("updating progress: bytesRead = {}, totalBytes = {}",
                  bytesRead,
                  totalBytes);
    }

    public String getId() {
        return this.contentItem.getStoreId() + "/" +
            this.contentItem.getSpaceId() + "/" +
            this.contentItem.getContentId();
    }


    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("bytesRead", String.valueOf(this.bytesRead));
        map.put("totalBytes", String.valueOf(this.totalBytes));
        map.put("contentId", this.contentItem.getContentId());
        map.put("spaceId", this.contentItem.getSpaceId());
        map.put("storeId", this.contentItem.getStoreId());
        return map;
    }


    public Date getStartDate() {
        return this.startDate;
    }

    @Override
    public int compareTo(Object o) {
        ContentItemUploadTask other = (ContentItemUploadTask) o;
        return this.getStartDate().compareTo(other.getStartDate());
    }

    public String getUsername() {
        return this.username;
    }

    public String toString() {
        return "{startDate: " + startDate + ", bytesRead: " + this.bytesRead +
            ", totalBytes: " + totalBytes + ", storeId: " +
            contentItem.getStoreId() + ", spaceId: " +
            contentItem.getSpaceId() + ", contentId: " +
            contentItem.getContentId() +
            "}";
    }

}
