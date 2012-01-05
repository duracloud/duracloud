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
import org.duracloud.common.model.AclType;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
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
	            String writeableOnlyStr = request.getParameter("writeableOnly");
	            boolean writeableOnly = Boolean.valueOf(writeableOnlyStr);
	            String storeId = request.getParameter("storeId");   
	            ContentStore c = contentStoreManager.getContentStore(storeId);
	            ModelAndView mav = new ModelAndView("jsonView");
	            List<String> spaces = c.getSpaces();
                Authentication a =
                    SecurityContextHolder.getContext().getAuthentication();

                //remove all caller non writeable spaces from the spaces list
                if(writeableOnly && !SpaceUtil.isAdmin(a)){
                    for(int i = spaces.size()-1; i > -1; i--){
                        String spaceId = spaces.get(i);
	                    AclType acl = SpaceUtil.resolveCallerAcl(c.getSpaceACLs(spaceId), a);
	                    if(acl != AclType.WRITE){
	                        spaces.remove(i);
	                    }
	                }
	            }
	            mav.addObject("spaces",spaces);
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
                mav.addObject("user",
                              (DuracloudUserDetails) SecurityContextHolder.getContext()
                                                                          .getAuthentication()
                                                                          .getPrincipal());

	            return mav;
	        }
	        
	        
	    } catch(Exception ex){
            log.error(ex.getMessage(), ex);
            ModelAndView mav = new ModelAndView("spaces-manager");
            mav.addObject("contentStores", new LinkedList<ContentStore>());
            String error = "An error occurred attempting to retrieve your " +
                           "spaces. You may need to log out and back in to " +
                           "restore this feature. If this does not correct " +
                           "the problem, please contact your DuraCloud " +
                           "Administrator.";
            mav.addObject("error", error);
	        return mav;
	    }
		
	}


}
