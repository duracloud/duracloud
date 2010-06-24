/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.monitor;

import org.apache.commons.io.monitor.FilesystemMonitor;
import org.apache.commons.io.monitor.FilesystemObserver;
import org.duracloud.sync.mgmt.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Monitors of local file system directories for changes.
 *
 * @author: Bill Branan
 * Date: Mar 12, 2010
 */
public class DirectoryUpdateMonitor {

    private final Logger logger = LoggerFactory.getLogger(SyncManager.class);

    private FilesystemMonitor monitor;

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
        monitor = new FilesystemMonitor(pollFrequency);

        for (File watchDir : directories) {
            if (watchDir.exists() && watchDir.isDirectory()) {
                FilesystemObserver observer = new FilesystemObserver(watchDir);
                observer.addListener(new DirectoryListener(syncDeletes));
                monitor.addObserver(observer);
            } else {
                throw new RuntimeException("Path " +
                    watchDir.getAbsolutePath() +
                    " either does not exist or is not a directory");
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
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
