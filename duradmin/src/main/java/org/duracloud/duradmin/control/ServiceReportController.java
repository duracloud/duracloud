/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.report.ServiceReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.common.util.LineParsingIterator;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceconfig.ServiceSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private ContentStoreManager contentStoreManager;

    @Autowired
    public ServiceReportController(ServiceReportManager serviceReportManager, 
                                   @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.serviceReportManager = serviceReportManager;
        this.serviceReportManager.login(new RootUserCredential());
        this.contentStoreManager = contentStoreManager;
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
    
    @RequestMapping(value="/servicesreport/htmltable")
    public ModelAndView getReportAsHtmlTable(HttpServletResponse response,
            @RequestParam(required=true, value="spaceId" ) String spaceId,
            @RequestParam(required=true, value="contentId" ) String contentId)

                throws ReportException,NotFoundException, ContentStoreException, IOException {
        ContentStore store = contentStoreManager.getPrimaryContentStore();
        
        Content content = store.getContent(spaceId, contentId);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(content.getStream()));
        
        //defaults to tab separated parser
        LineParsingIterator iterator = new LineParsingIterator(reader);
        ModelAndView mav = new ModelAndView("simple-report");
        mav.addObject("title", "Report: " + spaceId + "/" + contentId);
        mav.addObject("data", iterator);
        mav.addObject("reportLink", "/duradmin/download/contentItem?"+
                                    "spaceId=" + spaceId + 
                                    "&contentId=" + contentId + 
                                    "&attachment=true");
        
        return mav;
    }
}
