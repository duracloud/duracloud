/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author dbernstein@duraspace.org
 */
public class ContentItemDownloadController 
        extends AbstractController {

    private ControllerSupport controllerSupport;

    public ContentItemDownloadController(ControllerSupport controllerSupport) {
        this.controllerSupport = controllerSupport;
    }


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {
    	
    	String storeId = request.getParameter("storeID");
    	if(storeId == null){
    	    storeId = request.getParameter("storeId");;
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
            response.setHeader("Content-Disposition", contentDisposition.toString());
    	}
    	OutputStream os = response.getOutputStream();

    	for(ContentStore store : controllerSupport.getContentStoreProvider().getContentStores()){
    		if(store.getStoreId().equals(storeId)){
    		   	Content c = store.getContent(spaceId, contentId);
    		   	Map<String,String> m = store.getContentMetadata(spaceId, contentId);
    		   	response.setContentType(m.get(ContentStore.CONTENT_MIMETYPE));
    		   	response.setContentLength(Integer.parseInt(m.get(ContentStore.CONTENT_SIZE)));
    		   	InputStream is = c.getStream();
    		   	byte[] buf = new byte[1024];
    		   	int read = -1;
    		   	while((read = is.read(buf)) > 0){
    		   		os.write(buf, 0, read);
    		   	}

    	    	response.flushBuffer();
    	    	response.getOutputStream().close();
    	    	break;
    		}
    	}
    	
        return null;
    }

}