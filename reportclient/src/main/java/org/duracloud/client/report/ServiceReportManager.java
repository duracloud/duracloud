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
import org.duracloud.serviceconfig.ServiceSummary;

import java.util.List;

/**
 * @author: Bill Branan
 * Date: 6/30/11
 */
public interface ServiceReportManager {

    /**
     * Retrieves a listing of services which are currently deployed in
     * DuraCloud. The returned list can include services which are in process,
     * services which have completed, and services which are long
     * running (i.e. will never complete).
     *
     * @return list of deployed services
     */
    public List<ServiceSummary> getDeployedServicesReport()
        throws ReportException;

    /**
     * Retrieves a listing of completed services up to the default
     * limit (currently 20).
     *
     * The returned list is sorted based on update (completed/started) time,
     * with the most recent at the top of the list.
     *
     * @return list of the most recent completed services
     */
    public List<ServiceSummary> getCompletedServicesReport()
        throws ReportException;

    /**
     * Retrieves a listing of completed services, including as many services
     * as have completed up to the provided limit.
     *
     * If the provided limit is 0 or less, or over 1000, the default limit
     * (currently 20) is used.
     *
     * The returned list is sorted based on update (completed/started) time,
     * with the most recent at the top of the list.
     *
     * @param limit the maximum number of services to be included in the list
     * @return list of the most recent completed services
     */
    public List<ServiceSummary> getCompletedServicesReport(int limit)
        throws ReportException;

    /**
     * Retrieves the list of all service report files which have been created.
     *
     * @return list of service report Ids
     */
    public List<String> getCompletedServicesReportList()
        throws ReportException;

    /**
     * Retrieves a specific service report by ID. Each service report will
     * contain a listing of all services which completed within a limited
     * time block.
     *
     * @param reportId
     * @return
     */
    public List<ServiceSummary> getCompletedServicesReport(String reportId)
        throws NotFoundException, ReportException;

}
