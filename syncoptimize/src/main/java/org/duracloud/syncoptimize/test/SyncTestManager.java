/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.test;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.duracloud.syncoptimize.status.SyncTestEvent;
import org.duracloud.syncoptimize.status.SyncTestStatus;

import java.io.File;
import java.io.IOException;

/**
 * Manages the running of the optimizer tests. These tests use the SyncTool
 * to transfer content, then capture the time required for the transfer to
 * complete. The tests begin with a high and low thread count value and migrate
 * toward the fastest time. If the fastest time is the original high number,
 * the tests are repeated with larger thread numbers.
 *
 * @author Bill Branan
 *         Date: 5/16/14
 */
public class SyncTestManager {

    private SyncOptimizeConfig syncOptConfig;
    private File dataDir;
    private File workDir;
    private ContentStore contentStore;
    private SyncTestStatus syncStatus;
    private String prefix;
    private int transferedMB;

    private int minThreadCount = 2;
    private int initialMaxThreadCount = 30;

    public SyncTestManager(SyncOptimizeConfig syncOptConfig,
                           File dataDir,
                           File workDir,
                           SyncTestStatus syncStatus,
                           String prefix) throws IOException {
        this.syncOptConfig = syncOptConfig;
        this.dataDir = dataDir;
        this.workDir = workDir;
        this.syncStatus = syncStatus;
        this.prefix = prefix;
        this.contentStore = getContentStore();

        this.transferedMB = syncOptConfig.getNumFiles() *
                            syncOptConfig.getSizeFiles();
    }

    /**
     * Manages the running of sync tests to determine the optimal number of
     * threads to maximize throughput.
     *
     * @return optimal thread number
     */
    public int runTest() {
        SyncTester syncTester =
            new SyncTester(syncOptConfig, dataDir, workDir, contentStore, prefix);

        int lowThreads = minThreadCount;
        int highThreads = initialMaxThreadCount;
        int optimalThreads =
            optimizeThreadCount(syncTester, lowThreads, highThreads);

        while(optimalThreads >= (highThreads -2)) {
            highThreads = highThreads * 2;
            optimalThreads =
                optimizeThreadCount(syncTester, optimalThreads, highThreads);
        }
        return optimalThreads;
    }

    private int optimizeThreadCount(SyncTester syncTester,
                                    int lowThreads,
                                    int highThreads) {
        SyncTestEvent lowEvent = runSyncTest(syncTester, lowThreads);
        SyncTestEvent highEvent = runSyncTest(syncTester, highThreads);

        return optimize(syncTester, lowThreads, lowEvent, highThreads, highEvent);
    }

    /*
     * Determines optimal thread count by comparing the time required to
     * complete a sync run with a low number of threads and a high number of
     * threads. Whichever one of these takes the longest is replaced by a run
     * using the number of threads that is at the midpoint between the low and
     * high thread numbers, and the test is run again. In this way, the test
     * continues until the high and low thread numbers are very close to one
     * another. The fastest run is returned as the optimal thread count.
     */
    private int optimize(SyncTester syncTester,
                         int lowThreads,
                         SyncTestEvent lowEvent,
                         int highThreads,
                         SyncTestEvent highEvent) {
        if((highThreads - lowThreads) <= 2) {
            if(lowEvent.getElapsed() < highEvent.getElapsed()) {
                return lowThreads;
            } else {
                return highThreads;
            }
        }

        int midThreads = (lowThreads + highThreads)/2;
        SyncTestEvent midEvent = runSyncTest(syncTester, midThreads);
        if(lowEvent.getElapsed() <= highEvent.getElapsed()) {
            highThreads = midThreads;
            highEvent = midEvent;
        } else {
            lowThreads = midThreads;
            lowEvent = midEvent;
        }
        return optimize(syncTester, lowThreads, lowEvent, highThreads, highEvent);
    }

    private SyncTestEvent runSyncTest(SyncTester syncTester, int threads) {
        long elapsed = syncTester.runSyncTest(threads);
        SyncTestEvent event = new SyncTestEvent(threads, elapsed, transferedMB);
        syncStatus.addEvent(event);
        return event;
    }

    private ContentStore getContentStore() {
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(syncOptConfig.getHost(),
                                        String.valueOf(syncOptConfig.getPort()),
                                        syncOptConfig.getContext());
        Credential credential = new Credential(syncOptConfig.getUsername(),
                                               syncOptConfig.getPassword());
        storeManager.login(credential);

        try {
            return storeManager.getPrimaryContentStore();
        } catch(ContentStoreException e) {
            throw new RuntimeException("Unable to create ContentStore due to" +
                                       e.getMessage());
        }
    }

}
