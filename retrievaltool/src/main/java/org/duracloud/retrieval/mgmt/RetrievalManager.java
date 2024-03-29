/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.duracloud.common.model.ContentItem;
import org.duracloud.common.retry.Retrier;
import org.duracloud.retrieval.source.RetrievalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RetreivalManager manages the retrieval of files from DuraCloud to the
 * local file system.
 *
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class RetrievalManager implements Runnable {

    private final Logger logger =
        LoggerFactory.getLogger(RetrievalManager.class);

    private RetrievalSource source;
    private File contentDir;
    private File workDir;
    private boolean overwrite;
    private ThreadPoolExecutor workerPool;
    private OutputWriter outWriter;
    private boolean createSpaceDir;
    private boolean applyTimestamps;
    private boolean complete;
    private Phaser phaser;

    public RetrievalManager(RetrievalSource source,
                            File contentDir,
                            File workDir,
                            boolean overwrite,
                            int threads,
                            OutputWriter outWriter,
                            boolean createSpaceDir,
                            boolean applyTimestamps) {
        logger.info("Starting Retrieval Manager with " + threads + " threads");
        this.source = source;
        this.contentDir = contentDir;
        this.workDir = workDir;
        this.overwrite = overwrite;
        this.outWriter = outWriter;
        this.createSpaceDir = createSpaceDir;
        this.applyTimestamps = applyTimestamps;
        this.phaser = new Phaser();

        // Create thread pool for retrieval workers
        workerPool =
            new ThreadPoolExecutor(threads,
                                   threads,
                                   Long.MAX_VALUE,
                                   TimeUnit.NANOSECONDS,
                                   new SynchronousQueue(),
                                   new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Begins the content retrieval process
     */
    public void run() {
        phaser.register();

        try {
            while (!complete) {

                ContentItem contentItem = new Retrier(5, 4000, 2).execute(() -> {
                    return source.getNextContentItem();
                });

                if (contentItem == null) {
                    break;
                }
                while (!retrieveContent(contentItem)) {
                    sleep(1000);
                }
            }

        } catch (Exception ex) {
            logger.error("Failed to run to completion", ex);
        } finally {
            shutdown();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Exit sleep on interruption
        }
    }

    private boolean retrieveContent(ContentItem contentItem) {
        try {
            logger.debug("contentItem={}", contentItem);
            RetrievalWorker worker = new RetrievalWorker(contentItem,
                                                         source,
                                                         contentDir,
                                                         overwrite,
                                                         outWriter,
                                                         createSpaceDir,
                                                         applyTimestamps);
            phaser.register();
            CompletableFuture.runAsync(worker, workerPool)
                             .thenRun(phaser::arriveAndDeregister);
            return true;
        } catch (RejectedExecutionException e) {
            phaser.arriveAndDeregister();
            return false;
        }
    }

    /**
     * Stops the retrieval, no further files will be retrieved after those
     * which are in progress have completed.
     */
    public void shutdown() {
        logger.info("Closing Retrieval Manager");
        workerPool.shutdown();

        logger.info("Waiting for retrievals to complete, this may take some time...");
        phaser.arriveAndAwaitAdvance();

        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

}
