/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.MimetypeUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Endpoint which pushes files to DuraCloud.
 *
 * @author: Bill Branan
 * Date: Mar 17, 2010
 */
public class DuraStoreSyncEndpoint implements SyncEndpoint {

    private final Logger logger =
        LoggerFactory.getLogger(DuraStoreSyncEndpoint.class);

    private ContentStore contentStore;
    private String spaceId;
    private boolean syncDeletes;
    private MimetypeUtil mimeUtil;

    public DuraStoreSyncEndpoint(String host,
                                 int port,
                                 String context,
                                 String username,
                                 String password,
                                 String spaceId,
                                 boolean syncDeletes) {
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(host, String.valueOf(port), context);
        storeManager.login(new Credential(username, password));

        try {
            contentStore = storeManager.getPrimaryContentStore();
        } catch(ContentStoreException e) {
            throw new RuntimeException("Could not create connection to " +
                "DuraStore due to " + e.getMessage(), e);
        }

        mimeUtil = new MimetypeUtil();

        this.spaceId = spaceId;
        this.syncDeletes = syncDeletes;
        ensureSpaceExists();
    }

    private void ensureSpaceExists() {
        boolean spaceExists = false;
        for(int i=0; i<10; i++) {
            if(spaceExists()) {
                spaceExists = true;
                break;
            }
            sleep(300);
        }
        if(!spaceExists) {
            throw new RuntimeException("Could not connect to space with ID '" +
                spaceId + "'.");
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {            
        }
    }

    private boolean spaceExists() {
        try {
            try {
                Iterator<String> contents =
                    contentStore.getSpaceContents(spaceId);
                if(contents.hasNext()) {
                    logger.warn("The specified space '" + spaceId +
                        "' is not empty. If this space is being used for an " +
                        "activity other than sync there is the possibility " +
                        "of data loss.");
                }
                return true;
            } catch (NotFoundException e) {
                contentStore.createSpace(spaceId,
                                         new HashMap<String, String>());
                return false;
            }
        } catch (ContentStoreException e) {
            throw new RuntimeException("Could not connect to space with ID '" +
                spaceId + "' due to error: " + e.getMessage(), e);
        }
    }

    public boolean syncFile(File syncFile, File watchDir) {
        String contentId = getContentId(syncFile, watchDir);
        logger.info("Syncing file " + syncFile.getAbsolutePath() +
                    " to DuraCloud with ID " + contentId);

        try {
            Map<String, String> contentMetadata = null;
            boolean dcFileExists = false;
            try {
                contentMetadata =
                     contentStore.getContentMetadata(spaceId, contentId);
                if(contentMetadata != null) {
                    dcFileExists = true;
                }
            } catch(NotFoundException e) {
                dcFileExists = false;
            }

            if(syncFile.exists()) {
                String localChecksum = computeChecksum(syncFile);

                if(dcFileExists) { // File was updated
                    String dcChecksum =
                        contentMetadata.get(ContentStore.CONTENT_CHECKSUM);
                    if(dcChecksum.equals(localChecksum)) {
                        logger.debug("Checksum for local file {} matches " +
                            "file in DuraCloud, no update needed.",
                            syncFile.getAbsolutePath());
                    } else {
                        logger.debug("Local file {} changed, updating DuraCloud.",
                                     syncFile.getAbsolutePath());
                        addUpdateContent(contentId, localChecksum, syncFile);
                    }
                } else { // File was added
                    logger.debug("Local file {} added, moving to DuraCloud.",
                                 syncFile.getAbsolutePath());
                    addUpdateContent(contentId, localChecksum, syncFile);
                }
            } else { // File was deleted
                if(dcFileExists) {
                    if(syncDeletes) {
                        logger.debug("Local file {} deleted, " +
                                     "removing from DuraCloud.",
                                     syncFile.getAbsolutePath());
                        contentStore.deleteContent(spaceId, contentId);
                    } else {
                        logger.debug("Ignoring delete of file {}",
                                     syncFile.getAbsolutePath());
                    }
                }
            }
        } catch(ContentStoreException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    protected void addUpdateContent(String contentId,
                                    String contentCheckdum,
                                    File syncFile)
        throws ContentStoreException {
        InputStream fileStream;
        try {
            fileStream = new FileInputStream(syncFile);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("Could not get stream for " +
                "file: " + syncFile.getAbsolutePath() + " due to " +
                e.getMessage(), e);
        }

        String mimetype = mimeUtil.getMimeType(syncFile);

        try {
            contentStore.addContent(spaceId,
                                    contentId,
                                    fileStream,
                                    syncFile.length(),
                                    mimetype,
                                    contentCheckdum,
                                    null);
        } finally {
            try {
                fileStream.close();
            } catch(IOException e) {
                logger.error("Error attempting to close stream for file " +
                    syncFile.getAbsolutePath() + ": " + e.getMessage(), e);
            }
        }
    }

    /*
     * Determines the content ID of a file: the path of the file relative to
     * the watched directory
     */
    private String getContentId(File syncFile, File watchDir) {
        URI relativeFileURI = watchDir.toURI().relativize(syncFile.toURI());
        return relativeFileURI.getPath();
    }

    /*
     * Computes the checksum of a local file
     */
    private String computeChecksum(File file) {
        try {
        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        return cksumUtil.generateChecksum(file);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("File not found: " +
                file.getAbsolutePath(), e);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<String> getFilesList() {
        Iterator<String> spaceContents;
        try {
            spaceContents = contentStore.getSpaceContents(spaceId);
        } catch(ContentStoreException e) {
            throw new RuntimeException("Unable to get list of files from " +
                                       "DuraStore due to: " + e.getMessage());
        }
        return spaceContents;
    }

    protected ContentStore getContentStore() {
        return contentStore;
    }

    protected String getSpaceId() {
        return spaceId;
    }
}
