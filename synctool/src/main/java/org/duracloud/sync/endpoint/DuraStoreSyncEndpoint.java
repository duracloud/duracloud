/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.storage.util.StorageProviderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
    private String username;
    private String spaceId;
    private boolean syncDeletes;

    public DuraStoreSyncEndpoint(ContentStore contentStore,
                                 String username,
                                 String spaceId,
                                 boolean syncDeletes) {
        this.contentStore = contentStore;
        this.username = username;
        this.spaceId = spaceId;
        this.syncDeletes = syncDeletes;
        ensureSpaceExists();
    }
    
    protected String getUsername(){
        return this.username;
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
                contentStore.createSpace(spaceId);
                return false;
            }
        } catch (ContentStoreException e) {
            logger.warn("Could not connect to space with ID '" + spaceId +
                "' due to error: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean syncFile(MonitoredFile syncFile, File watchDir) {
        String contentId = getContentId(syncFile, watchDir);
        logger.info("Syncing file " + syncFile.getAbsolutePath() +
                    " to DuraCloud with ID " + contentId);

        Map<String, String> contentProperties = getContentProperties(spaceId,
                                                                     contentId);
        boolean dcFileExists = (null != contentProperties);
        try {
            if(syncFile.exists()) {
                if(dcFileExists) { // File was updated
                    String dcChecksum =
                        contentProperties.get(ContentStore.CONTENT_CHECKSUM);
                    if(dcChecksum.equals(syncFile.getChecksum())) {
                        logger.debug("Checksum for local file {} matches " +
                            "file in DuraCloud, no update needed.",
                            syncFile.getAbsolutePath());
                    } else {
                        logger.debug("Local file {} changed, updating DuraCloud.",
                                     syncFile.getAbsolutePath());
                        addUpdateContent(contentId, syncFile);
                    }
                } else { // File was added
                    logger.debug("Local file {} added, moving to DuraCloud.",
                                 syncFile.getAbsolutePath());
                    addUpdateContent(contentId, syncFile);
                }
            } else { // File was deleted
                if(dcFileExists) {
                    if(syncDeletes) {
                        logger.debug("Local file {} deleted, " +
                                     "removing from DuraCloud.",
                                     syncFile.getAbsolutePath());
                        deleteContent(spaceId, contentId);
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

    protected Map<String, String> getContentProperties(String spaceId,
                                                       String contentId) {
        Map<String, String> props = null;
        try {
            props = contentStore.getContentProperties(spaceId, contentId);

        } catch (ContentStoreException e) {
            logger.info("Content properties !exist: {}/{}", spaceId, contentId);
        }
        return props;
    }

    protected void deleteContent(String spaceId, String contentId)
        throws ContentStoreException {
        contentStore.deleteContent(spaceId, contentId);
    }

    protected void addUpdateContent(String contentId, MonitoredFile syncFile)
        throws ContentStoreException {
        InputStream syncStream = syncFile.getStream();
        Map<String,String> props = createProps(syncFile.getAbsolutePath(), this.username);        

        try {
            contentStore.addContent(spaceId,
                                    contentId,
                                    syncStream,
                                    syncFile.length(),
                                    syncFile.getMimetype(),
                                    syncFile.getChecksum(),
                                    props);
        } finally {
            try {
                syncStream.close();
            } catch(IOException e) {
                logger.error("Error attempting to close stream for file " +
                             contentId + ": " + e.getMessage(), e);
            }
        }
    }

    protected Map<String, String> createProps(String absolutePath, String username) {
        return StorageProviderUtil.createContentProperties(absolutePath, username);
    }

    /*
     * Determines the content ID of a file: the path of the file relative to
     * the watched directory. If the watched directory is null, the content ID
     * is simply the name of the file.
     */
    protected String getContentId(MonitoredFile syncFile, File watchDir) {
        String contentId = syncFile.getName();
        if(null != watchDir) {
            URI relativeFileURI = watchDir.toURI().relativize(syncFile.toURI());
            contentId = relativeFileURI.getPath();
        }
        return contentId;
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
