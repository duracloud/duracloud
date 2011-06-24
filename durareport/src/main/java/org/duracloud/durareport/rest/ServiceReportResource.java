/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.durareport.service.ServiceReportBuilder;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.impl.ServiceSummarizerImpl;
import org.duracloud.servicemonitor.impl.TempServiceSummaryDirectoryImpl;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class ServiceReportResource {

    private ServicesManager servicesMgr = null;
    private ServiceSummarizer serviceSummarizer;
    private ServiceSummaryDirectory summaryDirectory;
    private ServiceReportBuilder reportBuilder;

    public void initialize(ServicesManager servicesMgr) {
        this.servicesMgr = servicesMgr;
        this.serviceSummarizer = new ServiceSummarizerImpl(servicesMgr);
        this.summaryDirectory = new TempServiceSummaryDirectoryImpl();
        this.reportBuilder = new ServiceReportBuilder(servicesMgr,
                                                      serviceSummarizer,
                                                      summaryDirectory);
    }

    public InputStream getDeployedServicesReport(){
        checkInitialized();
        return reportBuilder.getDeployedServicesReport();
    }

    public InputStream getCompletedServicesReport(int limit) {
        return reportBuilder.getCompletedServicesReport(limit);
    }

    public InputStream getCompletedServicesReportIds() {
        return reportBuilder.getCompletedServicesReportIds();
    }

    public InputStream getCompletedServicesReport(String reportId) {
        return reportBuilder.getCompletedServicesReport(reportId);
    }

    public void checkInitialized() {
        if(null == servicesMgr) {
            throw new RuntimeException("DuraReport must be initialized.");
        }
    }

}
