/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.services.duplication.ContentDuplicator;
import org.duracloud.services.duplication.SpaceDuplicator;
import org.duracloud.services.duplication.StoreCaller;
import org.duracloud.services.duplication.error.DuplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.util.Map;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

/**
 * Performs content replication activities
 *
 * @author Kristen Cannava
 */
public class ContentDuplicatorImpl implements ContentDuplicator {

    private static final Logger log = LoggerFactory.getLogger(
        ContentDuplicatorImpl.class);

    private ContentStore fromStore;
    private ContentStore toStore;
    private SpaceDuplicator spaceDuplicator;

    private FileCleaningTracker fileReaper;
    private final int waitMillis;

    public ContentDuplicatorImpl(ContentStore fromStore,
                                 ContentStore toStore,
                                 SpaceDuplicator spaceDuplicator) {
        this(fromStore, toStore, spaceDuplicator, 1000);
    }

    public ContentDuplicatorImpl(ContentStore fromStore,
                                 ContentStore toStore,
                                 SpaceDuplicator spaceDuplicator,
                                 int waitMillis) {
        this.fromStore = fromStore;
        this.toStore = toStore;
        this.waitMillis = waitMillis;
        this.spaceDuplicator = spaceDuplicator;
        this.fileReaper = new FileCleaningTracker();
    }

    @Override
    public String getFromStoreId() {
        return fromStore.getStoreId();
    }

    @Override
    public String getToStoreId() {
        return toStore.getStoreId();
    }

    @Override
    public String createContent(String spaceId, String contentId) {
        logDebug("Creating", spaceId, contentId);

        if (null == spaceId || null == contentId) {
            String err = "Space or content to create is null: {}, {}.";
            log.warn(err, spaceId, contentId);
            return null;
        }

        Content content = getContent(spaceId, contentId);
        if (null == content) {
            StringBuilder err = new StringBuilder();
            err.append("Unable to get content: ");
            err.append(spaceId);
            err.append("/");
            err.append(contentId);
            err.append(" from ");
            err.append(fromStore.getStorageProviderType());
            log.error(err.toString());
            throw new DuplicationException(err.toString());
        }

        InputStream contentStream = content.getStream();
        if (contentStream == null) {
            StringBuilder err = new StringBuilder();
            err.append("Content stream is null: ");
            err.append(spaceId);
            err.append("/");
            err.append(contentId);
            err.append(" from ");
            err.append(fromStore.getStorageProviderType());
            log.error(err.toString());
            return null;
        }

        String mimeType = null;
        String checksum = null;
        long contentSize = -1;

        Map<String, String> properties = content.getProperties();
        if (null != properties && !properties.isEmpty()) {
            mimeType = properties.get(ContentStore.CONTENT_MIMETYPE);
            checksum = properties.get(ContentStore.CONTENT_CHECKSUM);
            String size = properties.get(ContentStore.CONTENT_SIZE);

            try {
                contentSize = Long.valueOf(size);
            } catch (NumberFormatException nfe) {
                // do nothing.
            }
        }

        // It is not expected that this situation arises often.
        if (-1 == contentSize || null == checksum) {
            log.info("Size or checksum are null: {}, {}. For {}/{} in {}",
                     new Object[]{contentSize,
                                  checksum,
                                  spaceId,
                                  contentId,
                                  fromStore.getStorageProviderType()});

            DigestInputStream dis = ChecksumUtil.wrapStream(contentStream, MD5);
            File tmpFile = cacheStreamToFile(dis);

            contentSize = tmpFile.length();
            checksum = ChecksumUtil.getChecksum(dis);

            // Swap out content-stream since it is now at EOF.
            contentStream = openInputStream(tmpFile);
            fileReaper.track(tmpFile, contentStream);
        }

        return addContent(spaceId,
                          contentId,
                          contentStream,
                          contentSize,
                          mimeType,
                          checksum,
                          properties);
    }

    private String addContent(String spaceId,
                              String contentId,
                              InputStream contentStream,
                              long contentSize,
                              String mimeType,
                              String checksum,
                              Map<String, String> properties) {
        String md5 = null;
        try {
            md5 = toStore.addContent(spaceId,
                                     contentId,
                                     contentStream,
                                     contentSize,
                                     mimeType,
                                     checksum,
                                     properties);

        } catch (NotFoundException nfe) {
            log.info("Unable to create content {}/{} in {}, due to: {}",
                     new Object[]{spaceId,
                                  contentId,
                                  toStore.getStorageProviderType(),
                                  nfe.getMessage()});

            log.info("Attempting to create space ({}) for content ({})",
                     spaceId,
                     contentId);

            spaceDuplicator.createSpace(spaceId);

        } catch (Exception e) {
            log.warn("Unable to replicate {}/{} to {}, due to: {}",
                     new Object[]{spaceId,
                                  contentId,
                                  toStore.getStorageProviderType(),
                                  e.getMessage()});
        }

        // Try again if it did not succeed.
        if (null == md5) {
            md5 = doAddContent(spaceId,
                               contentId,
                               contentSize,
                               mimeType,
                               checksum,
                               properties);
        }
        return md5;
    }

    private String doAddContent(final String spaceId,
                                final String contentId,
                                final long contentSize,
                                final String mimeType,
                                final String checksum,
                                final Map<String, String> properties) {
        try {
            return new StoreCaller<String>(waitMillis) {
                protected String doCall() throws Exception {
                    Content content = getContent(spaceId, contentId);
                    return toStore.addContent(spaceId,
                                              contentId,
                                              content.getStream(),
                                              contentSize,
                                              mimeType,
                                              checksum,
                                              properties);
                }
            }.call();

        } catch (Exception e) {
            StringBuilder err = new StringBuilder();
            err.append("Error adding content: ");
            err.append(spaceId);
            err.append("/");
            err.append(contentId);
            err.append(", to ");
            err.append(toStore.getStorageProviderType());

            log.error(err.toString() + ", due to {}", e.getMessage());
            throw new DuplicationException(err.toString());
        }
    }

