/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.spaces.controller;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SpacesController implements Controller {

    protected final Logger log = LoggerFactory.getLogger(SpacesController.class);
    
    public ContentStoreManager getContentStoreManager() {
		return contentStoreManager;
	}


	public void setContentStoreManager(ContentStoreManager contentStoreManager) {
		this.contentStoreManager = contentStoreManager;
	}


	private ContentStoreManager contentStoreManager;
    
    
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	    try { 
	        
	        if("json".equals(request.getParameter("f"))){
	            String storeId = request.getParameter("storeId");   
	            ContentStore c = contentStoreManager.getContentStore(storeId);
	            ModelAndView mav = new ModelAndView("jsonView");
	            mav.addObject("spaces",c.getSpaces());
	            return mav;
	        }else{
	            ModelAndView mav = new ModelAndView("spaces-manager");
	            List<ContentStore> stores = new LinkedList<ContentStore>();
	            String primaryStoreId = contentStoreManager.getPrimaryContentStore().getStoreId();
	            for(ContentStore store : contentStoreManager.getContentStores().values()){
	            	if(store.getStoreId().equals(primaryStoreId)){
	            		stores.add(0,store);
	            	}else{
	            		stores.add(store);
	            	}
	            	
	            }
	            mav.addObject("contentStores", stores);
	            return mav;
	        }
	        
	        
	    } catch(Exception ex){
	        ex.printStackTrace();
	        throw ex;
	    }
		
	}


}
