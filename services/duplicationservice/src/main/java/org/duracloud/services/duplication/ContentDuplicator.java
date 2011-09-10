/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.util.Map;

/**
 * Performs content replication activities
 *
 * @author Kristen Cannava
 */
public class ContentDuplicator {

    private static final Logger log =
        LoggerFactory.getLogger(ContentDuplicator.class);

    private ContentStore fromStore;
    private ContentStore toStore;

    public ContentDuplicator(ContentStore fromStore,
                             ContentStore toStore) {
        this.fromStore = fromStore;
        this.toStore = toStore;
    }

    public void createContent(String spaceId, String contentId) {
        logDebug("Creating", spaceId, contentId);

        if(spaceId == null || contentId == null)
            return;

        Content content = getContent(spaceId, contentId);
        if(content == null) return;

        InputStream contentStream = content.getStream();
        if(contentStream == null) {
            log.error("The content stream retrieved from the store was null.");
            return;
        }

        Map<String, String> properties = content.getProperties();

        String mimeType = "application/octet-stream";
        long contentSize = 0;
        String checksum = null;

        if(properties != null && !properties.isEmpty()) {
            mimeType = properties.get(ContentStore.CONTENT_MIMETYPE);

            contentSize =
                getContentSize(properties.get(ContentStore.CONTENT_SIZE),
                                              contentStream);

            checksum = properties.get(ContentStore.CONTENT_CHECKSUM);
        } else {
            DigestInputStream stream =
                ChecksumUtil.wrapStream(contentStream,
                                        ChecksumUtil.Algorithm.MD5);
            File tmpFile = cacheStreamToFile(stream);
            contentSize = tmpFile.length();

            checksum = ChecksumUtil.getChecksum(stream);
        }

        addContent(spaceId,
                   contentId,
                   contentStream,
                   contentSize,
                   mimeType,
                   checksum,
                   properties);
    }

    private void addContent(String spaceId,
                            String contentId,
                            InputStream contentStream,
                            long contentSize,
                            String mimeType,
                            String checksum,
                            Map<String, String> properties) {
        try {
            toStore.addContent(spaceId,
                               contentId,
                               contentStream,
                               contentSize,
                               mimeType,
                               checksum,
                               properties);
        } catch(NotFoundException nfe) {
            String error = "Unable to create content " + contentId + " in space " +
                           spaceId + " due to not found error: " + nfe.getMessage();
            log.debug(error, nfe);

            createSpace(spaceId);

            doAddContent(spaceId,
                         contentId,
                         contentSize,
                         mimeType,
                         checksum,
                         properties);
        } catch(ContentStoreException ce) {
            String error = "Unable to replicate content " + contentId + " in space " +
                           spaceId + " due to error: " + ce.getMessage();
            log.warn(error, ce);

            doAddContent(spaceId,
                         contentId,
                         contentSize,
                         mimeType,
                         checksum,
                         properties);
        }
    }

    private void doAddContent(final String spaceId,
                              final String contentId,
                              final long contentSize,
                              final String mimeType,
                              final String checksum,
                              final Map<String, String> properties) {
        new RetryDuplicate() {
            protected void doReplicate() throws Exception {
                Content content = getContent(spaceId, contentId);

                toStore.addContent(spaceId,
                                   contentId,
                                   content.getStream(),
                                   contentSize,
                                   mimeType,
                                   checksum,
                                   properties);
            }
        }.replicate();
    }

    public void updateContent(String spaceId, String contentId) {
        logDebug("Updating", spaceId, contentId);

        if(spaceId == null || contentId == null)
            return;

        Map<String, String> contentMeta = null;

        try {
            // Get Content properties
            contentMeta = fromStore.getContentProperties(
                spaceId,
                contentId);
        } catch (ContentStoreException cse) {
            String error = "Unable to get content " + contentId +
                           " properties in space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);
            return;
        }

        try {
            // Set Content properties
            toStore.setContentProperties(spaceId, contentId, contentMeta);
        } catch (NotFoundException nfe) {
            String error = "Unable to update content properties " + contentId +
                           " in space " + spaceId + " due to not found error: " +
                           nfe.getMessage();
            log.debug(error, nfe);

            createContent(spaceId, contentId);
        } catch (ContentStoreException cse) {
            String error = "Unable to update content " + contentId +
                           " in space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.warn(error, cse);

            setProperties(spaceId, contentId, contentMeta);
        }
    }

    private void setProperties(final String spaceId,
                               final String contentId,
                               final Map<String, String> properties) {
        new RetryDuplicate() {
            protected void doReplicate() throws Exception {
                toStore.setContentProperties(spaceId, contentId, properties);
            }
        }.replicate();
    }


    public void deleteContent(String spaceId, String contentId) {
        logDebug("Deleting", spaceId, contentId);

        if(spaceId == null || contentId == null)
            return;

        try {
            toStore.deleteContent(spaceId, contentId);
        } catch (ContentStoreException cse) {
            String error = "Unable to delete content " + contentId + " from space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);

            doDeleteContent(spaceId, contentId);
        }
    }

    private void doDeleteContent(final String spaceId,
                                 final String contentId) {
        new RetryDuplicate() {
            protected void doReplicate() throws Exception {
                toStore.deleteContent(spaceId, contentId);
            }
        }.replicate();
    }

    private Content getContent(String spaceId, String contentId) {
        Content content = null;

        try {
            content = fromStore.getContent(spaceId, contentId);
        } catch(ContentStoreException cse) {
            String error = "Error retrieving content " + contentId + " in space " +
                           spaceId + " from " + fromStore.getStorageProviderType();
            log.error(error, cse);
        }

        return content;
    }

    private long getContentSize(String size, InputStream inputStream) {
        if(size != null) {
            try {
                return Long.valueOf(size);
            } catch(NumberFormatException nfe) {
                log.warn("Could not convert stream size header " +
                         "value '" + size + "' to a number");
            }
        }

        return getContentSizeFromFile(inputStream);
    }

    private long getContentSizeFromFile(InputStream inputStream) {
        File tmpFile = cacheStreamToFile(inputStream);
        return tmpFile.length();
    }

    private File cacheStreamToFile(InputStream inputStream) {
        File file = null;
        OutputStream outStream = null;
        try {
            file = File.createTempFile("content-create", ".tmp");
            outStream = FileUtils.openOutputStream(file);
            IOUtils.copy(inputStream, outStream);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException("Error caching stream.", e);
        } finally {
            closeStream(outStream);
        }
        return file;
    }

    private void closeStream(OutputStream outStream) {
        try{
            if(outStream != null)
                outStream.close();
        } catch (IOException ioe) {
            log.error("Error closing stream", ioe);
        }
    }

    private void createSpace(String spaceId) {
        try {
            Map<String, String> spaceMeta =
                fromStore.getSpaceProperties(spaceId);
            toStore.createSpace(spaceId, spaceMeta);
        }
        catch(ContentStoreException ce) {
            String error = "Unable to create space " +
                       spaceId + " due to error: " + ce.getMessage();
            log.error(error, ce);
        }
    }

    private void logDebug(String action, String spaceId, String contentId) {
        if(log.isDebugEnabled()) {
            log.debug(action + " content " + contentId + " in space " + spaceId +
                      " from " + fromStore.getStorageProviderType() + " to " +
                      toStore.getStorageProviderType());
        }
    }
}