/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.report.StorageReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 * 
 */
@Controller
public class StorageReportController {

    private Logger log = LoggerFactory.getLogger(StorageReportController.class);
    private StorageReportManager storageReportManager;
    private StorageSummaryCache storageSummaryCache;
    
    @Autowired
    public StorageReportController(
        StorageReportManager storageReportManager,
        StorageSummaryCache storageSummaryCache) {
        this.storageReportManager = storageReportManager;
        this.storageSummaryCache = storageSummaryCache;
        this.storageReportManager.login(new RootUserCredential());
    }

    @RequestMapping("/storagereport/list")
    public ModelAndView getStorageReportList() throws ReportException {
        return new ModelAndView("jsonView",
            "storageReportList",
            this.storageReportManager.getStorageReportList());
    }


    @RequestMapping("/storagereport/summaries")
    public ModelAndView  getStorageReportSummaries(
                                                  @RequestParam String storeId, 
                                                  @RequestParam(required=false) String spaceId)
        throws ParseException,
            ReportException,
            NotFoundException {

        
        List<StorageSummary> summaries = this.storageSummaryCache.getSummaries(storeId, spaceId);

        ModelAndView mav = new ModelAndView("jsonView");
        mav.addObject("summaries", summaries);        
        return mav;
    }

    @RequestMapping(value="/storagereport/get")
    @Deprecated
    public ModelAndView getStorageReport(
            @RequestParam(required=false, value="reportId" ) String reportId, 
            @RequestParam(required=false, value="format" ) String format, 
            HttpServletResponse response)
                throws ReportException,NotFoundException {
        
        StorageReport report;
        
        if(reportId != null){
            report = this.storageReportManager.getStorageReport(reportId);
        }else{
            report = this.storageReportManager.getLatestStorageReport();
        }

        if(format == null || "json".equals(format)){
            return new ModelAndView("jsonView",
                "storageReport", report);
        }else{
            StorageReportSerializer srs = new StorageReportSerializer();
            String xml = srs.serialize(report);
            try {
                response.getWriter().write(xml);
            } catch (IOException e) {
                log.error("failed to write serialized report", e);
                throw new RuntimeException(e);
            }
            return null;
        }
    }
    
    @RequestMapping(value="/storagereport/detail")
    public ModelAndView getDetail(
            @RequestParam(required=true, value="storeId" ) String storeId, 
            @RequestParam(required=false, value="reportId" ) String reportId)
                throws ReportException,NotFoundException {
        
        StorageReport report;
        if(reportId != null){
            report = this.storageReportManager.getStorageReport(reportId);
        }else{
            report = this.storageReportManager.getLatestStorageReport();
        }
        
        StorageProviderMetrics metrics = null;
        for(StorageProviderMetrics spm : report.getStorageMetrics().getStorageProviderMetrics()){
            if(spm.getStorageProviderId().equals(storeId)){
               metrics = spm;
               break;
            }
        }
        ModelAndView mav =  new ModelAndView("jsonView");
        mav.addObject("metrics", metrics);
        mav.addObject("reportId", report.getReportId());
        return mav;
    }
}
