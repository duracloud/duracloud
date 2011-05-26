/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.durareport.storage.StorageReport;
import org.duracloud.durareport.storage.StorageReportBuilder;
import org.duracloud.durareport.storage.StorageReportHandler;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 5/25/11
 */
public class StorageReportResourceTest {

    ContentStoreManager mockStoreMgr;
    StorageReportHandler mockReportHandler;
    StorageReportBuilder mockReportBuilder;

    @Before
    public void setup() {
        mockStoreMgr = EasyMock.createMock(ContentStoreManager.class);
        mockReportHandler = EasyMock.createMock(StorageReportHandler.class);
        mockReportBuilder = EasyMock.createMock(StorageReportBuilder.class);
    }

    private void replayMocks() {
        EasyMock.replay(mockStoreMgr, mockReportHandler, mockReportBuilder);
    }

    private StorageReportResource getResource() {
        StorageReportResource srResource = new StorageReportResource();
        srResource.initialize(mockStoreMgr,
                              mockReportHandler,
                              mockReportBuilder);
        return srResource;
    }

    @After
    public void teardown() {
        EasyMock.verify(mockStoreMgr, mockReportHandler, mockReportBuilder);
    }

    @Test
    public void testVerifyInitialized() throws Exception {
        StorageReportResource srResource = new StorageReportResource();

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
        ByteArrayInputStream stream =
            new ByteArrayInputStream("test".getBytes());
        StorageReport report = new StorageReport(reportId, stream, 0, 0);
        EasyMock.expect(mockReportHandler.getStorageReport(reportId))
            .andReturn(report)
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
        ByteArrayInputStream stream =
            new ByteArrayInputStream("test".getBytes());
        StorageReport report = new StorageReport("contentId", stream, 0, 0);
        EasyMock.expect(mockReportHandler.getLatestStorageReport())
            .andReturn(report)
            .times(1);

        replayMocks();
    }

    @Test
    public void testGetStorageReportList() throws Exception {
        setUpMocksGetStorageReportList();
        StorageReportResource srResource = getResource();

        InputStream stream = srResource.getStorageReportList();
        assertNotNull(stream);
    }

    private void setUpMocksGetStorageReportList() throws Exception {
        ByteArrayInputStream stream =
            new ByteArrayInputStream("test".getBytes());
        EasyMock.expect(mockReportHandler.getStorageReportList())
            .andReturn(stream)
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

        replayMocks();
    }

    @Test
    public void testStartStorageReport() {
        setUpMockStartStorageReport();
        StorageReportResource srResource = getResource();
        srResource.startStorageReport();
    }

    private void setUpMockStartStorageReport() {
        EasyMock.expect(mockReportBuilder.getStatus())
            .andReturn(StorageReportBuilder.Status.COMPLETE)
            .times(1);

        mockReportBuilder.run();
        EasyMock.expectLastCall()
            .anyTimes();

        EasyMock.makeThreadSafe(mockReportBuilder, true);
        replayMocks();
    }

}
