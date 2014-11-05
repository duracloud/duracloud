/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.apache.commons.lang3.event.EventListenerSupport;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ContentIdUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.storage.util.StorageProviderUtil;
import org.duracloud.sync.config.SyncToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private boolean syncUpdates;
    private boolean renameUpdates;
    private boolean jumpStart;
    private String updateSuffix;
    private String storeId;
    private String prefix;
    EventListenerSupport<EndPointListener> listenerList;

    public DuraStoreSyncEndpoint(ContentStore contentStore,
                                 String username,
                                 String spaceId,
                                 boolean syncDeletes,
                                 boolean syncUpdates, 
                                 boolean renameUpdates,
                                 boolean jumpStart,
                                 String updateSuffix,
                                 String prefix) {
        this.contentStore = contentStore;
        this.username = username;
        this.storeId = this.contentStore.getStoreId();
        this.spaceId = spaceId;
        this.syncDeletes = syncDeletes;
        this.syncUpdates = syncUpdates;
        this.renameUpdates = renameUpdates;
        this.jumpStart = jumpStart;
        this.updateSuffix = updateSuffix;
        this.prefix = prefix;
        this.listenerList = new EventListenerSupport<>(EndPointListener.class);

        ensureSpaceExists();
    }
    
    public DuraStoreSyncEndpoint(ContentStore contentStore,
                                 String username,
                                 String spaceId,
                                 boolean syncDeletes,
                                 boolean jumpStart) {
        this(contentStore,
             username,
             spaceId,
             syncDeletes,
             true,
             false,
             jumpStart,
             SyncToolConfig.DEFAULT_UPDATE_SUFFIX,
             null);
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

    @Override
    public boolean syncFile(MonitoredFile syncFile, File watchDir) {
        SyncResultType result =
            syncFileAndReturnDetailedResult(syncFile, watchDir);
        return (result != SyncResultType.FAILED);
    }
    
    @Override
    public SyncResultType syncFileAndReturnDetailedResult(MonitoredFile syncFile,
                                                          File watchDir) {
        SyncResultType result = SyncResultType.ALREADY_IN_SYNC;
        String contentId =
            ContentIdUtil.getContentId(syncFile.getFile(), watchDir, prefix);
        String absPath = syncFile.getAbsolutePath();

        logger.debug("Syncing file " + absPath +
                    " to DuraCloud with ID " + contentId);
        try {
            if(jumpStart) { // Skip all of the usual checks, just push the file
                if(syncFile.exists()) {
                    doAddContent(syncFile, contentId, absPath);
                    return SyncResultType.ADDED;
                }
            }

            Map<String, String> contentProperties =
                getContentProperties(spaceId, contentId);
            boolean dcFileExists = (null != contentProperties);

            if(syncFile.exists()) {
                if(dcFileExists) { // File was updated
                    String dcChecksum =
                        contentProperties.get(ContentStore.CONTENT_CHECKSUM);
                    if(dcChecksum.equals(syncFile.getChecksum())) {
                        logger.debug("Checksum for local file {} matches " +
                                     "file in DuraCloud, no update needed.",
                                     absPath);
                    } else {
                        if(syncUpdates){
                            logger.debug("Local file {} changed, updating DuraCloud.",
                                         absPath);
                            if(renameUpdates){
                                // create backup of original using current timestamp
                                // in backup content id. I'm using current timestamp
                                // since original timestamp is just a date without
                                // timestamp info. Plus I think it is more intuitive
                                // to have the timestamp reflect the moment when the
                                // backup file was created. -dbernstein
                                String timeStamp = DateUtil.nowPlain();
                                String backupContentId = contentId +
                                    this.updateSuffix + "." +timeStamp;
                                logger.info("Renaming {} to {} to prevent it " +
                                            "from being overwritten",
                                            contentId, backupContentId);
                                this.contentStore.copyContent(this.spaceId,
                                                              contentId,
                                                              this.spaceId,
                                                              backupContentId);
                                this.listenerList.fire()
                                                 .contentBackedUp(this.storeId,
                                                                  this.spaceId,
                                                                  contentId,
                                                                  backupContentId,
                                                                  absPath);
                            }

                            addUpdateContent(contentId, syncFile, absPath);

                            this.listenerList
                                .fire().contentUpdated(this.storeId, this.spaceId,
                                                       contentId, absPath);
                            result = SyncResultType.UPDATED;
                        }else{
                            logger.debug("Local file {} changed, but sync updates options ",
                                         absPath);
                            this.listenerList.fire()
                                             .contentUpdateIgnored(this.storeId,
                                                             this.spaceId,
                                                             contentId, 
                                                             absPath);
                            result = SyncResultType.UPDATE_IGNORED;
                        }
                    }
                } else { // File was added
                    doAddContent(syncFile, contentId, absPath);
                    result =  SyncResultType.ADDED;
                }
            } else { // File was deleted (does not exist locally)
                if(syncDeletes) {
                    if(dcFileExists) {
                        result = deleteContent(spaceId, contentId, absPath);
                    } else if(null != prefix) {
                        // Check for dc file without prefix
                        String noPrefixContentId =
                            contentId.substring(prefix.length());
                        if(null != getContentProperties(spaceId,
                                                        noPrefixContentId)) {
                            result = deleteContent(spaceId,
                                                   noPrefixContentId,
                                                   absPath);
                        }
                    }
                } else {
                    logger.debug("Ignoring delete of file {}", absPath);
                }
            }
        } catch(ContentStoreException e) {
            throw new RuntimeException(e);
        }
        
        return result;
    }

    protected void doAddContent(MonitoredFile syncFile,
                                String contentId,
                                String absPath) throws ContentStoreException {
       logger.debug("Local file {} added, moving to DuraCloud.", absPath);
       addUpdateContent(contentId, syncFile, syncFile.getAbsolutePath());
       this.listenerList.fire().contentAdded(this.storeId, this.spaceId,
                                             contentId, absPath);
    }

    protected Map<String, String> getContentProperties(String spaceId,
                                                       String contentId) {
        Map<String, String> props = null;
        try {
            props = contentStore.getContentProperties(spaceId, contentId);

        } catch (ContentStoreException e) {
            logger.debug("Content properties do not exist for content item " +
                         "{} in space {}", contentId, spaceId);
        }
        return props;
    }

    private SyncResultType deleteContent(String spaceId,
                                         String contentId,
                                         String absPath)
        throws ContentStoreException {
        logger.debug("Local file {} deleted, removing from DuraCloud.", absPath);
        deleteContent(spaceId, contentId);
        return SyncResultType.DELETED;
    }

    @Override
    public void deleteContent(String spaceId, String contentId)
        throws ContentStoreException {
        logger.info("Deleting {} from DuraCloud space {}", contentId, spaceId);
        contentStore.deleteContent(spaceId, contentId);
        this.listenerList.fire().contentDeleted(this.storeId, this.spaceId,
                                                contentId);
    }

    private void addUpdateContent(String contentId,
                                  MonitoredFile syncFile,
                                  String absPath)
        throws ContentStoreException {
        logger.info("Adding local file {} to DuraCloud space {}" +
                    " with content ID {}", absPath, spaceId, contentId);
        addUpdateContent(contentId, syncFile);
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
    
    @Override
    public void addEndPointListener(EndPointListener listener) {
        this.listenerList.addListener(listener);
    }
    
    @Override
    public void removeEndPointListener(EndPointListener listener) {
        this.listenerList.removeListener(listener);
    }
}
