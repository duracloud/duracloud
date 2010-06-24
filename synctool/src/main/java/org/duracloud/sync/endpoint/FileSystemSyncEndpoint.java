/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Allows syncing to be performed to a location on the local file system.
 *
 * This class is primarily for testing purposes, and it not intended for
 * production use.
 *
 * Note that directories that have been deleted at the source are not deleted
 * by this endpoint. The nature of cloud storage is that if no files exist in
 * a given directory, that directory doesn't exist (directories are completely
 * virtual and exist only in the names of the individual files.) 
 *
 * @author: Bill Branan
 * Date: Mar 26, 2010
 */
public class FileSystemSyncEndpoint implements SyncEndpoint {

    private final Logger logger =
        LoggerFactory.getLogger(FileSystemSyncEndpoint.class);    

    private File syncToDir;
    private boolean syncDeletes;

    /**
     * Creates a SyncEnpoint pointing to a directory on the local file system
     * where files will be synced to.
     *
     * @param syncToDir
     */
    public FileSystemSyncEndpoint(File syncToDir, boolean syncDeletes) {
        this.syncToDir = syncToDir;
        this.syncDeletes = syncDeletes;
    }

    public boolean syncFile(File syncFile, File watchDir) {
        boolean success = false;
        File syncToFile = getSyncToFile(syncFile, watchDir);

        logger.info("Syncing file: " + syncFile.getAbsolutePath() +
                    "\n   in watchDir " + watchDir.getAbsolutePath() +
                    "\n   to " + syncToFile.getAbsolutePath());

        if(syncFile.exists()) { // File was added or updated
            try {
                if(!syncToFile.getParentFile().exists()) {
                    createParentDir(syncToFile.getParentFile());
                }
                FileUtils.copyFile(syncFile, syncToFile);
                success = true;
            } catch(IOException e) {
                logger.error("Unable to sync updated file " +
                    syncFile.getAbsolutePath() + " to " +
                    syncToFile.getAbsolutePath() + " due to " +
                    e.getMessage(), e);
                success = false;
            }
        } else { // File was deleted
            if(syncDeletes) {
                success = syncToFile.delete();
            } else {
                success = true;
            }
        }
        return success;
    }

    private synchronized void createParentDir(File parentDir) {
        parentDir.mkdir();
    }

    protected File getSyncToFile(File syncFile, File watchDir) {
        URI relativeFileURI = watchDir.toURI().relativize(syncFile.toURI());
        return new File(syncToDir, relativeFileURI.getPath());
    }

    public Iterator<String> getFilesList() {
        List<String> filesList = new ArrayList<String>();
        getFilesRelative(filesList, syncToDir);
        return filesList.iterator();
    }

    private void getFilesRelative(List<String> filesList, File dir) {
        if(dir.isDirectory()) {
            for(File file : dir.listFiles()) {
                if(file.isDirectory()) {
                    getFilesRelative(filesList, file);
                } else {
                    filesList.add(getRelativeFilePath(file));
                }
            }
        }
    }

    private String getRelativeFilePath(File file) {
        return syncToDir.toURI().relativize(file.toURI()).getPath();
    }
}
