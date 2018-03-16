/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 * Date: 6/8/11
 */
public class ContentStoreUtil {
    private final Logger log = LoggerFactory.getLogger(ContentStoreUtil.class);

    private ContentStore contentStore;

    public ContentStoreUtil(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    public InputStream getContentStream(String spaceId, String contentId) {
        Content content = getContent(spaceId, contentId);
        if (null == content) {
            StringBuilder sb = new StringBuilder("Error: content is null: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(contentId);
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString());
        }
        return new AutoCloseInputStream(content.getStream());
    }

    private Content getContent(String spaceId, String contentId) {
        try {
            return contentStore.getContent(spaceId, contentId);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error: ");
            sb.append("getting content: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(contentId);
            sb.append(": ");
            sb.append(e.getMessage());
            log.error(sb.toString());

            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

    public void deleteOldContent(String spaceId, String contentId) {
        try {
            contentStore.deleteContent(spaceId, contentId);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error: ");
            sb.append("deleting content: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(contentId);
            sb.append(": ");
            sb.append(e.getMessage());
            log.error(sb.toString());
        }
    }

    public void storeContentStream(File file,
                                   String spaceId,
                                   String contentId,
                                   String contentMimetype) {
        log.debug("storing content to storage-provider: " + file.getPath());
        try {
            contentStore.addContent(spaceId,
                                    contentId,
                                    new FileInputStream(file),
                                    file.length(),
                                    contentMimetype,
                                    null,
                                    null);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error adding content: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(contentId);
            sb.append(", from: ");
            sb.append(file.getPath());
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);

        } catch (FileNotFoundException e) {
            StringBuilder sb = new StringBuilder("Error finding file from: ");
            sb.append(", from: ");
            sb.append(file.getPath());
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

    public void storeContentStream(File file,
                                   String spaceId,
                                   String contentId) {
        storeContentStream(file, spaceId, contentId, null);
    }
}
