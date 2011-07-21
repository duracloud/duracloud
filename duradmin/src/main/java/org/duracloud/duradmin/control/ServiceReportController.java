/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.util.List;

import org.duracloud.client.report.ServiceReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.serviceconfig.ServiceSummary;
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
public class ServiceReportController {
    private ServiceReportManager serviceReportManager;

    @Autowired
    public ServiceReportController(ServiceReportManager serviceReportManager) {
        this.serviceReportManager = serviceReportManager;
        this.serviceReportManager.login(new RootUserCredential());
    }

    @RequestMapping("/servicesreport/deployed")
    public ModelAndView getDeployedReport() throws ReportException {
        return new ModelAndView("jsonView",
            "serviceSummaries",
            this.serviceReportManager.getDeployedServicesReport());
    }
    
    @RequestMapping("/servicesreport/completed/list")
    public ModelAndView getCompletedServicesReportList() throws ReportException {
        return new ModelAndView("jsonView",
            "serviceReportList",
            this.serviceReportManager.getCompletedServicesReportList());
    }

    @RequestMapping(value="/servicesreport/completed/get")
    public ModelAndView getStorageReport(
            @RequestParam(required=true, value="reportId" ) String reportId)
                throws ReportException,NotFoundException {
        
        List<ServiceSummary> serviceSummaries = this.serviceReportManager.getCompletedServicesReport(reportId);

        return new ModelAndView("jsonView",
            "serviceSummaries", serviceSummaries);
    }
}
