/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.backup;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.duracloud.sync.mgmt.ChangedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the execution of the changed list backup manager
 *
 * @author: Bill Branan
 * Date: Mar 24, 2010
 */
public class SyncBackupManager {

    private final Logger logger =
        LoggerFactory.getLogger(SyncBackupManager.class);

    private ChangedListBackupManager backupManager;
    
    private ExecutorService execPool;

    public SyncBackupManager(File backupDir, long frequency, List<File> contentDirs) {
        logger.info("Starting Sync Backup Manager");
        backupManager = new ChangedListBackupManager(ChangedList.getInstance(),
                                                     backupDir,
                                                     frequency, 
                                                     contentDirs);

        // Create thread pool for backupManager
        execPool = Executors.newFixedThreadPool(1);
    }

    public long attemptRestart() {
        return backupManager.loadBackup();
    }

    public boolean hasBackups(){
        return this.backupManager.hasBackups();
    }
    
    public void startupBackups() {
        execPool.execute(backupManager);
    }

    public void endBackups() {
        logger.info("Closing Sync Backup Manager, ending backups");
        backupManager.endBackup();
        execPool.shutdown();
    }
    
    public void clearBackups(){
        backupManager.clear();
    }
}