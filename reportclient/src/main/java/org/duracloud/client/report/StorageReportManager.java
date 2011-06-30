/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report;

import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.Securable;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportInfo;

import java.util.Date;
import java.util.List;

/**
 * Allows for communication with DuraReport
 *
 * @author: Bill Branan
 * Date: 6/2/11
 */
public interface StorageReportManager extends Securable {

    public String getBaseURL();

    /**
     * Retrieves the latest completed storage report.
     *
     * @return StorageReport
     * @throws NotFoundException if no reports are available
     * @throws ReportException
     */
    public StorageReport getLatestStorageReport()
        throws NotFoundException, ReportException;

    /**
     * Retrieves the list of storage report IDs, ordered by
     * report completion date.
     *
     * @return List os report IDs
     * @throws ReportException
     */
    public List<String> getStorageReportList() throws ReportException;

    /**
     * Retrieves a storage report indicated by a report ID.
     *
     * @param reportId
     * @return StorageReport
     * @throws NotFoundException if a storage report with that ID does not exist
     * @throws ReportException
     */
    public StorageReport getStorageReport(String reportId)
        throws NotFoundException, ReportException;

    /**
     * Retrieves information about storage reporting activities.
     *
     * @return
     * @throws ReportException
     */
    public StorageReportInfo getStorageReportInfo()
        throws ReportException;

    /**
     * Tells DuraReport to start running a new storage report generation
     * process. If a report generation process is already underway, this
     * call is ignored.
     *
     * @return success message
     * @throws ReportException
     */
    public String startStorageReport() throws ReportException;

    /**
     * Cancels any currently running storage report.
     *
     * @return success message
     */
    public String cancelStorageReport() throws ReportException;

    /**
     * Schedules a series of storage reports to run. The first such report
     * will begin at the indicated start time, followed by reports at the
     * given frequency.
     *
     * @param startTime time to start the next storage report
     * @param frequency time in milliseconds to wait between reports
     * @return success message
     */
    public String scheduleStorageReport(Date startTime, long frequency)
        throws ReportException;

    /**
     * Cancels any existing storage report schedule.
     * @return success message
     */
    public String cancelStorageReportSchedule() throws ReportException;

}
