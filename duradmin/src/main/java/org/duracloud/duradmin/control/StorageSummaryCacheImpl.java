/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.duracloud.client.report.StorageReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.SpaceMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class creates an in-memory cache of storage summary lists indexed by
 * store[/spaceId] to provide a quick way for the client to display the storage
 * statistics through time for a given store or space. On construction the 
 * cache fires off a thread to start building the cache immediately.  It also 
 * schedules the cache to be refreshed every 24 hours.
 * @author Daniel Bernstein
 * 
 */
@Component
public class StorageSummaryCacheImpl implements StorageSummaryCache {
    private Logger log = LoggerFactory.getLogger(StorageSummaryCacheImpl.class);
    
    private StorageReportManager storageReportManager;

    private Map<String, List<StorageSummary>> summaryListCache =
        new HashMap<String, List<StorageSummary>>();

    private static final DateFormat REPORT_ID_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final int DEFAULT_CACHE_RELOAD_FREQUENCY_IN_MINUTES = 60;

    private Timer timer = null;
    
    private boolean running = false;
 
    private int cacheReloadFrequencyInMinutes = DEFAULT_CACHE_RELOAD_FREQUENCY_IN_MINUTES;

    @Autowired
    public StorageSummaryCacheImpl(StorageReportManager storageReportManager) {
        this(storageReportManager, DEFAULT_CACHE_RELOAD_FREQUENCY_IN_MINUTES);
    }
    
    public StorageSummaryCacheImpl(StorageReportManager storageReportManager, int cacheReloadFrequencyInMinutes) {
        if (storageReportManager == null) {
            throw new IllegalArgumentException("The storageReportManager must be non-null");
        }
        this.storageReportManager = storageReportManager;
        this.cacheReloadFrequencyInMinutes = cacheReloadFrequencyInMinutes;
    }

    public void init(){
        if(timer != null){
            this.timer.cancel();
        }
        this.timer = new Timer();
        
        this.running = false;
        
        
        class LoadTimerTask extends TimerTask {
            @Override
            public void run() {
                if(running){
                    log.info("Storage summary cache is being built. Skipping cache load...");
                    return;
                }
                try {
                    running = true;
                    log.info("loading storage summary cache...");
                    loadCache();
                    log.info("loaded storage summary cache.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                } 
                
                running = false;
            }
        };

        long frequency = this.cacheReloadFrequencyInMinutes*60*1000;
        
        timer.schedule(new LoadTimerTask(), new Date(), frequency);
    }
    
    private Long parseDateFromReportId(String reportId) {
        // report/storage-report-2012-03-26T23:54:58.xml
        String dateString =
            reportId.replace("report/storage-report-", "").replace(".xml", "");
        try {
            return REPORT_ID_DATE_FORMAT.parse(dateString).getTime();
        } catch (ParseException e) {
            throw new DuraCloudRuntimeException(e);
        }

    }

    private void loadCache() throws Exception {
        log.info("retrieving report list...");
        List<String> list = this.storageReportManager.getStorageReportList();
        
        log.info("approximately " + list.size() + " reports in list");
        Map<String, List<StorageSummary>> newCache =
            new HashMap<String, List<StorageSummary>>();
        for (String reportId : list) {
            if (reportId.endsWith("xml")) {
                appendSummaries(newCache, reportId);
            }
        }
        
        this.summaryListCache = newCache;
    }

    private void appendSummaries(Map<String, List<StorageSummary>> cache, String reportId)
        throws NotFoundException,
            ReportException {
        Long dateInMs = parseDateFromReportId(reportId);

        StorageReport report =
            this.storageReportManager.getStorageReport(reportId);
        for (StorageProviderMetrics spm : report.getStorageMetrics()
                                                .getStorageProviderMetrics()) {
            String storeId = spm.getStorageProviderId();
            StorageSummary sps =
                new StorageSummary(dateInMs,
                                   spm.getTotalSize(),
                                   spm.getTotalItems(),
                                   reportId);

            appendToSummaryList(storeId, sps, cache);
            for (SpaceMetrics spaceMetrics : spm.getSpaceMetrics()) {
                StorageSummary spaceSummary =
                    new StorageSummary(dateInMs,
                                       spaceMetrics.getTotalSize(),
                                       spaceMetrics.getTotalItems(),
                                       reportId);
                appendToSummaryList(storeId,
                                    spaceMetrics.getSpaceName(),
                                    spaceSummary,cache);
            }
        }
        
        log.info("added storage summaries extracted from " + reportId);
    }

    private void appendToSummaryList(String storeId, StorageSummary summary, Map<String, List<StorageSummary>> cache) {
        appendToSummaryList(storeId, null, summary,cache);
    }

    private void appendToSummaryList(String storeId,
                                     String spaceId,
                                     StorageSummary summary, Map<String,List<StorageSummary>> cache) {
        List<StorageSummary> list = getSummaryList(storeId, spaceId,cache);
        list.add(summary);
    }

    private List<StorageSummary> getSummaryList(String storeId, String spaceId, Map<String,List<StorageSummary>> cache) {
        String key = formatKey(storeId, spaceId);
        List<StorageSummary> summaryList = cache.get(key);
        if (summaryList == null) {
            summaryList = new LinkedList<StorageSummary>();
            cache.put(key, summaryList);
        }

        return summaryList;
    }

    public List<StorageSummary> getSummaries(String storeId, String spaceId) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId must be non-null");
        }

        return getSummaryList(storeId, spaceId, this.summaryListCache);
    }

    private String formatKey(String storeId, String spaceId) {
        return storeId + (spaceId != null ? "/" + spaceId : "");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(timer != null){
            timer.cancel();
        }
    }
}
