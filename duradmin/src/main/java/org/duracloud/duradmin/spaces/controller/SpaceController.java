/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.StoreCaller;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.ExtendedIteratorCounterThread;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.domain.SpaceProperties;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.mill.db.model.BitIntegrityReport;
import org.duracloud.mill.db.repo.JpaBitIntegrityReportRepo;
import org.duracloud.mill.db.repo.MillJpaRepoConfig;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReportProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@Controller
@RequestMapping("/spaces/space")
public class SpaceController {

    protected final Logger log = LoggerFactory.getLogger(getClass());

	private ContentStoreManager contentStoreManager;

    private JpaBitIntegrityReportRepo reportRepo;

    private String adminSpaceId;

    @Autowired
    public SpaceController(
        @Qualifier("adminSpaceId") String adminSpaceId,
        @Qualifier("bitIntegrityReportRepo") JpaBitIntegrityReportRepo reportRepo,
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.adminSpaceId = adminSpaceId;
        this.reportRepo = reportRepo;
        this.contentStoreManager = contentStoreManager;
    }
    

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ModelAndView get(HttpServletRequest request,
			HttpServletResponse response, @Valid Space space,
			BindingResult result) throws Exception {
		try{
			String prefix = request.getParameter("prefix");
			if(prefix != null){
				prefix = ("".equals(prefix.trim())?null:prefix);
			}
			String marker = request.getParameter("marker");
			ContentStore contentStore = contentStoreManager.getContentStore(space.getStoreId());
			org.duracloud.domain.Space cloudSpace =
                contentStore.getSpace(space.getSpaceId(), prefix, 200, marker);
            ContentStore contentStoreWithoutRetries =
                contentStoreManager.getContentStore(space.getStoreId(),0);
			populateSpace(space, cloudSpace, contentStoreWithoutRetries);
			populateSpaceCount(space, request);
			if(space.isMillDbEnabled()){
	            populateBitIntegrityResults(space, contentStore);
			}
			return createModel(space);
		}catch(ContentStoreException ex){
			ex.printStackTrace();
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return createModel(null);
		}
	}

    private void populateBitIntegrityResults(Space space, ContentStore contentStore) {
        try{
            BitIntegrityReportProperties bitReportProps =
                contentStore.getBitIntegrityReportProperties(space.getSpaceId());
            if(bitReportProps == null){
                log.warn("No bit report properties found for space {}", space.getSpaceId());
            }else{
                space.setBitIntegrityReportProperties(bitReportProps);
            }

        }catch(Exception ex){
            log.error("failed to populate bit integrity results due to error:" + ex.getMessage(), ex);
        }
    }
    
    private void populateSpace(Space space,
                               org.duracloud.domain.Space cloudSpace,
                               ContentStore contentStore)
        throws ContentStoreException {
        SpaceUtil.populateSpace(space,
                                cloudSpace,
                                contentStore,
                                getAuthentication());

        String primaryStoreId = contentStoreManager.getPrimaryContentStore().getStoreId();
        boolean primary = primaryStoreId.equals(space.getStoreId());
        space.setPrimaryStorageProvider(primary);
    }
	
	
	private void populateSpaceCount(Space space, HttpServletRequest request) throws Exception{
	    //flush space count cache
	    if(request.getParameterMap().containsKey("recount")){
	        expireItemCount(request, space);
	    }
	    
		String countStr = space.getProperties().getCount();
		if(countStr.endsWith("+")){
			setItemCount(space, request);
		}else{
			space.setItemCount(Long.valueOf(space.getProperties().getCount()));
		}
	}


	private void setItemCount(final Space space, HttpServletRequest request) throws ContentStoreException{
		String key = formatItemCountCacheKey(space);
		final ServletContext appContext = request.getSession().getServletContext();
		ItemCounter listener = (ItemCounter)appContext.getAttribute(key);
		space.setItemCount(new Long(-1));
        if(listener != null){
            if(listener.isCountComplete()) {
                space.setItemCount(listener.getCount());
            } else {
                SpaceProperties properties = space.getProperties();
                Long interCount = listener.getIntermediaryCount();
                if(interCount == null){
                    interCount = 0l;
                }
                properties.setCount(String.valueOf(interCount) + "+");
                space.setProperties(properties);
            }
		}else{
		    final ItemCounter itemCounterListener = new ItemCounter();
		    appContext.setAttribute(key, itemCounterListener);
			final ContentStore contentStore = contentStoreManager.getContentStore(space.getStoreId());
			final StoreCaller<Iterator<String>> caller = new StoreCaller<Iterator<String>>() {
	            protected Iterator<String> doCall() throws ContentStoreException {
	            	return contentStore.getSpaceContents(space.getSpaceId());
	            }
	            public String getLogMessage() {
	                return "Error calling contentStore.getSpaceContents() for: " +
	                   space.getSpaceId();
	            }
	        };

	        new Thread(new Runnable(){
	            public void run(){
	                ExtendedIteratorCounterThread runnable = 
	                    new ExtendedIteratorCounterThread(caller.call(), itemCounterListener);              
	                runnable.run();
	            }
	        }).start();
		}
	}

    private String formatItemCountCacheKey(Space space) {
        return space.getStoreId() + "/" + space.getSpaceId() + "/itemCountListener";
    }

    private void expireItemCount(HttpServletRequest request, Space space){
        String key = formatItemCountCacheKey(space);
        request.getSession().getServletContext().removeAttribute(key);
    }

	private Authentication getAuthentication() {
	    return (Authentication)SecurityContextHolder.getContext().getAuthentication();
    }

	
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ModelAndView addSpace(HttpServletRequest request,
                                HttpServletResponse response,
                                @Valid Space space,
                                BindingResult result) throws Exception {
        String spaceId = space.getSpaceId();
        ContentStore contentStore = getContentStore(space);
        contentStore.createSpace(spaceId);

        if ("true".equals(request.getParameter("publicFlag"))) {
            Map<String, AclType> acls = new HashMap<String, AclType>();
            acls.put("group-public", AclType.READ);
            contentStore.setSpaceACLs(spaceId, acls);
        }

        populateSpace(space,
                      contentStore.getSpace(spaceId, null, 0, null),
                      contentStore);
        return createModel(space);
	}


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ModelAndView delete(HttpServletRequest request,
			HttpServletResponse response, Space space,
			BindingResult result) throws Exception {
		String spaceId = space.getSpaceId();
        ContentStore contentStore = getContentStore(space);
        contentStore.deleteSpace(spaceId);
        return createModel(space);
	}

	private ModelAndView createModel(Space space){
        return new ModelAndView("jsonView", "space",space);
	}
	
	protected ContentStore getContentStore(Space space) throws ContentStoreException{
		return contentStoreManager.getContentStore(space.getStoreId());
	}
}
