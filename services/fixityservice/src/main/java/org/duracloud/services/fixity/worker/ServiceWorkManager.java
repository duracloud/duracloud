/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Andrew Woods
 *         Date: Aug 4, 2010
 */
public class ServiceWorkManager extends Thread {

    private final Logger log = LoggerFactory.getLogger(ServiceWorkManager.class);

    private boolean continueProcessing = true;
    private ThreadPoolExecutor workerPool;

    private ServiceWorkload workload;
    private ServiceWorkerFactory workerFactory;
    private ServiceResultListener resultListener;
    protected final CountDownLatch doneWorking;

    public ServiceWorkManager(ServiceWorkload workload,
                              ServiceWorkerFactory workerFactory,
                              ServiceResultListener resultListener,
                              int threads,
                              CountDownLatch doneWorking) {
        this.workload = workload;
        this.workerFactory = workerFactory;
        this.resultListener = resultListener;
        this.doneWorking = doneWorking;

        workerPool = new ThreadPoolExecutor(threads,
                                            threads,
                                            Long.MAX_VALUE,
                                            TimeUnit.NANOSECONDS,
                                            new SynchronousQueue(),
                                            new ThreadPoolExecutor.AbortPolicy());
    }

    public void run() {
        printStartMessage();

        while (continueProcessing && workload.hasNext()) {
            Runnable worker = workerFactory.newWorker(workload.next());

            boolean successStartingWorker = false;
            while (!successStartingWorker) {
                try {
                    workerPool.execute(worker);
                    successStartingWorker = true;

                } catch (RejectedExecutionException e) {
                    successStartingWorker = false;
                    doSleep(10000);
                }
            }
        }

        shutdown();

        printEndMessage();
    }

    private void shutdown() {
        resultListener.setProcessingComplete();
        
        workerPool.shutdown();
        try {
            workerPool.awaitTermination(60, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            log.warn("Interruped waiting for worker pool to shut down. " +
                "Assuming shutdown is complete.");
        } finally {
            if (doneWorking != null) {
                doneWorking.countDown();
            }
        }
    }

    private void doSleep(long millis) {
        try {
            sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    private void printStartMessage() {
        StringBuffer startMsg = new StringBuffer();
        log.info(startMsg.toString());
    }

    private void printEndMessage() {
        log.info(getProcessingStatus());
    }

    public String getProcessingStatus() {
        return resultListener.getProcessingStatus();
    }

    /**
     * Indicate that service should stop after the items currently being
     * processed have completed.
     */
    public void stopProcessing() {
        continueProcessing = false;
        shutdown();
    }

}
