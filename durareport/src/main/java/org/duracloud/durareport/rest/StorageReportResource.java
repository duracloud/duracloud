/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.durareport.error.InvalidScheduleException;
import org.duracloud.durareport.storage.StorageReportBuilder;
import org.duracloud.durareport.storage.StorageReportHandler;
import org.duracloud.durareport.storage.StorageReportScheduler;
import org.duracloud.error.ContentStoreException;
import org.duracloud.reportdata.storage.StorageReportInfo;
import org.duracloud.reportdata.storage.serialize.StorageReportInfoSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportListSerializer;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private StorageReportScheduler reportScheduler;
    private static final long ONE_WEEK_MILLIS = 604800000L;

    public void initialize(ContentStoreManager storeMgr) {
        this.storeMgr = storeMgr;
        this.reportHandler = new StorageReportHandler(storeMgr);
        this.reportBuilder = new StorageReportBuilder(storeMgr, reportHandler);
        this.reportScheduler = new StorageReportScheduler(reportBuilder);

        // adds default report schedule: weekly at 1 AM on Saturday
        scheduleStorageReport(getDefaultScheduleStartDate().getTime(),
                              ONE_WEEK_MILLIS);
        // start a report now
        startStorageReport();
    }

    private Date getDefaultScheduleStartDate() {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        date.set(Calendar.HOUR, 1);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    /**
     * Initialization option for tests only.
     */
    protected void initialize(ContentStoreManager storeMgr,
                              StorageReportHandler reportHander,
                              StorageReportBuilder reportBuilder,
                              StorageReportScheduler reportScheduler) {
        this.storeMgr = storeMgr;
        this.reportHandler = reportHander;
        this.reportBuilder = reportBuilder;
        this.reportScheduler = reportScheduler;
    }

    /**
     * Provides the xml stream of the lastest storage report or null if no
     * reports exist.
     */
    public InputStream getLatestStorageReport() throws ContentStoreException {
        checkInitialized();
        return reportHandler.getLatestStorageReportStream();
    }

    /**
     * Provides the xml stream of the specified storage report or null if the
     * report does not exist.
     */
    public InputStream getStorageReport(String reportId)
        throws ContentStoreException {
        checkInitialized();
        return reportHandler.getStorageReportStream(reportId);
    }

    /**
     * Provides the xml stream of the list of storage reports, the list may
     * be empty
     */
    public String getStorageReportList() throws ContentStoreException {
        checkInitialized();
        List<String> reportList = reportHandler.getStorageReportList();
        StorageReportListSerializer serializer =
            new StorageReportListSerializer();
        return serializer.serializeReportList(reportList);
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
        StorageReportInfo reportInfo = new StorageReportInfo();
        reportInfo.setStatus(reportBuilder.getStatus().name());

        long startTime = reportBuilder.getStartTime();
        long stopTime = reportBuilder.getStopTime();
        long elapsedTime = reportBuilder.getElapsedTime();
        long count = reportBuilder.getCurrentCount();

        reportInfo.setStartTime(startTime);
        if(stopTime < startTime) { // A new run has started since the last stop
            if(elapsedTime > 0) { // A previous run has completed
                long estCompletionTime = startTime + elapsedTime;
                reportInfo.setEstimatedCompletionTime(estCompletionTime);
            }

            reportInfo.setCurrentCount(count);
        } else { // No new runs since the last stop
            reportInfo.setCompletionTime(stopTime);
            reportInfo.setFinalCount(count);
        }

        Date nextScheduledDate = reportScheduler.getNextScheduledStartDate();
        if(null != nextScheduledDate) {
            reportInfo.setNextScheduledStartTime(nextScheduledDate.getTime());
        }

        StorageReportInfoSerializer serializer =
            new StorageReportInfoSerializer();
        return serializer.serializeReportInfo(reportInfo);
    }

    /**
     * Starts a new storage report if one is not currently running.
     *
     * @return String indicating a successful start or that a report is running
     */
    public String startStorageReport() {
        checkInitialized();
        return reportScheduler.startStorageReport();
    }

    /**
     * Schedules a series of storage reports to run. The first such report
     * will begin at the indicated start time, followed by reports at the
     * given frequency.
     *
     * @param startTime time to start the next storage report
     * @param frequency time in milliseconds to wait between reports
     * @return String indicating the successful creation of a report schedule
     * @throws InvalidScheduleException if the parameters do not create a
     *                                  valid schedule
     */
    public String scheduleStorageReport(long startTime, long frequency)
        throws InvalidScheduleException {
        checkInitialized();

        if(new Date(startTime).before(new Date())) {
            throw new InvalidScheduleException("Cannot set report schedule" +
                                               " which starts in the past");
        }

        if(frequency < 600000) {
            throw new InvalidScheduleException("Minimum frequency for report " +
                                               "schedule is 10 minutes.");
        }
        return reportScheduler.scheduleStorageReport(new Date(startTime),
                                                     frequency);
    }

    /**
     * Cancels any existing storage report schedule.
     * @return String indicating the successful cancellation of a report schedule
     */
    public String cancelStorageReportSchedule() {
        checkInitialized();
        return reportScheduler.cancelStorageReportSchedule();
    }

    private void checkInitialized() {
        if(null == storeMgr) {
            throw new RuntimeException("DuraReport must be initialized.");
        }
    }

}
