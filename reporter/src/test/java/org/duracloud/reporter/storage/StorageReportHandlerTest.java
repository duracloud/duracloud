/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reporter.storage;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.domain.Content;
import org.duracloud.reporter.storage.metrics.DuraStoreMetricsCollector;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 5/25/11
 */
public class StorageReportHandlerTest {

    private static String compMeta = StorageReportHandler.COMPLETION_TIME_META;
    private static String elapMeta = StorageReportHandler.ELAPSED_TIME_META;

    private String spaceId = "report-storage-space";
    private String reportContentId =
        "report/storage-report-2011-05-17T16:01:58.xml";
    private String reportContentIdRegex =
        "report/storage-report-2011-05-1[7-8]T[0-9][0-9]:[0,3]1:58.xml";
    private long completionTime = 1305662518734L;
    private long elapsedTime = 10;
    private String reportPrefix = "report/storage-report-";
    private String errorLogName = "report/error-log-storage-report";

    private ContentStore mockStore;
    private ContentStoreManager mockStoreMgr;

    @Before
    public void setup() {
        mockStore = EasyMock.createMock(ContentStore.class);
        mockStoreMgr = EasyMock.createMock(ContentStoreManager.class);
    }

    private void replayMocks() {
        EasyMock.replay(mockStore, mockStoreMgr);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockStore, mockStoreMgr);
    }

    @Test
    public void testStoreReport() throws Exception {
        Capture<Map<String, String>> propertiesCapture =
            new Capture<Map<String, String>>();

        StorageReportHandler handler = setUpMocksStoreReport();

        DuraStoreMetricsCollector metrics = new DuraStoreMetricsCollector();

        String contentId =
            handler.storeReport(metrics, completionTime, elapsedTime);
        assertNotNull(contentId);
        assertTrue(contentId, contentId.matches(reportContentIdRegex));
    }

    private StorageReportHandler setUpMocksStoreReport() throws Exception {
        String mimetype = "application/xml";
        EasyMock.expect(mockStore.addContent(EasyMock.eq(spaceId),
                                             EasyMock.isA(String.class),
                                             EasyMock.isA(InputStream.class),
                                             EasyMock.anyLong(),
                                             EasyMock.eq(mimetype),
                                             EasyMock.isA(String.class),
                                             EasyMock.<Map<String,String>>isNull()))
            .andReturn(null)
            .times(1);

        return setUpMockStore();
    }

    private StorageReportHandler setUpMockStore() throws Exception {
        EasyMock.expect(mockStore.getSpaceProperties(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        return setUpMockStoreMgr();
    }

    private StorageReportHandler setUpMockStoreMgr()
        throws Exception {
        EasyMock.expect(mockStoreMgr.getPrimaryContentStore())
            .andReturn(mockStore)
            .times(1);

        replayMocks();
        return new StorageReportHandler(mockStoreMgr,
                                        spaceId,
                                        reportPrefix,
                                        errorLogName);
    }

    @Test
    public void testGetStorageReport() throws Exception {
        testGetStorageReport(false);
    }

    @Test
    public void testGetStorageReportStream() throws Exception {
        testGetStorageReport(true);
    }

    private void testGetStorageReport(boolean stream) throws Exception {
        String reportId = "reportId";

        StorageReportHandler handler = setUpMocksGetStorageReport(reportId);

        StorageReport report = null;
        if(stream) {
            InputStream reportStream = handler.getStorageReportStream(reportId);
            assertNotNull(reportStream);
            StorageReportSerializer serializer = new StorageReportSerializer();
            report = serializer.deserialize(reportStream);
        } else {
            report = handler.getStorageReport(reportId);
        }

        assertNotNull(report);
        assertEquals(reportId, report.getReportId());
        assertEquals(completionTime, report.getCompletionTime());
        assertEquals(elapsedTime, report.getElapsedTime());
        assertNotNull(report.getStorageMetrics());
    }

    private StorageReportHandler setUpMocksGetStorageReport(String reportId)
        throws Exception {
        EasyMock.expect(mockStore.getSpaceProperties(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        EasyMock.expect(mockStore.getContent(EasyMock.eq(spaceId),
                                             EasyMock.eq(reportId)))
            .andReturn(getContent(reportId))
            .times(1);

        return setUpMockStoreMgr();
    }

    private Content getContent(String reportId) throws Exception {
        Content content = new Content();
        content.setId(reportId);

        StorageReportSerializer serializer = new StorageReportSerializer();
        StorageMetrics metrics = new StorageMetrics(null, 0, 0, null);
        StorageReport storageReport =
            new StorageReport(reportId, metrics, completionTime, elapsedTime);
        String xml = serializer.serialize(storageReport);
        content.setStream(new ByteArrayInputStream(xml.getBytes("UTF-8")));

        return content;
    }

    @Test
    public void testGetLatestStorageReport() throws Exception {
        testGetLatestStorageReport(false);
    }

    public void testGetLatestStorageReport(boolean stream) throws Exception {
        StorageReportHandler handler = setUpMocksGetLatestStorageReport();

        StorageReport report = null;
        if(stream) {
            InputStream reportStream = handler.getLatestStorageReportStream();
            assertNotNull(reportStream);
            StorageReportSerializer serializer = new StorageReportSerializer();
            report = serializer.deserialize(reportStream);
        } else {
            report = handler.getLatestStorageReport();
        }

        assertNotNull(report);
        assertEquals(reportContentId, report.getReportId());
        assertEquals(completionTime, report.getCompletionTime());
        assertEquals(elapsedTime, report.getElapsedTime());
        assertNotNull(report.getStorageMetrics());
    }

    private StorageReportHandler setUpMocksGetLatestStorageReport()
        throws Exception {
        EasyMock.expect(mockStore.getSpaceProperties(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        List<String> reports = new ArrayList<String>();
        reports.add(reportContentId);
        reports.add("report/storage-report-2011-05-01T16:01:58.xml"); // older
        EasyMock.expect(mockStore.getSpaceContents(EasyMock.eq(spaceId),
                                                   EasyMock.isA(String.class)))
            .andReturn(reports.iterator())
            .times(1);

        EasyMock.expect(mockStore.getContent(EasyMock.eq(spaceId),
                                             EasyMock.eq(reportContentId)))
            .andReturn(getContent(reportContentId))
            .times(1);

        return setUpMockStoreMgr();
    }

    @Test
    public void testGetStorageReportList() throws Exception {
        StorageReportHandler handler = setUpMocksGetStorageReportList();

        List<String> reportList =
            handler.getStorageReportList().getStorageReportList();
        assertNotNull(reportList);

        assertNotNull(reportList);
        assertEquals(3, reportList.size());
        assertTrue(reportList.contains(reportContentId));
    }

    private StorageReportHandler setUpMocksGetStorageReportList()
        throws Exception {
        EasyMock.expect(mockStore.getSpaceProperties(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        List<String> reports = new ArrayList<String>();
        reports.add(reportContentId);
        reports.add("report/storage-report-2011-05-01T16:01:58.xml");
        reports.add("report/storage-report-2010-02-01T19:21:43.xml");
        EasyMock.expect(mockStore.getSpaceContents(EasyMock.eq(spaceId),
                                                   EasyMock.isA(String.class)))
            .andReturn(reports.iterator())
            .times(1);

        return setUpMockStoreMgr();
    }

    @Test
    public void testAddToErrorLog() throws Exception {
        String errMsg = "Error Message";
        StorageReportHandler handler = setUpMockAddToErrorLog(errMsg);

        handler.addToErrorLog(errMsg);
    }

    private StorageReportHandler setUpMockAddToErrorLog(String errMsg)
        throws Exception {

        String existingLogContent = "Existing Error Message";
        InputStream existingStream =
            new ByteArrayInputStream(existingLogContent.getBytes("UTF-8"));
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ContentStore.CONTENT_SIZE,
                     String.valueOf(existingLogContent.length()));

        Content content = new Content();
        content.setId(errorLogName);
        content.setStream(existingStream);
        content.setProperties(properties);

        EasyMock.expect(mockStore.getContent(spaceId, errorLogName))
            .andReturn(content)
            .times(1);

        long totalSize = existingLogContent.length() + errMsg.length();
        EasyMock.expect(mockStore.addContent(EasyMock.eq(spaceId),
                                             EasyMock.eq(errorLogName),
                                             EasyMock.isA(SequenceInputStream.class),
                                             EasyMock.geq(totalSize),
                                             EasyMock.eq("text/plain"),
                                             EasyMock.<String>isNull(),
                                             EasyMock.<Map<String,String>>isNull()))
            .andReturn("success")
            .times(1);

        return setUpMockStore();
    }

}
