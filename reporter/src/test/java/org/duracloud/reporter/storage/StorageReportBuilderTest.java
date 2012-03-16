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
import org.duracloud.reporter.storage.metrics.DuraStoreMetricsCollector;
import org.duracloud.reporter.storage.metrics.MimetypeMetricsCollector;
import org.duracloud.reporter.storage.metrics.SpaceMetricsCollector;
import org.duracloud.reporter.storage.metrics.StorageProviderMetricsCollector;
import org.duracloud.reportdata.storage.StorageReport;
import org.easymock.Capture;
import org.easymock.IAnswer;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 5/24/11
 */
public class StorageReportBuilderTest {

     private String storeId = "storeId";
     private String storeType = "storeType";

    @Test
    public void testRunBuilder() throws Exception {
        ContentStore mockStore = createMockStore();
        ContentStoreManager mockStoreMgr = createMockStoreMgr(mockStore);

        Capture<DuraStoreMetricsCollector> metricsCapture =
            new Capture<DuraStoreMetricsCollector>();
        StorageReportHandler mockReportHandler =
            createMockReportHandler(metricsCapture);

        StorageReportBuilder builder =
            new StorageReportBuilder(mockStoreMgr, mockReportHandler);
        builder.run();

        StorageReportBuilder.Status status = builder.getStatus();
        assertNotNull(status);
        assertEquals(StorageReportBuilder.Status.COMPLETE, status);

        assertNull(builder.getError());

        assertEquals(9, builder.getCurrentCount());

        long startTime = builder.getStartTime();
        long stopTime = builder.getStopTime();
        long elapsedTime = builder.getElapsedTime();
        assertTrue(stopTime > 0);
        assertTrue(startTime > 0);
        assertTrue(elapsedTime < startTime);
        assertTrue(startTime <= stopTime);

        DuraStoreMetricsCollector metrics = metricsCapture.getValue();
        assertNotNull(metrics);
        assertEquals(9, metrics.getTotalItems());
        assertEquals(900, metrics.getTotalSize());

        Collection<StorageProviderMetricsCollector> spMetrics =
            metrics.getStorageProviderMetrics().values();
        assertNotNull(spMetrics);
        assertEquals(1, spMetrics.size());
        for(StorageProviderMetricsCollector spMetric : spMetrics) {
            assertEquals(storeId, spMetric.getStorageProviderId());
            assertEquals(storeType, spMetric.getStorageProviderType());
            assertEquals(9, spMetric.getTotalItems());
            assertEquals(900, spMetric.getTotalSize());

            Collection<SpaceMetricsCollector> spaceMetrics =
                spMetric.getSpaceMetrics().values();
            assertNotNull(spaceMetrics);
            assertEquals(3, spaceMetrics.size());
            for(SpaceMetricsCollector spaceMetric : spaceMetrics) {
                assertEquals(3, spaceMetric.getTotalItems());
                assertEquals(300, spaceMetric.getTotalSize());

                Collection<MimetypeMetricsCollector> mimeMetrics =
                    spaceMetric.getMimetypeMetrics().values();
                assertNotNull(mimeMetrics);
                assertEquals(1, mimeMetrics.size());
            }
        }

        EasyMock.verify(mockStore, mockStoreMgr, mockReportHandler);
    }

    private ContentStore createMockStore() throws Exception {
        ContentStore mockStore = EasyMock.createMock(ContentStore.class);

        EasyMock.expect(mockStore.getStoreId())
            .andReturn(storeId)
            .times(1);
        EasyMock.expect(mockStore.getStorageProviderType())
            .andReturn(storeType)
            .times(1);

        List<String> spaces = new ArrayList<String>();
        spaces.add("space1");
        spaces.add("space2");
        spaces.add("space3");
        EasyMock.expect(mockStore.getSpaces())
            .andReturn(spaces)
            .times(1);

        EasyMock.expect(mockStore.getSpaceContents(EasyMock.isA(String.class)))
            .andAnswer(new SpaceContents())
            .times(3);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ContentStore.CONTENT_MIMETYPE, "text/plain");
        properties.put(ContentStore.CONTENT_SIZE, "100");
        EasyMock.expect(
            mockStore.getContentProperties(EasyMock.isA(String.class),
                                           EasyMock.isA(String.class)))
            .andReturn(properties)
            .times(9);

        EasyMock.replay(mockStore);
        return mockStore;
    }

    private class SpaceContents implements IAnswer<Iterator<String>> {
        @Override
        public Iterator<String> answer() throws Exception {
            List<String> contents = new ArrayList<String>();
            contents.add("item1");
            contents.add("item2");
            contents.add("item3");
            return contents.iterator();
        }
    }

    private ContentStoreManager createMockStoreMgr(ContentStore mockStore)
        throws Exception {
        ContentStoreManager mockStoreMgr =
            EasyMock.createMock(ContentStoreManager.class);

        Map<String, ContentStore> stores = new HashMap<String, ContentStore>();
        stores.put("0", mockStore);
        EasyMock.expect(mockStoreMgr.getContentStores())
            .andReturn(stores)
            .times(1);

        EasyMock.replay(mockStoreMgr);
        return mockStoreMgr;
    }

    private StorageReportHandler createMockReportHandler(
        Capture<DuraStoreMetricsCollector> metricsCapture)
        throws Exception {

        StorageReportHandler mockHandler =
            EasyMock.createMock(StorageReportHandler.class);

        StorageReport report = new StorageReport("contentId", null, 2, 1);
        EasyMock.expect(mockHandler.getLatestStorageReport())
            .andReturn(report)
            .times(1);

        EasyMock.expect(
            mockHandler.storeReport(EasyMock.capture(metricsCapture),
                                    EasyMock.anyLong(),
                                    EasyMock.anyLong()))
            .andReturn("0")
            .times(1);

        EasyMock.replay(mockHandler);
        return mockHandler;
    }
}