    @Override
    public void updateContent(String spaceId, String contentId) {
        logDebug("Updating", spaceId, contentId);

        if (null == spaceId || null == contentId) {
            String err = "Space or content to update is null: {}, {}.";
            log.warn(err, spaceId, contentId);
            return;
        }

        Map<String, String> props = getContentProperties(spaceId, contentId);
        if (null == props) {
            StringBuilder err = new StringBuilder();
            err.append("Unable to get content properties for: ");
            err.append(spaceId);
            err.append("/");
            err.append(contentId);
            err.append(" from ");
            err.append(fromStore.getStorageProviderType());
            log.error(err.toString());
            throw new DuplicationException(err.toString());
        }

        boolean success = false;
        try {
            toStore.setContentProperties(spaceId, contentId, props);
            success = true;

        } catch (NotFoundException nfe) {
            String err = "Unable to update content props for {}/{}, due to: {}";
            log.warn(err, new Object[]{spaceId, contentId, nfe.getMessage()});

            // Try to create the content since it was not found.
            log.info("Trying to add content {}/{}", spaceId, contentId);
            createContent(spaceId, contentId);

        } catch (ContentStoreException cse) {
            String err = "Error updating content props for {}/{}, due to: {}";
            log.warn(err, new Object[]{spaceId, contentId, cse.getMessage()});

            // Try setting the properties again.
            log.info("Trying to readd content props {}/{}", spaceId, contentId);
            success = setProperties(spaceId, contentId, props);
        }

        if (!success) {
            StringBuilder err = new StringBuilder();
            err.append("Error updating content: ");
            err.append(spaceId);
            err.append("/");
            err.append(contentId);
            err.append(", to ");
            err.append(toStore.getStorageProviderType());
            throw new DuplicationException(err.toString());
        }
    }

    private Map<String, String> getContentProperties(final String spaceId,
                                                     final String contentId) {
        try {
            return new StoreCaller<Map<String, String>>(waitMillis) {
                protected Map<String, String> doCall() throws Exception {
                    return fromStore.getContentProperties(spaceId, contentId);
                }
            }.call();

        } catch (Exception e) {
            String err = "Error getting content props for: {}/{}, due to: {}";
            log.error(err, new Object[]{spaceId, contentId, e.getMessage()});
            return null;
        }
    }

    private boolean setProperties(final String spaceId,
                                  final String contentId,
                                  final Map<String, String> properties) {
        try {
            return new StoreCaller<Boolean>(waitMillis) {
                protected Boolean doCall() throws Exception {
                    toStore.setContentProperties(spaceId,
                                                 contentId,
                                                 properties);
                    return true;
                }
            }.call();

        } catch (Exception e) {
            String err = "Error setting content props for: {}/{}, due to: {}";
            log.error(err, new Object[]{spaceId, contentId, e.getMessage()});
            return false;
        }
    }

    @Override
    public void deleteContent(String spaceId, String contentId) {
        logDebug("Deleting", spaceId, contentId);

        if (null == spaceId || null == contentId) {
            String err = "Space or content to delete is null: {}, {}.";
            log.warn(err, spaceId, contentId);
            return;
        }

        boolean success = doDeleteContent(spaceId, contentId);
        if (!success) {
            StringBuilder err = new StringBuilder();
            err.append("Unable to delete ");
            err.append(spaceId);
            err.append("/");
            err.append(contentId);
            err.append(" from ");
            err.append(toStore.getStorageProviderType());
            log.error(err.toString());
            throw new DuplicationException(err.toString());
        }
    }

    private boolean doDeleteContent(final String spaceId,
                                    final String contentId) {
        try {
            return new StoreCaller<Boolean>(waitMillis) {
                protected Boolean doCall() throws Exception {
                    toStore.deleteContent(spaceId, contentId);
                    return true;
                }
            }.call();

        } catch (Exception e) {
            log.error("Error deleting {}/{} from {}, due to: {}",
                      new Object[]{spaceId,
                                   contentId,
                                   toStore.getStorageProviderType(),
                                   e.getMessage()});
            return false;
        }
    }

    @Override
    public void stop() {
        fileReaper.exitWhenFinished();
    }

    private Content getContent(final String spaceId, final String contentId) {
        try {
            return new StoreCaller<Content>(waitMillis) {
                protected Content doCall() throws Exception {
                    return fromStore.getContent(spaceId, contentId);
                }
            }.call();

        } catch (Exception e) {
            log.error("Error retrieving {}/{} from {}, due to: {}",
                      new Object[]{contentId,
                                   spaceId,
                                   fromStore.getStorageProviderType(),
                                   e.getMessage()});
            return null;
        }
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
            IOUtils.closeQuietly(outStream);
            IOUtils.closeQuietly(inputStream);
        }
        return file;
    }

    private FileInputStream openInputStream(File file) {
        try {
            return FileUtils.openInputStream(file);

        } catch (IOException e) {
            String err = "Unable to open file: " + file.getAbsolutePath();
            log.error(err + ", due to: {}", e.getMessage());
            throw new DuraCloudRuntimeException(err, e);
        }
    }

    private void logDebug(String action, String spaceId, String contentId) {
        log.debug("{} content {} in space {} from {} to {}",
                  new Object[]{action,
                               contentId,
                               spaceId,
                               fromStore.getStorageProviderType(),
                               toStore.getStorageProviderType()});
    }
}