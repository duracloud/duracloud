/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.monitor;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.duracloud.sync.mgmt.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors of local file system directories for changes.
 *
 * @author: Bill Branan
 * Date: Mar 12, 2010
 */
public class DirectoryUpdateMonitor {

    private final Logger logger = LoggerFactory.getLogger(SyncManager.class);

    private FileAlterationMonitor monitor;

    /**
     * Creates a directory update monitor which, when started, will notify
     * on changes within the given directories.
     *
     * @param directories to monitor
     * @param pollFrequency how often the monitor should look for changes
     */
    public DirectoryUpdateMonitor(List<File> directories,
                                  long pollFrequency,
                                  boolean syncDeletes) {
        monitor = new FileAlterationMonitor(pollFrequency);

        for (File watchDir : directories) {
            if (watchDir.exists()) {
                FileAlterationObserver observer; 
                if(watchDir.isDirectory()){
                    observer =
                        new FileAlterationObserver(watchDir);
                }else {
                    final File file = watchDir;
                    observer =
                        new FileAlterationObserver(watchDir.getParentFile(), new FileFilter(){
                            @Override
                            public boolean accept(File pathname) {
                                return (file.equals(pathname));
                            }});
                }

                observer.addListener(new DirectoryListener(syncDeletes));
                monitor.addObserver(observer);

            } else {
                throw new RuntimeException("Path " +
                    watchDir.getAbsolutePath() +
                    " does not exist");
            }
        }
    }

    /**
     * Starts the monitor watching for updates.
     */
    public void startMonitor() {
        logger.info("Starting Directory Update Monitor");
        try {
            monitor.start();
        } catch(IllegalStateException e) {
            logger.info("File alteration monitor is already started: " + e.getMessage());
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Stops the monitor, no further updates will be reported.
     */
    public void stopMonitor() {
        logger.info("Stopping Directory Update Monitor");
        try {
            monitor.stop();
        } catch(IllegalStateException e) {
            logger.info("File alteration monitor is already stopped: " + e.getMessage());
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
