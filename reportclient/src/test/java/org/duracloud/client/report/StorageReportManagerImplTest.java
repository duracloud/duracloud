/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report;

import org.duracloud.client.report.error.ReportException;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportInfo;
import org.duracloud.reportdata.storage.StorageReportList;
import org.duracloud.reportdata.storage.serialize.StorageReportInfoSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportListSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 6/20/11
 */
public class StorageReportManagerImplTest extends ReportManagerTestBase {

    protected StorageReportManagerImpl reportManager;

    @Before
    @Override
    public void setup() {
        super.setup();
        reportManager = new StorageReportManagerImpl(host, port, context);
        reportManager.setRestHelper(mockRestHelper);
        setResponse("result");
    }

    private String getBaseUrl() {
        return baseUrl + "/storagereport";
    }

    @Test
    public void testGetLatestStorageReport() throws Exception {
        String id = "reportId";
        setStorageReportInResponse(id);

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(getBaseUrl())))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        StorageReport report = reportManager.getLatestStorageReport();
        assertNotNull(report);
        assertEquals(id, report.getReportId());
    }

    private void setStorageReportInResponse(String id) {
        StorageReportSerializer serializer = new StorageReportSerializer();
        String xmlReport =
            serializer.serialize(new StorageReport(id, null, 0L, 0L));
        setResponse(xmlReport);
    }

    @Test
    public void testGetStorageReportList() throws Exception {
        String url = getBaseUrl() + "/list";
        String listItem = "list-item";
        setStorageReportListInResponse(listItem);

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<String> reportList = reportManager.getStorageReportList();
        assertNotNull(reportList);
        assertTrue(reportList.contains(listItem));
    }

    private void setStorageReportListInResponse(String listItem) {
        StorageReportListSerializer serializer =
            new StorageReportListSerializer();
        List<String> listData = new ArrayList<String>();
        listData.add(listItem);
        StorageReportList reportList = new StorageReportList(listData);
        String xmlReport = serializer.serialize(reportList);
        setResponse(xmlReport);
    }

    @Test
    public void testGetStorageReportListEmpty() throws Exception {
        String url = getBaseUrl() + "/list";
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
        List<String> listData = new ArrayList<String>();
        StorageReportList reportList = new StorageReportList(listData);
        String xmlReport = serializer.serialize(reportList);
        setResponse(xmlReport);
    }

    @Test
    public void testGetStorageReport() throws Exception {
        String id = "report/reportId";
        String url = getBaseUrl() + "/" + id;
        setStorageReportInResponse(id);

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        StorageReport report = reportManager.getStorageReport(id);
        assertNotNull(report);
        assertEquals(id, report.getReportId());
    }

    @Test
    public void testGetStorageReportInfo() throws Exception {
        String url = getBaseUrl() + "/info";
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
            serializer.serialize(new StorageReportInfo());
        setResponse(xmlReport);
    }

    @Test
    public void testStartStorageReport() throws Exception {
        EasyMock.expect(
            mockRestHelper.post(EasyMock.eq(getBaseUrl()),
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
        EasyMock.expect(mockRestHelper.delete(EasyMock.eq(getBaseUrl())))
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
        String url = getBaseUrl() + "/schedule?startTime=" + time.getTime() +
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
        String url = getBaseUrl() + "/schedule";

        EasyMock.expect(mockRestHelper.delete(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        String success = reportManager.cancelStorageReportSchedule();
        assertNotNull(success);
    }

}
