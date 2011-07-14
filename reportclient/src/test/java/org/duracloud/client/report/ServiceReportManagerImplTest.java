/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report;

import org.duracloud.serviceconfig.ServiceReportList;
import org.duracloud.serviceconfig.ServiceSummariesDocument;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.serviceconfig.serialize.ServiceReportListSerializer;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 6/30/11
 */
public class ServiceReportManagerImplTest extends ReportManagerTestBase {

    private ServiceReportManagerImpl reportManager;
    private int reportId = 25;

    @Before
    @Override
    public void setup() {
        super.setup();
        reportManager = new ServiceReportManagerImpl(host, port, context);
        reportManager.setRestHelper(mockRestHelper);

        List<ServiceSummary> summaryList = new ArrayList<ServiceSummary>();
        ServiceSummary summary = new ServiceSummary();
        summary.setId(reportId);
        summaryList.add(summary);
        String xmlReport =
            ServiceSummariesDocument.getServiceSummaryListAsXML(summaryList);
        setResponse(xmlReport);
    }

    private String getBaseUrl() {
        return baseUrl + "/servicereport";
    }

    @Test
    public void testGetDeployedServicesReport() throws Exception {
        String url = getBaseUrl() + "/deployed";

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<ServiceSummary> summaries =
            reportManager.getDeployedServicesReport();
        assertNotNull(summaries);
        assertEquals(reportId, summaries.get(0).getId());
    }

    @Test
    public void testGetCompletedServicesReport() throws Exception {
        String url = getBaseUrl();

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<ServiceSummary> summaries =
            reportManager.getCompletedServicesReport();
        assertNotNull(summaries);
        assertEquals(reportId, summaries.get(0).getId());
    }

    @Test
    public void testGetCompletedServicesReportLimited() throws Exception {
        int limit = 50;
        String url = getBaseUrl() + "?limit=" + limit;

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<ServiceSummary> summaries =
            reportManager.getCompletedServicesReport(limit);
        assertNotNull(summaries);
        assertEquals(reportId, summaries.get(0).getId());
    }

    @Test
    public void testGetCompletedServicesReportList() throws Exception {
        String url = getBaseUrl() + "/list";
        String reportId = "reportId";
        setServiceReportListInResponse(reportId);

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<String> reportList =
            reportManager.getCompletedServicesReportList();
        assertNotNull(reportList);
        assertEquals(reportId, reportList.iterator().next());
    }

    private void setServiceReportListInResponse(String reportId) {
        List<String> reportIds = new ArrayList<String>();
        reportIds.add(reportId);
        ServiceReportList reportList = new ServiceReportList(reportIds);

        String xmlReport =
            new ServiceReportListSerializer().serialize(reportList);
        setResponse(xmlReport);
    }

    @Test
    public void testGetCompletedServicesReportListEmpty() throws Exception {
        String url = getBaseUrl() + "/list";
        setServiceReportListInResponse();

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<String> reportList =
            reportManager.getCompletedServicesReportList();
        assertNotNull(reportList);
    }

    private void setServiceReportListInResponse() {
        ServiceReportList reportList =
            new ServiceReportList(new ArrayList<String>());
        String xmlReport =
            new ServiceReportListSerializer().serialize(reportList);
        setResponse(xmlReport);
    }

    @Test
    public void testGetCompletedServicesReportById() throws Exception {
        String contentId = "report/service-report-123.xml";
        String url = getBaseUrl() + "/" + contentId;

        EasyMock.expect(mockRestHelper.get(EasyMock.eq(url)))
            .andReturn(successResponse)
            .times(1);

        replayMocks();

        List<ServiceSummary> summaries =
            reportManager.getCompletedServicesReport(contentId);
        assertNotNull(summaries);
        assertEquals(reportId, summaries.get(0).getId());
    }

}
