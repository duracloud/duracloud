/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.durareport.error.ReportBuilderException;
import org.duracloud.durareport.error.StorageReportCancelledException;
import org.duracloud.durareport.storage.metrics.DuraStoreMetricsCollector;
import org.duracloud.error.ContentStoreException;
import org.duracloud.reportdata.storage.StorageReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Builder to be run in a thread to generate metrics reports.
 *
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class StorageReportBuilder implements Runnable {

    public static enum Status {CREATED, RUNNING, COMPLETE, CANCELLED, ERROR};
    private final Logger log =
        LoggerFactory.getLogger(StorageReportBuilder.class);
    public static final int maxRetries = 20;

    private ContentStoreManager storeMgr = null;
    private StorageReportHandler reportHandler;

    private Status status;
    private String error;
    private long startTime;
    private long stopTime;
    private long elapsedTime;
    private boolean run;

    private DuraStoreMetricsCollector durastoreMetrics;

    public StorageReportBuilder(ContentStoreManager storeMgr,
                                StorageReportHandler reportHandler) {
        this.storeMgr = storeMgr;
        this.reportHandler = reportHandler;
        this.status = Status.CREATED;
        this.error = null;
        this.startTime = 0;
        this.stopTime = 0;
        this.elapsedTime = 0;

        try {
            StorageReport lastReport = reportHandler.getLatestStorageReport();
            if(null != lastReport) {
                this.stopTime = lastReport.getCompletionTime();
                this.elapsedTime = lastReport.getElapsedTime();
            }
        } catch(Exception e) {
            log.warn("Unable to retrieve latest storage report due to: " +
                     e.getMessage());
        }
    }

    @Override
    public void run() {
        run = true;
        startTime = System.currentTimeMillis();
        log.info("Storage Report starting at time: " + startTime);
        status = Status.RUNNING;
        try {
            collectStorageMetrics();
            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            reportHandler.storeReport(durastoreMetrics,
                                      stopTime,
                                      elapsedTime);
            status = Status.COMPLETE;
            log.info("Storage Report completed at time: " + stopTime);
        } catch (StorageReportCancelledException e) {
            stopTime = System.currentTimeMillis();
            log.warn("Storage Report cancelled at: " + stopTime);
            status = Status.CANCELLED;
        } catch(ReportBuilderException e) {
            stopTime = System.currentTimeMillis();
            error = e.getMessage();
            log.error("Unable to complete metrics collection due to: " +
                      e.getMessage());
            status = Status.ERROR;
        }
    }

    public void cancelReport() {
        new DuraCloudCheckedException(); // loads this exception type
        log.info("Cancelling Storage Report");
        run = false;
    }

    private void collectStorageMetrics() {
        durastoreMetrics = new DuraStoreMetricsCollector();

        Map<String, ContentStore> contentStores = retryGetContentStores();
        for(ContentStore contentStore : contentStores.values()) {
            checkRun();
            String storeId = contentStore.getStoreId();
            String storeType = contentStore.getStorageProviderType();
            for(String spaceId : retryGetSpaces(contentStore)) {
                checkRun();
                Iterator<String> contentIds =
                    retryGetSpaceContents(contentStore, spaceId);
                while(contentIds.hasNext()) {
                    checkRun();
                    String contentId = contentIds.next();
                    Map<String, String> contentMetadata =
                        retryGetContentMetadata(contentStore,
                                                spaceId,
                                                contentId);
                    updateMetrics(contentMetadata, storeId, storeType, spaceId);
                }
            }
        }
    }

    private void updateMetrics(Map<String, String> contentMetadata,
                               String storeId,
                               String storeType,
                               String spaceId) {
        if(null != contentMetadata) {
            String mimetype = contentMetadata.get(ContentStore.CONTENT_MIMETYPE);
            long size = convert(contentMetadata.get(ContentStore.CONTENT_SIZE));
            durastoreMetrics.update(storeId, storeType, spaceId, mimetype, size);
        }
    }

    private Map<String, ContentStore> retryGetContentStores() {
        for(int i=0; i<maxRetries; i++) {
            checkRun();
            try {
                return storeMgr.getContentStores();
            } catch (ContentStoreException e) {
                log.warn("Exception attempting to retrieve content " +
                         "stores list: " + e.getMessage());
                wait(i);
            }
        }
        throw new ReportBuilderException("Exceeded retries attempting to " +
                                         "retrieve content stores list");
    }

    private List<String> retryGetSpaces(ContentStore contentStore) {
        for(int i=0; i<maxRetries; i++) {
            checkRun();
            try {
                return contentStore.getSpaces();
            } catch (ContentStoreException e) {
                String store = getStoreInfo(contentStore);
                log.warn("Exception attempting to retrieve spaces list for " +
                         "store: " + store + " due to: " + e.getMessage());
                wait(i);
            }
        }
        throw new ReportBuilderException("Exceeded retries attempting to " +
                                         "retrieve spaces list");
    }

    private Iterator<String> retryGetSpaceContents(ContentStore contentStore,
                                                   String spaceId) {
        for(int i=0; i<maxRetries; i++) {
            checkRun();
            try {
                return contentStore.getSpaceContents(spaceId);
            } catch (ContentStoreException e) {
                String store = getStoreInfo(contentStore);
                log.warn("Exception attempting to retrieve space contents " +
                         "list (for " + spaceId + " in store " + store +
                         "): " + e.getMessage());
                wait(i);
            }
        }
        throw new ReportBuilderException("Exceeded retries attempting to " +
                                         "retrieve space contents list " +
                                         "(for " + spaceId + ")");
    }

    private Map<String, String> retryGetContentMetadata(ContentStore contentStore,
                                                        String spaceId,
                                                        String contentId) {
        for(int i=0; i<maxRetries; i++) {
            checkRun();
            try {
                return contentStore.getContentMetadata(spaceId, contentId);
            } catch (ContentStoreException e) {
                String store = getStoreInfo(contentStore);
                log.warn("Exception attempting to retrieve content metadata " +
                         "(for " + spaceId + ":" + contentId + " in store " +
                         store + "): " + e.getMessage());
                wait(i);
            }
        }
        log.error("Exceeded retries attempting to retrieve content metadata " +
                  "(for " + spaceId + ":" + contentId + "). Skipping item.");
        return null;
    }

    private String getStoreInfo(ContentStore contentStore) {
        return contentStore.getStoreId() + "(" +
               contentStore.getStorageProviderType() + ")";
    }

    private long convert(String sizeStr) {
        try {
            return Long.valueOf(sizeStr);
        } catch(NumberFormatException e) {
            return 0;
        }
    }

    private void wait(int index) {
        checkRun();
        try {
            Thread.sleep(1000 * index);
        } catch(InterruptedException e) {
        }
    }

    private void checkRun() {
        if(!run) {
            throw new StorageReportCancelledException();
        }
    }

    /**
     * Gets the current status of the report builder
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the text of the last error which occurred (if any)
     */
    public String getError() {
        return error;
    }

    /**
     * Gets the current count for the in-process report builder run
     */
    public long getCurrentCount() {
        return durastoreMetrics.getTotalItems();
    }

    /**
     * Gets the stop time (in millis) of the most recent report builder run
     */
    public long getStopTime() {
        return stopTime;
    }

    /**
     * Gets the starting time (in millis) of the most recent report builder run
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the time (in millis) required to complete the most recent
     * report builder run
     */
    public long getElapsedTime() {
        return elapsedTime;
    }
}