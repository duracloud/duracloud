/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report;

import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportInfo;
import org.duracloud.reportdata.storage.serialize.StorageReportInfoSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportListSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 6/20/11
 */
public class StorageReportManagerTest {

    private RestHttpHelper mockRestHelper;
    private String host = "host";
    private String port = "8080";
    private String context = "context";
    private StorageReportManagerImpl reportManager;
    private String baseUrl = "http://host:8080/context/storagereport";
    private RestHttpHelper.HttpResponse successResponse;

    @Before
    public void setup() {
        mockRestHelper = EasyMock.createMock(RestHttpHelper.class);
        reportManager = new StorageReportManagerImpl(host, port, context);
        reportManager.setRestHelper(mockRestHelper);

        setResponse("result");
    }

    private void setResponse(String value) {
        InputStream stream = new ByteArrayInputStream(value.getBytes());
        successResponse =
            new RestHttpHelper.HttpResponse(200, null, null, stream);
    }

    private void replayMocks() {
        EasyMock.replay(mockRestHelper);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockRestHelper);
    }

    @Test
    public void testGetLatestStorageReport() throws Exception {
        String id = "reportId";
        setStorageReportInResponse(id);

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(baseUrl)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        StorageReport report = reportManager.getLatestStorageReport();
        assertNotNull(report);
        assertEquals(id, report.getContentId());
    }

    private void setStorageReportInResponse(String id) {
        StorageReportSerializer serializer = new StorageReportSerializer();
        String xmlReport =
            serializer.serializeReport(new StorageReport(id, null, 0L, 0L));
        setResponse(xmlReport);
    }

    @Test
    public void testGetStorageReportList() throws Exception {
        String url = baseUrl + "/list";
        setStorageReportListInResponse();

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<String> reportList = reportManager.getStorageReportList();
        assertNotNull(reportList);
    }

    private void setStorageReportListInResponse() {
        StorageReportListSerializer serializer =
            new StorageReportListSerializer();
        String xmlReport =
            serializer.serializeReportList(new ArrayList<String>());
        setResponse(xmlReport);
    }

    @Test
    public void testGetStorageReport() throws Exception {
        String id = "report/reportId";
        String url = baseUrl + "/" + id;
        setStorageReportInResponse(id);

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        StorageReport report = reportManager.getStorageReport(id);
        assertNotNull(report);
        assertEquals(id, report.getContentId());
    }

    @Test
    public void testGetStorageReportInfo() throws Exception {
        String url = baseUrl + "/info";
        setStorageReportInfoInResponse();

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        StorageReportInfo reportInfo = reportManager.getStorageReportInfo();
        assertNotNull(reportInfo);
    }

    private void setStorageReportInfoInResponse() {
        StorageReportInfoSerializer serializer =
            new StorageReportInfoSerializer();
        String xmlReport =
            serializer.serializeReportInfo(new StorageReportInfo());
        setResponse(xmlReport);
    }

    @Test
    public void testStartStorageReport() throws Exception {
        EasyMock.expect(
            mockRestHelper.post(EasyMock.eq(baseUrl),
                                EasyMock.<String>isNull(),
                                EasyMock.<Map<String,String>>isNull()))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        String success = reportManager.startStorageReport();
        assertNotNull(success);
    }

    @Test
    public void testCancelStorageReport() throws Exception {
        EasyMock.expect(mockRestHelper.delete(EasyMock.eq(baseUrl)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        String success = reportManager.cancelStorageReport();
        assertNotNull(success);
    }

    @Test
    public void testScheduleStorageReport() throws Exception {
        long millis = 5000000;
        Date time = new Date(System.currentTimeMillis() + millis);
        String url = baseUrl + "/schedule?startTime=" + time.getTime() +
                     "&frequency=" +  millis;

        EasyMock.expect(
            mockRestHelper.post(EasyMock.eq(url),
                                EasyMock.<String>isNull(),
                                EasyMock.<Map<String,String>>isNull()))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        // Invalid start time
        try {
            reportManager.scheduleStorageReport(
                new Date(System.currentTimeMillis() - 5000), 0);
            fail("Exception expected");
        } catch(ReportException expected) {
            assertNotNull(expected);
        }

        // Invalid frequency
        try {
            reportManager.scheduleStorageReport(
                new Date(System.currentTimeMillis() + 5000), 0);
            fail("Exception expected");
        } catch(ReportException expected) {
            assertNotNull(expected);
        }

        // Valid call
        String success = reportManager.scheduleStorageReport(time, millis);
        assertNotNull(success);
    }

    @Test
    public void testCancelStorageReportSchedule() throws Exception {
        String url = baseUrl + "/schedule";

        EasyMock.expect(mockRestHelper.delete(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        String success = reportManager.cancelStorageReportSchedule();
        assertNotNull(success);
    }

}
