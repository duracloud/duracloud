/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.serviceconfig.ServiceReportList;
import org.duracloud.serviceconfig.ServiceSummariesDocument;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.serviceconfig.serialize.ServiceReportListSerializer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 6/30/11
 */
public class ServiceReportManagerImpl extends BaseReportManager implements ServiceReportManager {

    public ServiceReportManagerImpl(String host, String port) {
        super(host, port);
    }

    public ServiceReportManagerImpl(String host, String port, String context) {
        super(host, port, context);
    }

    private String buildURL(String relativeURL) {
        String storageReport = "report/service";
        if (null == relativeURL) {
            return getBaseURL() + "/" + storageReport;
        }
        return getBaseURL() + "/" + storageReport + "/" + relativeURL;
    }

    private String buildBaseServicesReportURL() {
        return buildURL(null);
    }

    private String buildGetServicesReportListURL() {
        return buildURL("list");
    }

    private String buildGetCompletedServicesReportURL(int limit) {
        return buildBaseServicesReportURL() + "?limit=" + limit;
    }

    private String buildGetCompletedServicesReportURL(String reportId) {
        return buildURL(reportId);
    }

    @Override
    public List<ServiceSummary> getDeployedServicesReport()
        throws ReportException {
        String url = buildURL("deployed");
        String err = "Could not get deployed service report due to: ";
        return runGetServiceSummaryList(url, err);
    }

    private List<ServiceSummary> runGetServiceSummaryList(String url,
                                                          String err)
        throws ReportException {
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream reportXml = response.getResponseStream();
            return ServiceSummariesDocument.getServiceSummaryList(reportXml);
        } catch (Exception e) {
            String error = err + e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public List<ServiceSummary> getCompletedServicesReport()
        throws ReportException {
        String url = buildBaseServicesReportURL();
        String err = "Could not get completed service reports due to: ";
        return runGetServiceSummaryList(url, err);
    }

    @Override
    public List<ServiceSummary> getCompletedServicesReport(int limit)
        throws ReportException {
        String url = buildGetCompletedServicesReportURL(limit);
        String err = "Could not get completed service reports (with " +
                     "limit" + limit + ") due to: ";
        return runGetServiceSummaryList(url, err);
    }

    @Override
    public List<String> getCompletedServicesReportList()
        throws ReportException {
        String url = buildGetServicesReportListURL();
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String listXml = response.getResponseBody();

            ServiceReportList reportList =
                new ServiceReportListSerializer().deserialize(listXml);
            List<String> reportIds = reportList.getServiceReportList();

            if(null == reportIds) {
                reportIds = new ArrayList<String>();
            }
            return reportIds;
        } catch (Exception e) {
            String error = "Could not get service report list due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public List<ServiceSummary> getCompletedServicesReport(String reportId)
        throws NotFoundException, ReportException  {
        String url = buildGetCompletedServicesReportURL(reportId);
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream reportXml = response.getResponseStream();
            return ServiceSummariesDocument.getServiceSummaryList(reportXml);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            String error = "Could not get service report with ID " + reportId +
                           " due to: " + e.getMessage();
            throw new ReportException(error, e);
        }
    }

}
