package org.duracloud.duradmin.control;

import org.duracloud.client.report.StorageReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.RootUserCredential;
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
public class ReportController {

    private StorageReportManager storageReportManager;

    @Autowired
    public ReportController(StorageReportManager storageReportManager) {
        this.storageReportManager = storageReportManager;
        this.storageReportManager.login(new RootUserCredential());
    }

    @RequestMapping("/storagereport/list")
    public ModelAndView getStorageReportList() throws ReportException {
        return new ModelAndView("jsonView",
            "storageReportList",
            this.storageReportManager.getStorageReportList());
    }

    @RequestMapping(value="/storagereport/get")
    public ModelAndView getStorageReport(@RequestParam(required=true, value="reportId" ) String reportId) throws ReportException,NotFoundException {
        return new ModelAndView("jsonView",
            "storageReport",
            this.storageReportManager.getStorageReport(reportId));
    }

}
