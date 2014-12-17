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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.LineParsingIterator;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReport;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReportProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 * 
 */
@Controller
@RequestMapping("/servicesreport")
public class ServiceReportController {
    private ContentStoreManager contentStoreManager;

    @Autowired
    public ServiceReportController(
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    @RequestMapping(value="/htmltable")
    public ModelAndView getReportAsHtmlTable(HttpServletResponse response,
            @RequestParam(required=false, value="storeId" ) String storeId,            
            @RequestParam(required=true, value="spaceId" ) String spaceId)

                throws ReportException,NotFoundException, ContentStoreException, IOException {
       
        ContentStore store = contentStoreManager.getPrimaryContentStore();
        
        if(storeId != null){
            store = contentStoreManager.getContentStore(storeId);
        }
        
        BitIntegrityReport report = store.getBitIntegrityReport(spaceId);
        BitIntegrityReportProperties props = report.getProperties();
        BufferedReader reader = new BufferedReader(new InputStreamReader(report.getStream()));
        
        //defaults to tab separated parser
        LineParsingIterator iterator = new LineParsingIterator(reader);
        ModelAndView mav = new ModelAndView("simple-report");
        mav.addObject("title", "Report: " + spaceId + " as of " + props.getCompletionDate());
        mav.addObject("data", iterator);
        mav.addObject("reportLink", "/duradmin/servicesreport/raw?"+
                                    "spaceId=" + spaceId + 
                                    (storeId != null ? "&storeId="+storeId : "") +
                                    "&attachment=true");
        
        return mav;
    }


    @RequestMapping(value="/info")
    public ModelAndView
        getInfo(@RequestParam(required = false, value = "storeId") String storeId,
                @RequestParam(required = true, value = "spaceId") String spaceId)

            throws ReportException,
                NotFoundException,
                ContentStoreException,
                IOException {

        ContentStore store = contentStoreManager.getPrimaryContentStore();

        if (storeId != null) {
            store = contentStoreManager.getContentStore(storeId);
        }

        BitIntegrityReportProperties props = store.getBitIntegrityReportProperties(spaceId);

        int size = props.getSize();
        Map<String, String> fileInfo = new HashMap<String, String>();
        fileInfo.put("size", size+"");
        return new ModelAndView("jsonView", "fileInfo", fileInfo);
    }
    
    @RequestMapping(value = "/raw", method = RequestMethod.GET)
    public ModelAndView
        getRaw(@RequestParam(required = false, value = "storeId") String storeId,
               @RequestParam(required = true, value = "spaceId") String spaceId,
               HttpServletResponse response) throws Exception {

        ContentStore store = contentStoreManager.getContentStore(storeId);
        BitIntegrityReport report = store.getBitIntegrityReport(spaceId);

        StringBuffer contentDisposition = new StringBuffer();
        contentDisposition.append("attachment;");
        contentDisposition.append("filename=\"");
        String date =
            DateUtil.convertToString(report.getProperties()
                                           .getCompletionDate()
                                           .getTime());
        contentDisposition.append(MessageFormat.format("bit-integrity-report_{0}_{1}_{2}.tsv",
                                                       storeId,
                                                       spaceId,
                                                       date));
        contentDisposition.append("\"");
        response.setHeader("Content-Disposition", contentDisposition.toString());

        SpaceUtil.streamToResponse(report.getStream(),
                                   response,
                                   "text/tsv",
                                   report.getProperties().getSize() + "");

        return null;
    }

}
