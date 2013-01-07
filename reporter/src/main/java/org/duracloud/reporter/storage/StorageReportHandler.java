/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reporter.storage;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.domain.Content;
import org.duracloud.reporter.error.ReportBuilderException;
import org.duracloud.reporter.storage.metrics.DuraStoreMetricsCollector;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportList;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
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

    private static final String REPORT_FILE_NAME_SUFFIX = ".xml";
    public static final String COMPLETION_TIME_META = "completion-time";
    public static final String ELAPSED_TIME_META = "elapsed-time";
    public static final int maxRetries = 8;

    protected String storageSpace;
    private ContentStore primaryStore = null;
    private String reportFileNamePrefix;
    private String reportErrorLogFileName;

    public StorageReportHandler(ContentStoreManager storeMgr,
                                String storageSpace,
                                String reportFileNamePrefix,
                                String reportErrorLogFileName) {
        this.storageSpace = storageSpace;
        this.reportFileNamePrefix = reportFileNamePrefix;
        this.reportErrorLogFileName = reportErrorLogFileName;
        try {
            this.primaryStore = storeMgr.getPrimaryContentStore();
            try {
                primaryStore.getSpaceProperties(storageSpace);
            } catch(NotFoundException e) {
                primaryStore.createSpace(storageSpace);
            }
        } catch(ContentStoreException e) {
            throw new DuraCloudRuntimeException("Error checking metrics " +
                                                "storage space: " +
                                                e.getMessage());
        }
    }

    /**
     * Returns a specific storage report stream or null if the report does
     * not exist.
     *
     * @param reportId content ID of the report to retrieve
     * @return InputStream containing report
     */
    public InputStream getStorageReportStream(String reportId)
        throws ContentStoreException {
        try {
            return primaryStore.getContent(storageSpace, reportId).getStream();
        } catch(NotFoundException e) {
            return null;
        }
    }

    /**
     * Returns a specific storage report or null if the report does not exist.
     *
     * @param reportId content ID of the report to retrieve
     * @return StorageReport
     */
    public StorageReport getStorageReport(String reportId)
        throws ContentStoreException {
        try {
            Content content = primaryStore.getContent(storageSpace, reportId);
            return deserializeStorageReport(content);
        } catch(NotFoundException e) {
            return null;
        }
    }

    private StorageReport deserializeStorageReport(Content content) {
        StorageReportSerializer serializer = new StorageReportSerializer();
        return serializer.deserialize(content.getStream());
    }

    /**
     * Returns the latest storage report stream or null if no reports exist
     */
    public InputStream getLatestStorageReportStream()
        throws ContentStoreException {
        Content latestContent = getLatestStorageReportContent();
        if(null != latestContent) {
            return latestContent.getStream();
        } else {
            return null;
        }
    }

    /**
     * Returns the latest storage report or null if no reports exist
     */
    public StorageReport getLatestStorageReport() throws ContentStoreException {
        Content latestContent = getLatestStorageReportContent();
        if(null != latestContent) {
            return deserializeStorageReport(latestContent);
        } else {
            return null;
        }
    }

    private Content getLatestStorageReportContent()
        throws ContentStoreException {
        LinkedList<String> reportList = getSortedReportList();
        if(reportList.size() > 0) {
            String latestContentId = reportList.getFirst();
            Content latestContent =
                primaryStore.getContent(storageSpace, latestContentId);
            return latestContent;
        } else {
            return null;
        }
    }

    /*
     * Retrieves a list of all report lists (limited to a maximum of 5000),
     * sorted by name in descending order (i.e. the latest report will be first)
     */
    private LinkedList<String> getSortedReportList()
        throws ContentStoreException {
        Iterator<String> reports =
            primaryStore.getSpaceContents(storageSpace, reportFileNamePrefix);

        // Read the list of storage reports into a list, note that there is
        // the assumption here that there will not be a very large number of
        // these files.
        LinkedList<String> reportList = new LinkedList<String>();
        while(reports.hasNext() && reportList.size() < 5000) {
            reportList.add(reports.next());
        }
        if(reportList.size() > 0) {
            Collections.sort(reportList);
            Collections.reverse(reportList);
        }
        return reportList;
    }

    /**
     * Retrieve a sorted list of all storage reports in XML format. Sorting
     * is by name in descending order (i.e. the latest report will be first).
     *
     * @return list of storage reports
     * @throws ContentStoreException
     */
    public StorageReportList getStorageReportList() throws ContentStoreException {
        return new StorageReportList(getSortedReportList());
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
    public String storeReport(DuraStoreMetricsCollector metrics,
                              long completionTime,
                              long elapsedTime) {
        String contentId = buildContentId(completionTime);

        StorageReportConverter converter = new StorageReportConverter();
        StorageReport report = converter.createStorageReport(contentId,
                                                             metrics,
                                                             completionTime,
                                                             elapsedTime);

        StorageReportSerializer serializer = new StorageReportSerializer();
        String xml = serializer.serialize(report);
        byte[] metricsBytes = getXmlBytes(xml);

        log.info("Storing Storage Report with ID: " + contentId);
        for(int i=0; i<maxRetries; i++) {
            try {
                primaryStore.addContent(storageSpace,
                                        contentId,
                                        new ByteArrayInputStream(metricsBytes),
                                        metricsBytes.length,
                                        MediaType.APPLICATION_XML,
                                        getMetricsChecksum(xml),
                                        null);
                return contentId;
            } catch (ContentStoreException e) {
                log.warn("Exception attempting to store storage report: " +
                         e.getMessage());
                wait(i);
            }
        }
        throw new ReportBuilderException("Exceeded retries attempting to " +
                                         "store storage report");
    }

    private String buildContentId(long time) {
        String date = DateUtil.convertToString(time);
        return reportFileNamePrefix + date + REPORT_FILE_NAME_SUFFIX;
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

    public void addToErrorLog(String errMsg) {
        InputStream existingLog = null;
        long existingLogSize = 0;
        try {
            Content errorLogContent =
                primaryStore.getContent(storageSpace, reportErrorLogFileName);
            if(null != errorLogContent) {
                existingLog = errorLogContent.getStream();
                existingLogSize = getExistingLogSize(errorLogContent);
            }
        } catch(ContentStoreException e) {
            // Could not get error log, likely because it does not yet exist
        }

        String logMsg = createLogMsg(errMsg);
        InputStream newMsg = createLogMsgStream(logMsg);
        InputStream newLog;
        if(null != existingLog) {
            newLog = new SequenceInputStream(newMsg, existingLog);
        } else {
            newLog = newMsg;
        }

        for(int i=0; i<maxRetries; i++) {
            try {
                primaryStore.addContent(storageSpace,
                                        reportErrorLogFileName,
                                        newLog,
                                        existingLogSize + logMsg.length(),
                                        MediaType.TEXT_PLAIN,
                                        null,
                                        null);
                return;
            } catch(ContentStoreException e) {
                log.warn("Exception attempting to store error log: " +
                         e.getMessage());
                wait(i);
            }
        }
        log.error("Unable to store error log file!");
    }

    private String createLogMsg(String msg) {
        return DateUtil.now() + "  " + msg + "\n";
    }

    private InputStream createLogMsgStream(String logMsg) {
        try {
            return IOUtils.toInputStream(logMsg, "UTF-8");
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private long getExistingLogSize(Content logContent) {
        Map<String, String> properties = logContent.getProperties();
        if(null != properties) {
            String logSize = properties.get(ContentStore.CONTENT_SIZE);
            if(null != logSize) {
                try {
                    return Long.valueOf(logSize);
                } catch(NumberFormatException e) {
                }
            }
        }
        return 0;
    }

    private void wait(int index) {
        try {
            Thread.sleep(1000 * index);
        } catch(InterruptedException e) {
        }
    }

}
