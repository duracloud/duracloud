/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.duracloud.client.ContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The SpaceListManager manages the creation of files containing DuraCloud space content IDs
 * for the specified spaces on the local file system.
 * One content file for each space will be created.
 *
 * @author: Erik Paulsson
 * Date: June 27, 2013
 */
public class SpaceListManager implements Runnable {

    private final Logger logger =
        LoggerFactory.getLogger(SpaceListManager.class);

    private ThreadPoolExecutor workerPool;
    private ContentStore contentStore;
    private File contentDir;
    private List<String> spaces;
    private boolean overwrite = true;
    private boolean complete = false;

    public SpaceListManager(ContentStore contentStore,
                            File contentDir,
                            List<String> spaces,
                            boolean overwrite,
                            int threads) {
        this.contentStore = contentStore;
        this.contentDir = contentDir;
        this.spaces = spaces;
        this.overwrite = overwrite;

        // Create thread pool for  SpaceListWorkers
        workerPool =
            new ThreadPoolExecutor(threads,
                                   threads,
                                   Long.MAX_VALUE,
                                   TimeUnit.NANOSECONDS,
                                   new SynchronousQueue(),
                                   new ThreadPoolExecutor.AbortPolicy());
    }

    public void run() {
        while(!complete) {
            for(String spaceName: spaces) {
                while(!retrieveSpaceList(spaceName)) {
                    sleep(1000);
                }
            }
            shutdown();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private boolean retrieveSpaceList(String spaceName) {
        try {
            SpaceListWorker worker = new SpaceListWorker(contentStore,
                                                         spaceName,
                                                         contentDir,
                                                         overwrite);
            workerPool.execute(worker);
            return true;
        } catch(RejectedExecutionException e) {
            return false;
        }
    }

    /**
     * Stops the retrieval of space content listings, no further listings will be
     * retrieved after those which are in progress have completed.
     */
    public void shutdown() {
        logger.info("Closing SpaceList Manager");
        workerPool.shutdown();

        try {
            workerPool.awaitTermination(30, TimeUnit.MINUTES);
        } catch(InterruptedException e) {          
        }

        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }
}