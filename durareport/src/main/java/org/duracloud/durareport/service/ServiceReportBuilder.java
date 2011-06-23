/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.duracloud.durareport.error.ReportBuilderException;
import org.duracloud.durareport.storage.StorageReportScheduler;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: Bill Branan
 * Date: 6/22/11
 */
public class ServiceReportBuilder {

    private final Logger log =
        LoggerFactory.getLogger(StorageReportScheduler.class);

    private ServicesManager servicesMgr;
    private ServiceSummarizer serviceSummarizer;

    public ServiceReportBuilder(ServicesManager servicesMgr,
                                ServiceSummarizer serviceSummarizer) {
        this.servicesMgr = servicesMgr;
        this.serviceSummarizer = serviceSummarizer;
    }

    public String buildServiceReport() {
        List<ServiceSummary> runningServices =  collectRunningServices();
        XStream xstream = new XStream(new DomDriver());
        return xstream.toXML(runningServices);
    }

    protected List<ServiceSummary> collectRunningServices()
        throws ReportBuilderException {
        try {
            List<ServiceInfo> deployedServices =
                servicesMgr.getDeployedServices();
            return serviceSummarizer.summarizeServices(deployedServices);
        } catch(ServicesException e) {
            String error = "Unable to collect information about running " +
                           "services due to: " + e.getMessage();
            throw new ReportBuilderException(error, e);
        }
    }

    protected void collectCompletedServices() {
        // TODO: Pull in completed service listing
    }

}
