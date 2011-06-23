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
import org.duracloud.servicemonitor.impl.ServiceSummarizerImpl;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class ServiceReportResource {

    ServicesManager servicesMgr = null;
    ServiceSummarizer serviceSummarizer;
    ServiceReportBuilder reportBuilder;

    public void initialize(ServicesManager servicesMgr) {
        this.servicesMgr = servicesMgr;
        this.serviceSummarizer = new ServiceSummarizerImpl(servicesMgr);
        this.reportBuilder = new ServiceReportBuilder(servicesMgr,
                                                      serviceSummarizer);
    }

    public String getServiceReport(){
        checkInitialized();
        return reportBuilder.buildServiceReport();
    }

    public void checkInitialized() {
        if(null == servicesMgr) {
            throw new RuntimeException("DuraReport must be initialized.");
        }
    }

}
