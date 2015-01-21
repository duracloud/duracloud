/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.error.ContentStateException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author dbernstein@duraspace.org
 */
@Controller
public class ContentItemDownloadController {

    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private ControllerSupport controllerSupport;

    @Autowired
    public ContentItemDownloadController(ControllerSupport controllerSupport) {
        this.controllerSupport = controllerSupport;
    }


    @RequestMapping(value="/download/contentItem", method=RequestMethod.GET)
    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {
    	
    	String storeId = request.getParameter("storeID");
    	if(storeId == null){
    	    storeId = request.getParameter("storeId");
    	}
    	
    	if(storeId == null){
            storeId =
                controllerSupport.getContentStoreManager()
                                 .getPrimaryContentStore()
                                 .getStoreId();
    	}
    	
    	
    	String spaceId = request.getParameter("spaceId");
    	String contentId = request.getParameter("contentId");
    	String attachment = request.getParameter("attachment");
    	
    	if(Boolean.valueOf(attachment)){
            StringBuffer contentDisposition = new StringBuffer();
            contentDisposition.append("attachment;");
            contentDisposition.append("filename=\"");
            contentDisposition.append(contentId);
            contentDisposition.append("\"");
            response.setHeader(CONTENT_DISPOSITION_HEADER, contentDisposition.toString());
    	}

    	ContentStore store = 
    	    controllerSupport.getContentStoreManager().getContentStore(storeId);
    	try {
            SpaceUtil.streamContent(store, response, spaceId, contentId);
    	    
    	} catch(ContentStoreException ex){
    	    if(response.containsHeader(CONTENT_DISPOSITION_HEADER)){
    	        response.setHeader(CONTENT_DISPOSITION_HEADER, null);
    	    }
    	    
    	    if(ex instanceof ContentStateException){
    	        response.setStatus(HttpStatus.SC_CONFLICT);
    	    }else if (ex instanceof UnauthorizedException){
    	        response.setStatus(HttpStatus.SC_UNAUTHORIZED);
    	    }
    	    
    	    Throwable t = ex;
    	    if(t.getCause() != null){
    	        t = ex.getCause();
    	    }
    	    response.getWriter().println(t.getMessage());
    	}

    	return null;
    }
}