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
import org.duracloud.servicemonitor.error.ServiceSummaryNotFoundException;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class ServiceReportResource {

    private ServiceReportBuilder reportBuilder;

    public void initialize(ServiceSummaryDirectory summaryDirectory,
                           ServiceSummarizer summarizer,
                           ServicesManager servicesMgr) {
        // Note: ServicesManager can be removed from this method and the
        //       ServiceReportBuilder class if ServiceSummarizer has the
        //       following method added to its interface:
        //   List<ServiceInfo> summarizer.getDeployedServices();
        this.reportBuilder = new ServiceReportBuilder(servicesMgr,
                                                      summarizer,
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

    public InputStream getCompletedServicesReport(String reportId)
        throws ServiceSummaryNotFoundException {
        return reportBuilder.getCompletedServicesReport(reportId);
    }

    public void checkInitialized() {
        if(null == reportBuilder) {
            throw new RuntimeException("DuraReport must be initialized.");
        }
    }

}
