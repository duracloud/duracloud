/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reporter.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author: Bill Branan
 * Date: 6/17/11
 */
public class StorageReportScheduler {

    private StorageReportBuilder reportBuilder;
    private Timer timer;
    private Date startDate;
    private long frequency;

    private final Logger log =
        LoggerFactory.getLogger(StorageReportScheduler.class);

    public StorageReportScheduler(StorageReportBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
        this.timer = new Timer();
        this.startDate = null;
        this.frequency = 0;
    }

    /**
     * Sets up storage reports to be run on a new schedule. Reports are started
     * based on the startDate and frequency parameters.
     *
     * If, for instance, the startDate parameter indicates Friday at 5:00pm
     * and the frequency is 1 week (passed in as 604800000), then a report
     * will be scheduled to run weekly on Fridays at 5pm.
     *
     * If a report is still in process when the time comes to start another
     * report, the in process report continues and the scheduled report is
     * skipped. An attempt will be made again at the next scheduled time.
     *
     * Any previous report schedules are removed when this call is made.
     *
     * @param startDate when to run the next report
     * @param frequency how often reports should be run, in milliseconds
     */
    public String scheduleStorageReport(Date startDate, long frequency) {
        this.timer = new Timer();
        this.startDate = startDate;
        this.frequency = frequency;

        // Start a scheduled report based on startDate and frequency
        timer.scheduleAtFixedRate(new ReportTask(), startDate, frequency);

        String success = "Storage reports scheduled to begin on " + startDate +
            " and repeat every " + frequency/60000 + " minutes";
        log.info(success);
        return success;
    }

    public String cancelStorageReportSchedule() {
        this.timer = new Timer();
        this.startDate = null;
        this.frequency = 0;

        String success = "Storage Reports schedule cancelled.";
        log.info(success);
        return success;
    }

    /**
     * Starts a new storage report if one is not currently running.
     *
     * @return String indicating a successful start or that a report is running
     */
    public String startStorageReport() {
        if(!builderRunning()) {
            timer.schedule(new ReportTask(), 0);
            return "Storage Report Started";
        } else {
            long count = reportBuilder.getCurrentCount();
            return "Report Already In Progress, " + count + " items counted";
        }
    }

    /**
     * Cancels the currently running storage report. No report will be written
     * to storage.
     */
    public String cancelStorageReport() {
        if(builderRunning()) {
            reportBuilder.cancelReport();
            return "Storage report cancelled";
        } else {
            return "No storage report is currently running";
        }
    }

    private boolean builderRunning() {
        StorageReportBuilder.Status builderStatus = reportBuilder.getStatus();
        return builderStatus.equals(StorageReportBuilder.Status.RUNNING);
    }

    private class ReportTask extends TimerTask {
        public void run() {
            if(!builderRunning()) {
                runStorageReport();
            }
        }

        private void runStorageReport() {
            Thread builderThread = new Thread(reportBuilder);
            builderThread.start();
        }
    }

    /**
     * Provides the next scheduled start date,
     * or null if there is no scheduled date.
     */
    public Date getNextScheduledStartDate() {
        if(null != startDate) {
            Date scheduledDate = startDate;
            Date now = new Date();
            while(scheduledDate.before(now)) {
                scheduledDate = new Date(scheduledDate.getTime() + frequency);
            }
            return scheduledDate;
        } else {
            return null;
        }
    }

}
