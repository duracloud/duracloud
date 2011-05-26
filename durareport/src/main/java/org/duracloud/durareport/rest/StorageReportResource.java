/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.durareport.storage.StorageReport;
import org.duracloud.durareport.storage.StorageReportBuilder;
import org.duracloud.durareport.storage.StorageReportHandler;
import org.duracloud.error.ContentStoreException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * First line of business logic to handle the requests coming in via the
 * Storage Report REST API.
 *
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class StorageReportResource {

    private ContentStoreManager storeMgr = null;
    private StorageReportBuilder reportBuilder;
    private StorageReportHandler reportHandler;

    public void initialize(ContentStoreManager storeMgr) {
        this.storeMgr = storeMgr;
        this.reportHandler = new StorageReportHandler(storeMgr);
        this.reportBuilder = new StorageReportBuilder(storeMgr, reportHandler);
        startStorageReport();
    }

    /**
     * Initialization option for tests only.
     */
    protected void initialize(ContentStoreManager storeMgr,
                              StorageReportHandler reportHander,
                              StorageReportBuilder reportBuilder) {
        this.storeMgr = storeMgr;
        this.reportHandler = reportHander;
        this.reportBuilder = reportBuilder;
    }

    /**
     * Provides the xml stream of the lastest storage report or null if no
     * reports exist.
     */
    public InputStream getLatestStorageReport() throws ContentStoreException {
        checkInitialized();
        StorageReport latestReport = reportHandler.getLatestStorageReport();
        if(null != latestReport) {
            return latestReport.getReportStream();
        } else {
            return null;
        }
    }

    /**
     * Provides the xml stream of the specified storage report or null if the
     * report does not exist.
     */
    public InputStream getStorageReport(String reportId)
        throws ContentStoreException {
        checkInitialized();
        StorageReport report = reportHandler.getStorageReport(reportId);
        if(null != report) {
            return report.getReportStream();
        } else {
            return null;
        }
    }

    /**
     * Provides the xml stream of the list of storage reports, the list may
     * be empty
     */
    public InputStream getStorageReportList() throws ContentStoreException {
        checkInitialized();
        return reportHandler.getStorageReportList();
    }

    /**
     * Provides information about the storage report. This will include
     * status of any running reports as well data about the last successful
     * report run.
     *
     * @return XML serialized set of information about the storage report system
     */
    public String getStorageReportInfo() {
        checkInitialized();
        String prefix = "storage-report-";
        Map<String, String> infoItems = new HashMap<String, String>();
        infoItems.put(prefix+"status", reportBuilder.getStatus().name());

        long startTime = reportBuilder.getStartTime();
        long stopTime = reportBuilder.getStopTime();
        long elapsedTime = reportBuilder.getElapsedTime();
        String count = String.valueOf(reportBuilder.getCurrentCount());
        if(stopTime < startTime) { // A new run has started since the last stop
            String start = DateUtil.convertToStringMid(startTime);
            infoItems.put(prefix+"start-time", start);

            if(elapsedTime > 0) { // A previous run has completed
                long estCompletionTime = startTime + elapsedTime;
                String completion =
                    DateUtil.convertToStringMid(estCompletionTime);
                infoItems.put(prefix+"estimated-completion-time", completion);
            }

            infoItems.put(prefix+"current-count", count);
        } else { // No new runs since the last stop
            String stop = DateUtil.convertToStringMid(stopTime);
            infoItems.put(prefix+"completion-time", stop);

            infoItems.put(prefix+"final-count", count);

            // TODO: include next scheduled start time
        }

        return SerializationUtil.serializeMap(infoItems);
    }

    /**
     * Starts a new storage report if one is not currently running.
     *
     * @return String indicating a successful start or that a report is running
     */
    public String startStorageReport() {
        checkInitialized();
        StorageReportBuilder.Status builderStatus = reportBuilder.getStatus();
        if(builderStatus.equals(StorageReportBuilder.Status.CREATED) ||
           builderStatus.equals(StorageReportBuilder.Status.COMPLETE)) {
            runStorageReport();
            return "Report Started";
        } else {
            long count = reportBuilder.getCurrentCount();
            return "Report Already In Progress, " + count + " items counted";
        }
    }

    private void checkInitialized() {
        if(null == storeMgr) {
            throw new RuntimeException("DuraReport must be initialized.");
        }
    }

    private void runStorageReport() {
        Thread builderThread = new Thread(reportBuilder);
        builderThread.start();
    }

}
