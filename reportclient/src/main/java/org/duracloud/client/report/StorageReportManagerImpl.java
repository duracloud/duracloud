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
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportInfo;
import org.duracloud.reportdata.storage.serialize.StorageReportInfoSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportListSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Allows for communication with DuraReport
 *
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportManagerImpl extends BaseReportManager implements StorageReportManager {

    public StorageReportManagerImpl(String host, String port) {
        super(host, port);
    }

    public StorageReportManagerImpl(String host, String port, String context) {
        super(host, port, context);
    }

    private String buildURL(String relativeURL) {
        String storageReport = "storagereport";
        if (null == relativeURL) {
            return getBaseURL() + "/" + storageReport;
        }
        return getBaseURL() + "/" + storageReport + "/" + relativeURL;
    }

    private String buildBaseStorageReportURL() {
        return buildURL(null);
    }

    private String buildGetStorageReportListURL() {
        return buildURL("list");
    }

    private String buildGetStorageReportURL(String reportId) {
        return buildURL(reportId);
    }

    private String buildGetStorageReportInfoURL() {
        return buildURL("info");
    }

    private String buildScheduleStorageReportURL(long startTime,
                                                 long frequency) {
        String baseUrl = buildURL("schedule");
        return baseUrl + "?startTime=" + startTime + "&frequency=" + frequency;
    }

    private String buildCancelStorageReportScheduleURL() {
        return buildURL("schedule");
    }

    @Override
    public StorageReport getLatestStorageReport()
        throws NotFoundException, ReportException {
        String url = buildBaseStorageReportURL();
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream reportXml = response.getResponseStream();

            StorageReportSerializer serializer = new StorageReportSerializer();
            return serializer.deserialize(reportXml);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            String error = "Could not get latest storage report due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public List<String> getStorageReportList() throws ReportException {
        String url = buildGetStorageReportListURL();
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream listXml = response.getResponseStream();

            StorageReportListSerializer serializer =
                new StorageReportListSerializer();
            return serializer.deserialize(listXml).getStorageReportList();
        } catch (Exception e) {
            String error = "Could not get storage report list due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public StorageReport getStorageReport(String reportId)
        throws NotFoundException, ReportException {
        String url = buildGetStorageReportURL(reportId);
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream reportXml = response.getResponseStream();

            StorageReportSerializer serializer = new StorageReportSerializer();
            return serializer.deserialize(reportXml);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            String error = "Could not get storage report with ID " + reportId +
                           " due to: " + e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public StorageReportInfo getStorageReportInfo() throws ReportException {
        String url = buildGetStorageReportInfoURL();
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream reportInfoXml = response.getResponseStream();

            StorageReportInfoSerializer serializer =
                new StorageReportInfoSerializer();
            return serializer.deserialize(reportInfoXml);
        } catch (Exception e) {
            String error = "Could not get storage report info due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public String startStorageReport() throws ReportException {
        String url = buildBaseStorageReportURL();
        try {
            RestHttpHelper.HttpResponse response = getRestHelper()
                .post(url, null, null);

            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseBody();
        } catch (Exception e) {
            String error = "Could not start storage report due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public String cancelStorageReport() throws ReportException {
        String url = buildBaseStorageReportURL();
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().delete(url);
            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseBody();
        } catch (Exception e) {
            String error = "Could not cancel storage report due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public String scheduleStorageReport(Date startTime, long frequency)
        throws ReportException {

        if(null == startTime) {
            throw new ReportException("Start time may not be null");
        }
        if(startTime.before(new Date())) {
            throw new ReportException("Start time must be in the future");
        }
        if(frequency < 600000) {
            throw new ReportException("Frequency must be higher than " +
                                      "600000 milliseconds (10 minutes)");
        }

        String url =
            buildScheduleStorageReportURL(startTime.getTime(), frequency);
        try {
            RestHttpHelper.HttpResponse response = getRestHelper()
                .post(url, null, null);

            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseBody();
        } catch (Exception e) {
            String error = "Could not schedule storage report due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

    @Override
    public String cancelStorageReportSchedule() throws ReportException {
        String url = buildCancelStorageReportScheduleURL();
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().delete(url);

            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseBody();
        } catch (Exception e) {
            String error = "Could not cancel storage report schedule due to: " +
                           e.getMessage();
            throw new ReportException(error, e);
        }
    }

}

