/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.domain.Content;
import org.duracloud.durareport.error.ReportBuilderException;
import org.duracloud.durareport.storage.metrics.DuraStoreMetrics;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Handles the storage and retrieval of storage reports.
 *
 * @author: Bill Branan
 * Date: 5/13/11
 */
public class StorageReportHandler {

    private final Logger log =
        LoggerFactory.getLogger(StorageReportHandler.class);

    private static final String FILE_NAME_PREFIX = "storage-report-";
    private static final String FILE_NAME_SUFFIX = ".xml";
    public static final String COMPLETION_TIME_META = "completion-time";
    public static final String ELAPSED_TIME_META = "elapsed-time";

    protected static final String storageSpace = "x-duracloud-admin";
    public static final int maxRetries = 8;

    private ContentStore primaryStore = null;

    public StorageReportHandler(ContentStoreManager storeMgr) {
        try {
            this.primaryStore = storeMgr.getPrimaryContentStore();
            try {
                primaryStore.getSpaceMetadata(storageSpace);
            } catch(NotFoundException e) {
                primaryStore.createSpace(storageSpace, null);
            }
        } catch(ContentStoreException e) {
            throw new DuraCloudRuntimeException("Error checking metrics " +
                                                "storage space: " +
                                                e.getMessage());
        }
    }

    /**
     * Returns the latest storage report or null if no reports exist
     */
    public StorageReport getLatestStorageReport() throws ContentStoreException {
        Iterator<String> reports =
            primaryStore.getSpaceContents(storageSpace, FILE_NAME_PREFIX);

        // Read the list of storage reports into a list, note that there is
        // the assumption here that there will not be a very large number of
        // these files.
        LinkedList<String> reportList = new LinkedList<String>();
        while(reports.hasNext() && reportList.size() < 5000) {
            reportList.add(reports.next());
        }
        if(reportList.size() > 0) {
            Collections.sort(reportList);
            String latestContentId = reportList.getLast();
            Content latestContent =
                primaryStore.getContent(storageSpace, latestContentId);

            Map<String, String> latestMetadata = latestContent.getMetadata();
            long compTime;
            long elapTime;
            try {
                compTime = Long.valueOf(latestMetadata.get(COMPLETION_TIME_META));
                elapTime = Long.valueOf(latestMetadata.get(ELAPSED_TIME_META));
            } catch(Exception e) {
                compTime = 0;
                elapTime = 0;
            }

            return new StorageReport(latestContent.getId(),
                                     latestContent.getStream(),
                                     compTime,
                                     elapTime);            
        } else {
            return null;
        }
    }

    /**
     * Stores a storage report in the primary storage provider,
     * returns the content ID of the new item.
     *
     * @param metrics storage report
     * @param completionTime time report completed (in millis)
     * @param elapsedTime millis required to complete the report
     * @return contentId of the newly stored report
     */
    public String storeReport(DuraStoreMetrics metrics,
                              long completionTime,
                              long elapsedTime) {
        String contentId = buildContentId(completionTime);
        MetricsSerializer serializer = new MetricsSerializer();
        String xml = serializer.serializeMetrics(metrics);
        byte[] metricsBytes = getXmlBytes(xml);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(COMPLETION_TIME_META, String.valueOf(completionTime));
        metadata.put(ELAPSED_TIME_META, String.valueOf(elapsedTime));

        log.info("Storing Storage Report with ID: " + contentId);
        for(int i=0; i<maxRetries; i++) {
            try {
                primaryStore.addContent(storageSpace,
                                        contentId,
                                        new ByteArrayInputStream(metricsBytes),
                                        metricsBytes.length,
                                        MediaType.APPLICATION_XML,
                                        getMetricsChecksum(xml),
                                        metadata);
                return contentId;
            } catch (ContentStoreException e) {
                log.warn("Exception attempting to store storage report: " +
                         e.getMessage());
            }
        }
        throw new ReportBuilderException("Exceeded retries attempting to " +
                                         "store storage report");
    }

    private String buildContentId(long time) {
        String date = DateUtil.convertToString(time);
        return FILE_NAME_PREFIX + date + FILE_NAME_SUFFIX;
    }

    private byte[] getXmlBytes(String xml) {
        try {
            return xml.getBytes("UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMetricsChecksum(String xml) {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        return util.generateChecksum(xml);
    }
}
