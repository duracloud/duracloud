/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import org.duracloud.client.ContentStore;
import org.duracloud.sync.endpoint.DuraStoreChunkSyncEndpoint;
import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.duracloud.sync.mgmt.StatusManager;
import org.duracloud.sync.mgmt.SyncManager;
import org.duracloud.sync.util.StoreClientUtil;
import org.duracloud.sync.walker.DirWalker;

import java.io.File;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/14/11
 */
public class Uploader {

    private SyncManager syncManager;
    private StatusManager statusManager;
    private DirWalker dirWalker;

    private String host;
    private String username;
    private String password;
    private String spaceId;
    private List<File> contentDirs;

    public Uploader(String host,
                    String username,
                    String password,
                    String spaceId,
                    List<File> contentDirs) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.spaceId = spaceId;
        this.contentDirs = contentDirs;
    }

    public void startUpload() {
        StoreClientUtil clientUtil = new StoreClientUtil();
        ContentStore contentStore =
            clientUtil.createContentStore(host,
                                          443,  // port
                                          null, // default context
                                          username,
                                          password,
                                          null); // primary content store
        SyncEndpoint syncEndpoint =
            new DuraStoreChunkSyncEndpoint(contentStore,
                                           spaceId,
                                           false,
                                           1073741824); // 1GB chunk size
        syncManager = new SyncManager(contentDirs,
                                      syncEndpoint,
                                      3, // threads
                                      10000); // change list poll frequency
        syncManager.beginSync();

        dirWalker = DirWalker.start(contentDirs);
        statusManager = StatusManager.getInstance();
    }

    public UploadStatus getUploadStatus() {
        int completed = Long.valueOf(statusManager.getSucceeded()).intValue() +
                        statusManager.getFailed().size();
        boolean complete = (statusManager.getQueueSize() == 0 &&
                            statusManager.getInWork() == 0);
        UploadStatus status = new UploadStatus(complete,
                                               dirWalker.getFilesCount(),
                                               completed);

        for(MonitoredFile file : syncManager.getFilesInTransfer()) {
            status.addFileInTransfer(file.getName(),
                                     file.length(),
                                     file.getStreamBytesRead());
        }

        return status;
    }

    public void stopUpload() {
        dirWalker.stopWalk();
        syncManager.terminateSync();
    }

}
