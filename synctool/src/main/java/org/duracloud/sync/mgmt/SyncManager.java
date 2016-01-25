/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The SyncManager is responsible to watch for new entries in the ChangedList
 * and make sure those changes are pushed to the SyncEndpoint.
 *
 * @author: Bill Branan
 * Date: Mar 15, 2010
 */
public class SyncManager implements ChangeHandler {

    private final Logger logger = LoggerFactory.getLogger(SyncManager.class);

    private ChangeWatcher changeWatcher;
    private List<File> watchDirs;
    private SyncEndpoint endpoint;
    private ExecutorService watcherPool;
    private ThreadPoolExecutor workerPool;
    private ArrayList<SyncWorker> workerList;

    /**
     * Creates a SyncManager which, when started, will watch for updates to
     * the ChangedList and kick off SyncWorkers to handle any changed files.
     *
     * @param endpoint
     * @param threads
     * @param frequency
     */
    public SyncManager(List<File> watchDirs,
                       SyncEndpoint endpoint,
                       int threads,
                       long frequency) {
        logger.info("Starting Sync Manager with " + threads + " threads");
        this.watchDirs = watchDirs;
        this.endpoint = endpoint;
        changeWatcher = new ChangeWatcher(ChangedList.getInstance(),
                                          this,
                                          frequency);

        // Create thread pool for changeWatcher
        watcherPool = Executors.newFixedThreadPool(1);
        // Create thread pool for workers
        workerPool =
            new ThreadPoolExecutor(threads,
                                   threads,
                                   Long.MAX_VALUE,
                                   TimeUnit.NANOSECONDS,
                                   new SynchronousQueue(),
                                   new ThreadPoolExecutor.AbortPolicy());
        workerList = new ArrayList<SyncWorker>();
    }

    /**
     * Allows the SyncManager to begin watching for updates to the ChangedList
     */
    public void beginSync() {
        watcherPool.execute(changeWatcher);
    }

    /**
     * Stops the sync, no further changed files will be handled after those
     * which are in progress have completed.
     */
    public void endSync() {
        logger.info("Closing Sync Manager, ending sync");
        changeWatcher.endWatch();
        watcherPool.shutdown();
        workerPool.shutdown();
    }

    public void terminateSync() {
        logger.info("Closing Sync Manager, terminating sync");
        changeWatcher.endWatch();
        watcherPool.shutdownNow();
        workerPool.shutdownNow();
    }

    /**
     * Notifies the SyncManager that a file has changed
     *
     * @param changedFile the changed file
     * @returns true if file accepted for processing, false otherwise
     */
    public boolean handleChangedFile(ChangedFile changedFile) {
        File watchDir = getWatchDir(changedFile.getFile());
        SyncWorker worker = new SyncWorker(changedFile, watchDir, endpoint);

        try {
            addToWorkerList(worker);
            workerPool.execute(worker);
            return true;
        } catch(RejectedExecutionException e) {
            synchronized(workerList){
                workerList.remove(worker);
            }
            return false;
        }
    }

    /*
     * Determines which of the watched directories includes the changed file.
     * Returns null if the file is not included in any watch directories.
     */
    protected File getWatchDir(File changedFile) {
        for(File watchDir : watchDirs) {
            File changedFileParent = changedFile.getParentFile();
            while(changedFileParent != null) {
                if(changedFileParent.equals(watchDir)) {
                    return watchDir;
                } else {
                    changedFileParent = changedFileParent.getParentFile();
                }
            }
        }
        return null;
    }

    private void addToWorkerList(SyncWorker workerToAdd) {
        cleanWorkerList();
        for(int i=0; i<workerList.size(); i++) {
            SyncWorker worker = workerList.get(i);
            if(worker.getMonitoredFile().getAbsolutePath().equals(
               workerToAdd.getMonitoredFile().getAbsolutePath())) {
                workerList.remove(i);
            }
        }
        workerList.add(workerToAdd);
    }

    private void cleanWorkerList() {
        for(int i=0; i<workerList.size(); i++) {
            SyncWorker worker = workerList.get(i);
            if(worker.isComplete()) {
                workerList.remove(i);
            }
        }
    }

    public synchronized List<MonitoredFile> getFilesInTransfer() {
        cleanWorkerList();
        List<MonitoredFile> monitoredFiles = new ArrayList<MonitoredFile>();
        for(SyncWorker worker : (ArrayList<SyncWorker>)workerList.clone()) {
            monitoredFiles.add(worker.getMonitoredFile());
        }
        return monitoredFiles;
    }

}
