package org.duracloud.duradmin.control;

import org.duracloud.client.report.StorageReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 * 
 */
@Controller
public class ReportController {

    private Logger log = LoggerFactory.getLogger(ReportController.class);
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
    public ModelAndView getStorageReport(
            @RequestParam(required=true, value="reportId" ) String reportId, 
            @RequestParam(required=false, value="format" ) String format, 
            HttpServletResponse response)
                throws ReportException,NotFoundException {
        
        StorageReport report = this.storageReportManager.getStorageReport(reportId);

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

}
