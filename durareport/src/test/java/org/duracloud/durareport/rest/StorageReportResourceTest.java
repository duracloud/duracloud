/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.durareport.storage.StorageReportBuilder;
import org.duracloud.durareport.storage.StorageReportHandler;
import org.duracloud.durareport.storage.StorageReportScheduler;
import org.duracloud.reportdata.storage.StorageReportList;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 5/25/11
 */
public class StorageReportResourceTest {

    ContentStoreManager mockStoreMgr;
    StorageReportHandler mockReportHandler;
    StorageReportBuilder mockReportBuilder;
    StorageReportScheduler mockReportScheduler;
    private String reportPrefix = "report/storage-report-";
    private String errorLogName = "report/error-log-storage-report";

    @Before
    public void setup() {
        mockStoreMgr = EasyMock.createMock(ContentStoreManager.class);
        mockReportHandler = EasyMock.createMock(StorageReportHandler.class);
        mockReportBuilder = EasyMock.createMock(StorageReportBuilder.class);
        mockReportScheduler = EasyMock.createMock(StorageReportScheduler.class);
    }

    private void replayMocks() {
        EasyMock.replay(mockStoreMgr,
                        mockReportHandler,
                        mockReportBuilder,
                        mockReportScheduler);
    }

    private StorageReportResource getResource() {
        StorageReportResource srResource =
            new StorageReportResource(reportPrefix, errorLogName);
        srResource.initialize(mockStoreMgr,
                              mockReportHandler,
                              mockReportBuilder,
                              mockReportScheduler);
        return srResource;
    }

    @After
    public void teardown() {
        EasyMock.verify(mockStoreMgr,
                        mockReportHandler,
                        mockReportBuilder,
                        mockReportScheduler);
    }

    @Test
    public void testDefaultSchedule() {
        StorageReportResource srResource = getResource();
        Date nextReportDate = srResource.getDefaultScheduleStartDate();
        assertNotNull(nextReportDate);
        Date now = new Date();
        assertTrue("Next report date should be after the current date",
                   nextReportDate.after(now));
        assertTrue("The next scheduled date should be within a week",
                   (nextReportDate.getTime() - now.getTime()) <= 604800000);

        replayMocks();
    }

    @Test
    public void testVerifyInitialized() throws Exception {
        StorageReportResource srResource =
            new StorageReportResource(reportPrefix, errorLogName);

        try {
            srResource.getLatestStorageReport();
            fail("Exception expected");
        } catch(RuntimeException expected) {
            assertNotNull(expected);
        }

        try {
            srResource.getStorageReportInfo();
            fail("Exception expected");
        } catch(RuntimeException expected) {
            assertNotNull(expected);
        }

        try {
            srResource.startStorageReport();
            fail("Exception expected");
        } catch(RuntimeException expected) {
            assertNotNull(expected);
        }

        replayMocks();
    }

    @Test
    public void testGetStorageReport() throws Exception {
        String reportId = "reportId";
        setUpMocksGetStorageReport(reportId);
        StorageReportResource srResource = getResource();

        InputStream stream = srResource.getStorageReport(reportId);
        assertNotNull(stream);
    }

    private void setUpMocksGetStorageReport(String reportId) throws Exception {
        EasyMock.expect(mockReportHandler.getStorageReportStream(reportId))
            .andReturn(new ByteArrayInputStream("test".getBytes()))
            .times(1);

        replayMocks();
    }

    @Test
    public void testGetLatestStorageReport() throws Exception {
        setUpMocksGetLatestStorageReport();
        StorageReportResource srResource = getResource();

        InputStream stream = srResource.getLatestStorageReport();
        assertNotNull(stream);
    }

    private void setUpMocksGetLatestStorageReport() throws Exception {
        EasyMock.expect(mockReportHandler.getLatestStorageReportStream())
            .andReturn(new ByteArrayInputStream("test".getBytes()))
            .times(1);

        replayMocks();
    }

    @Test
    public void testGetStorageReportList() throws Exception {
        setUpMocksGetStorageReportList();
        StorageReportResource srResource = getResource();

        String reportListXml = srResource.getStorageReportList();
        assertNotNull(reportListXml);
    }

    private void setUpMocksGetStorageReportList() throws Exception {
        List<String> reportList = new LinkedList<String>();
        reportList.add("reportId1");
        StorageReportList storageReportList = new StorageReportList(reportList);

        EasyMock.expect(mockReportHandler.getStorageReportList())
            .andReturn(storageReportList)
            .times(1);

        replayMocks();
    }

    @Test
    public void testGetStorageReportInfo() throws Exception {
        setUpMocksGetStorageReportInfo();
        StorageReportResource srResource = getResource();

        String info = srResource.getStorageReportInfo();
        assertNotNull(info);
    }

    private void setUpMocksGetStorageReportInfo() throws Exception {
        EasyMock.expect(mockReportBuilder.getStatus())
            .andReturn(StorageReportBuilder.Status.COMPLETE)
            .times(1);

        EasyMock.expect(mockReportBuilder.getStartTime())
            .andReturn(0L)
            .times(1);
        EasyMock.expect(mockReportBuilder.getStopTime())
            .andReturn(0L)
            .times(1);
        EasyMock.expect(mockReportBuilder.getElapsedTime())
            .andReturn(0L)
            .times(1);

        EasyMock.expect(mockReportBuilder.getCurrentCount())
            .andReturn(1L)
            .times(1);

        EasyMock.expect(mockReportScheduler.getNextScheduledStartDate())
            .andReturn(new Date())
            .times(1);

        replayMocks();
    }

    @Test
    public void testStartStorageReport() {
        setUpMockStartStorageReport();
        StorageReportResource srResource = getResource();
        srResource.startStorageReport();
    }

    private void setUpMockStartStorageReport() {
        EasyMock.expect(mockReportScheduler.startStorageReport())
            .andReturn(StorageReportBuilder.Status.COMPLETE.name())
            .times(1);

        replayMocks();
    }

    @Test
    public void testScheduleStorageReport() {
        setUpMockScheduleStorageReport();
        StorageReportResource srResource = getResource();

        long now = System.currentTimeMillis();
        try {
            srResource.scheduleStorageReport(now - 500, 1);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        try {
            srResource.scheduleStorageReport(now + 500, 1);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        srResource.scheduleStorageReport(now + 500, 1000000);
    }

    private void setUpMockScheduleStorageReport() {
        EasyMock.expect(
            mockReportScheduler.scheduleStorageReport(EasyMock.isA(Date.class),
                                                      EasyMock.anyLong()))
            .andReturn("success")
            .times(1);

        replayMocks();
    }

    @Test
    public void testCancelStorageReportSchedule() {
        setUpMockCancelStorageReportSchedule();
        StorageReportResource srResource = getResource();
        srResource.cancelStorageReportSchedule();
    }

    private void setUpMockCancelStorageReportSchedule() {
        EasyMock.expect(mockReportScheduler.cancelStorageReportSchedule())
            .andReturn("success")
            .times(1);

        replayMocks();
    }
}
