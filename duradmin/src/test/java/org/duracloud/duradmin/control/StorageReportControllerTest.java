/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;


import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.duracloud.client.report.StorageReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.Credential;
import org.duracloud.duradmin.test.AbstractTestBase;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class StorageReportControllerTest extends AbstractTestBase {
    private StorageReportController controller;
    private StorageReportManager storageReportManager;
    private StorageSummaryCache storageSummaryCache;

    @Override
    @Before
    public void setup() {
        super.setup();
        storageReportManager  = createMock(StorageReportManager.class);
        storageSummaryCache = createMock(StorageSummaryCache.class);
        storageReportManager.login(EasyMock.isA(Credential.class));
        EasyMock.expectLastCall();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testGetStorageReportSummaries() throws ReportException, NotFoundException, ParseException {
        List<StorageSummary> list = new LinkedList<StorageSummary>();
        EasyMock.expect(storageSummaryCache.getSummaries(EasyMock.isA(String.class),
                                                         EasyMock.isA(String.class))).andReturn(list);

        replay();
        controller = new StorageReportController(storageReportManager, storageSummaryCache);
        ModelAndView mav = controller.getStorageReportSummaries("test","test");
        Assert.assertEquals(list, mav.getModel().get("summaries"));
    }

    @Test
    public void testGetDetail() throws Exception{
        StorageReport report = createMock(StorageReport.class);
        EasyMock.expect(report.getReportId()).andReturn("reportspace/reportId");
        report.getStorageMetrics();
        List<StorageProviderMetrics> providerMetrics = new LinkedList<StorageProviderMetrics>();
        StorageMetrics sm = new StorageMetrics(providerMetrics, 0, 0, null);
        EasyMock.expectLastCall().andReturn(sm);
        EasyMock.expect(storageReportManager.getStorageReport(EasyMock.isA(String.class))).andReturn(report);
        replay();
        controller = new StorageReportController(storageReportManager, storageSummaryCache);
        ModelAndView mav = controller.getDetail("test","test");
        Assert.assertTrue(mav.getModel().containsKey("metrics"));
        Assert.assertTrue(mav.getModel().containsKey("reportId"));
    }

}
