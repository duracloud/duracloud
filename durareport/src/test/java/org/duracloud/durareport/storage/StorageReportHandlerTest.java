/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.domain.Content;
import org.duracloud.durareport.storage.metrics.DuraStoreMetrics;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import java.io.InputStream;
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

    private static String spaceId = StorageReportHandler.storageSpace;
    private static String compMeta = StorageReportHandler.COMPLETION_TIME_META;
    private static String elapMeta = StorageReportHandler.ELAPSED_TIME_META;
    
    private String reportContentId = "storage-report-2011-05-17T16:01:58.xml";
    private String reportContentIdRegex =
        "storage-report-2011-05-1[7-8]T[0-9][0-9]:[0,3]1:58.xml";
    private long completionTime = 1305662518734L;
    private long elapsedTime = 10;

    @Test
    public void testStoreReport() throws Exception {
        Capture<Map<String, String>> metadataCapture =
            new Capture<Map<String, String>>();

        ContentStore mockStore = createMockStoreStoreReport(metadataCapture);
        ContentStoreManager mockStoreMgr = createMockStoreMgr(mockStore);

        StorageReportHandler handler = new StorageReportHandler(mockStoreMgr);

        DuraStoreMetrics metrics = new DuraStoreMetrics();

        String contentId =
            handler.storeReport(metrics, completionTime, elapsedTime);
        assertNotNull(contentId);
        assertTrue(contentId, contentId.matches(reportContentIdRegex));

        Map<String, String> metadata = metadataCapture.getValue();
        assertNotNull(metadata);
        assertEquals(2, metadata.size());
        assertEquals(String.valueOf(completionTime), metadata.get(compMeta));
        assertEquals(String.valueOf(elapsedTime), metadata.get(elapMeta));

        EasyMock.verify(mockStore, mockStoreMgr);
    }

    private ContentStore createMockStoreStoreReport(
        Capture<Map<String, String>> metadataCapture) throws Exception {
        ContentStore mockStore = EasyMock.createMock(ContentStore.class);

        EasyMock.expect(mockStore.getSpaceMetadata(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        String mimetype = "application/xml";
        EasyMock.expect(mockStore.addContent(EasyMock.eq(spaceId),
                                             EasyMock.isA(String.class),
                                             EasyMock.isA(InputStream.class),
                                             EasyMock.anyLong(),
                                             EasyMock.eq(mimetype),
                                             EasyMock.isA(String.class),
                                             EasyMock.capture(metadataCapture)))
            .andReturn(null)
            .times(1);

        EasyMock.replay(mockStore);
        return mockStore;
    }

    private ContentStoreManager createMockStoreMgr(ContentStore mockStore)
        throws Exception {
        ContentStoreManager mockStoreMgr =
            EasyMock.createMock(ContentStoreManager.class);

        EasyMock.expect(mockStoreMgr.getPrimaryContentStore())
            .andReturn(mockStore)
            .times(1);

        EasyMock.replay(mockStoreMgr);
        return mockStoreMgr;
    }

    @Test
    public void testGetStorageReport() throws Exception {
        String reportId = "reportId";

        ContentStore mockStore = createMockStoreGetStorageReport(reportId);
        ContentStoreManager mockStoreMgr = createMockStoreMgr(mockStore);

        StorageReportHandler handler = new StorageReportHandler(mockStoreMgr);


        StorageReport report = handler.getStorageReport(reportId);
        assertNotNull(report);
        assertEquals(reportId,
                     report.getContentId());
        assertEquals(completionTime, report.getCompletionTime());
        assertEquals(elapsedTime, report.getElapsedTime());

        EasyMock.verify(mockStore, mockStoreMgr);
    }

    private ContentStore createMockStoreGetStorageReport(String reportId)
        throws Exception {
        ContentStore mockStore = EasyMock.createMock(ContentStore.class);

        EasyMock.expect(mockStore.getSpaceMetadata(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        Content content = new Content();
        content.setId(reportId);
        Map<String, String> contentMeta = new HashMap<String, String>();
        contentMeta.put(compMeta, String.valueOf(completionTime));
        contentMeta.put(elapMeta, String.valueOf(elapsedTime));
        content.setMetadata(contentMeta);
        EasyMock.expect(mockStore.getContent(EasyMock.eq(spaceId),
                                             EasyMock.eq(reportId)))
            .andReturn(content)
            .times(1);

        EasyMock.replay(mockStore);
        return mockStore;
    }

    @Test
    public void testGetLatestStorageReport() throws Exception {
        ContentStore mockStore = createMockStoreGetLatestStorageReport();
        ContentStoreManager mockStoreMgr = createMockStoreMgr(mockStore);

        StorageReportHandler handler = new StorageReportHandler(mockStoreMgr);

        StorageReport report = handler.getLatestStorageReport();
        assertNotNull(report);
        assertEquals(reportContentId,
                     report.getContentId());
        assertEquals(completionTime, report.getCompletionTime());
        assertEquals(elapsedTime, report.getElapsedTime());

        EasyMock.verify(mockStore, mockStoreMgr);
    }

    private ContentStore createMockStoreGetLatestStorageReport()
        throws Exception {
        ContentStore mockStore = EasyMock.createMock(ContentStore.class);

        EasyMock.expect(mockStore.getSpaceMetadata(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        List<String> reports = new ArrayList<String>();
        reports.add(reportContentId);
        reports.add("storage-report-2011-05-01T16:01:58.xml"); // older
        EasyMock.expect(mockStore.getSpaceContents(EasyMock.eq(spaceId),
                                                   EasyMock.isA(String.class)))
            .andReturn(reports.iterator())
            .times(1);

        Content content = new Content();
        content.setId(reportContentId);
        Map<String, String> contentMeta = new HashMap<String, String>();
        contentMeta.put(compMeta, String.valueOf(completionTime));
        contentMeta.put(elapMeta, String.valueOf(elapsedTime));
        content.setMetadata(contentMeta);
        EasyMock.expect(mockStore.getContent(EasyMock.eq(spaceId),
                                             EasyMock.eq(reportContentId)))
            .andReturn(content)
            .times(1);

        EasyMock.replay(mockStore);
        return mockStore;
    }

    @Test
    public void testGetStorageReportList() throws Exception {
        ContentStore mockStore = createMockStoreGetStorageReportList();
        ContentStoreManager mockStoreMgr = createMockStoreMgr(mockStore);

        StorageReportHandler handler = new StorageReportHandler(mockStoreMgr);

        InputStream stream = handler.getStorageReportList();
        assertNotNull(stream);

        String xml = IOUtils.toString(stream, "UTF-8");
        List<String> reportList = SerializationUtil.deserializeList(xml);
        assertNotNull(reportList);
        assertEquals(3, reportList.size());
        assertTrue(reportList.contains(reportContentId));

        EasyMock.verify(mockStore, mockStoreMgr);
    }

    private ContentStore createMockStoreGetStorageReportList()
        throws Exception {
        ContentStore mockStore = EasyMock.createMock(ContentStore.class);

        EasyMock.expect(mockStore.getSpaceMetadata(EasyMock.isA(String.class)))
            .andReturn(null)
            .times(1);

        List<String> reports = new ArrayList<String>();
        reports.add(reportContentId);
        reports.add("storage-report-2011-05-01T16:01:58.xml");
        reports.add("storage-report-2010-02-01T19:21:43.xml");
        EasyMock.expect(mockStore.getSpaceContents(EasyMock.eq(spaceId),
                                                   EasyMock.isA(String.class)))
            .andReturn(reports.iterator())
            .times(1);

        EasyMock.replay(mockStore);
        return mockStore;
    }

}
