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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.util.LineParsingIterator;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
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
    private ContentStoreManager contentStoreManager;

    @Autowired
    public ServiceReportController(
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    @RequestMapping(value="/servicesreport/htmltable")
    public ModelAndView getReportAsHtmlTable(HttpServletResponse response,
            @RequestParam(required=false, value="storeId" ) String storeId,            
            @RequestParam(required=true, value="spaceId" ) String spaceId,
            @RequestParam(required=true, value="contentId" ) String contentId)

                throws ReportException,NotFoundException, ContentStoreException, IOException {
       
        ContentStore store = contentStoreManager.getPrimaryContentStore();
        
        if(storeId != null){
            store = contentStoreManager.getContentStore(storeId);
        }
        
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
                                    (storeId != null ? "&storeId="+storeId : "") +
                                    "&attachment=true");
        
        return mav;
    }


    @RequestMapping(value="/servicesreport/info")
    public ModelAndView
        getInfo(@RequestParam(required = false, value = "storeId") String storeId,
                @RequestParam(required = true, value = "spaceId") String spaceId,
                @RequestParam(required = true, value = "contentId") String contentId)

            throws ReportException,
                NotFoundException,
                ContentStoreException,
                IOException {

        ContentStore store = contentStoreManager.getPrimaryContentStore();

        if (storeId != null) {
            store = contentStoreManager.getContentStore(storeId);
        }

        Content content = store.getContent(spaceId, contentId);

        String size = content.getProperties().get(ContentStore.CONTENT_SIZE);
        Map<String, String> fileInfo = new HashMap<String, String>();
        fileInfo.put("size", size);
        return new ModelAndView("jsonView", "fileInfo", fileInfo);
    }

}
