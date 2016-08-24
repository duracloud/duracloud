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

import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.sync.mgmt.ChangedList;
import org.duracloud.sync.util.DirectoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the backing up of the changed list on a consistent schedule.
 *
 * @author: Bill Branan
 * Date: Mar 19, 2010
 */
public class ChangedListBackupManager implements Runnable {

    private final Logger logger =
        LoggerFactory.getLogger(ChangedListBackupManager.class);

    public static final int SAVED_BACKUPS = 3;
    protected static final int DEFAULT_SLEEP_TIME = 5000; // 5 seconds
    private File backupDir;
    private long backupFrequency;
    private ChangedList changedList;
    private boolean continueBackup;
    private long changedListVersion;
    private List<File> contentDirs;
    private boolean backingUp = false;
    
    public ChangedListBackupManager(ChangedList changedList,
                                    File backupDir,
                                    long backupFrequency, List<File> contentDirs) {
        this.backupDir = new File(backupDir, "changeList");
        if(!this.backupDir.exists()) {
            this.backupDir.mkdir();
        }

        this.backupFrequency = backupFrequency;
        this.changedList = changedList;
        this.contentDirs = contentDirs;
        continueBackup = true;
    }

    /**
     * Attempts to reload the changed list from a backup file. If there are
     * no backup files or the backup file cannot be read, returns -1, otherwise
     * the backup file is loaded and the time the backup file was written is
     * returned.
     *
     * @return the write time of the backup file, or -1 if no backup is available 
     */
    public long loadBackup() {
        long backupTime = -1;
        File[] backupDirFiles = getSortedBackupDirFiles();
        if(backupDirFiles.length > 0) {
            File latestBackup = backupDirFiles[0];
            try {
                backupTime = Long.parseLong(latestBackup.getName());
                changedList.restore(latestBackup, this.contentDirs);
            } catch(NumberFormatException e) {
                logger.error("Unable to load changed list backup. File in " +
                    "changed list backup dir has invalid name: " +
                    latestBackup.getName());
                backupTime = -1;
            }
        }
        return backupTime;
    }

    /**
     * Runs the backup manager. Writes out files which are a backups of the
     * changed list based on the set backup frequency. Retains SAVED_BACKUPS
     * number of backup files, removes the rest.
     */
    public void run() {
        while(continueBackup) {
            if(changedListVersion < changedList.getVersion()) {
                cleanupBackupDir(SAVED_BACKUPS);
                String filename = String.valueOf(System.currentTimeMillis());
                File persistFile = new File(backupDir, filename);
                backingUp = true;
                changedListVersion = changedList.persist(persistFile);
                backingUp = false;
            }

            sleepAndCheck(backupFrequency);
        }
    }

    /*
     * Sleeps for a given amount of time, checking frequently
     * to see if the process should be continued. This allows
     * the the backup manager to wait between activity and also
     * ensure that the higher level system shutdown process is
     * not held up by waiting for a sleep to complete here.
     */
    private void sleepAndCheck(long backupFrequency) {
        long sleepTime = DEFAULT_SLEEP_TIME;
        for(long sleepCompleted = 0;
            continueBackup && sleepCompleted < backupFrequency;
            sleepCompleted += sleepTime) {
            sleep(sleepTime);
        }
    }

    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e) {
            logger.warn("ChangedListBackupManager thread interrupted");
        }
    }

    /*
     * Removes all but the most recent backup files
     */
    private void cleanupBackupDir(int keep) {
        File[] backupDirFiles = getSortedBackupDirFiles();
        if(backupDirFiles.length > keep) {
            for(int i=keep; i<backupDirFiles.length; i++) {
                backupDirFiles[i].delete();
            }
        }
    }
    
    public void clear(){
        while(backingUp){
            sleep(100);
        }
        
        synchronized(this){
            cleanupBackupDir(0);
        }

    }

    private File[] getSortedBackupDirFiles() {
        return DirectoryUtil.listFilesSortedByModDate(backupDir);
    }

    public void endBackup() {
        continueBackup = false;
    }

    public boolean hasBackups() {
        return getSortedBackupDirFiles().length > 0;
    }    
}
