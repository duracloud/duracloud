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
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@Controller
public class SpacesController {

    protected final Logger log = LoggerFactory.getLogger(SpacesController.class);

    @Autowired
    @Qualifier("contentStoreManager")
    private ContentStoreManager contentStoreManager;

    public ContentStoreManager getContentStoreManager() {
		return contentStoreManager;
	}


	public void setContentStoreManager(ContentStoreManager contentStoreManager) {
		this.contentStoreManager = contentStoreManager;
	}

    
    @RequestMapping(value="/spaces/json")
	public ModelAndView getSpacesAsJson(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

        ModelAndView mav = new ModelAndView("jsonView");

        try { 
                String writeableOnlyStr = request.getParameter("writeableOnly");
                boolean writeableOnly = Boolean.valueOf(writeableOnlyStr);
                String storeId = request.getParameter("storeId");
                ContentStore c = contentStoreManager.getContentStore(storeId);
                List<String> spaceIds = c.getSpaces();
                List<Space> spaces = new LinkedList<Space>();

                Authentication a =
                    SecurityContextHolder.getContext().getAuthentication();
   
                ContentStore contentStoreWithoutRetries  = contentStoreManager.getContentStore(storeId, 0);

                for (String spaceId : spaceIds) {
                    AclType acl =
                        SpaceUtil.resolveCallerAcl(spaceId, contentStoreWithoutRetries, c.getSpaceACLs(spaceId), a);
                    Space space = new Space();
                    space.setCallerAcl(acl != null ? acl.name() : null);
                    space.setSpaceId(spaceId);
                    space.setStoreId(storeId);
                    spaces.add(space);
                }
                
                //remove all caller non writeable spaces from the spaces list
                if(writeableOnly && !SpaceUtil.isAdmin(a)){
                    for(int i = spaces.size()-1; i > -1; i--){
                        Space space = spaces.get(i);
	                    String acl = space.getCallerAcl();
	                    if(!AclType.WRITE.name().equals(acl)){
	                        spaces.remove(i);
	                    }
	                }
	            }
	            mav.addObject("spaces",spaces);
	            return mav;
	    } catch(Exception ex){
            log.error(ex.getMessage(), ex);
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


    @RequestMapping(value={"/spaces",  "/spaces/sm/**"})
    public ModelAndView handle(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("spaces-manager");

        try { 
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
        } catch(Exception ex){
            log.error(ex.getMessage(), ex);
            mav.addObject("contentStores", new LinkedList<ContentStore>());
            String error = "An error occurred attempting to retrieve your " +
                           "spaces. You may need to log out and back in to " +
                           "restore this feature. If this does not correct " +
                           "the problem, please contact your DuraCloud " +
                           "Administrator.";
            mav.addObject("error", error);
        }

        return mav;
        
    }

    
}
