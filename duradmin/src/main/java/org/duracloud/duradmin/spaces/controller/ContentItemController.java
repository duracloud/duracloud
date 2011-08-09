/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.controller.AbstractRestController;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.util.PropertiesUtils;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceapi.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class ContentItemController extends  AbstractRestController<ContentItem> {

    protected final Logger log = 
        LoggerFactory.getLogger(ContentItemController.class);

	private ContentStoreManager contentStoreManager;
	
	public ContentItemController(){
		super(null);
		setValidator(new Validator(){
			@Override
			public boolean supports(Class clazz) {
				return clazz == ContentItem.class;
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ContentItem command = (ContentItem)target;

		        if (!StringUtils.isBlank(command.getStoreId())) {
		            errors.rejectValue("storeId","required");
		        }

				if (!StringUtils.isBlank(command.getSpaceId())) {
		            errors.rejectValue("spaceId","required");
		        }

		        if (!StringUtils.isBlank(command.getContentId())) {
		            errors.rejectValue("contentId","required");
		        }
			}
		});

	}
    
    public ContentStoreManager getContentStoreManager() {
		return contentStoreManager;
	}

	public void setContentStoreManager(ContentStoreManager contentStoreManager) {
		this.contentStoreManager = contentStoreManager;
	}

	private ServicesManager servicesManager;
    
	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	
	protected ModelAndView delete(HttpServletRequest request,
			HttpServletResponse response, ContentItem contentItem,
			BindException errors) throws Exception {
		String spaceId = contentItem.getSpaceId();
        ContentStore contentStore = getContentStore(contentItem);
        contentStore.deleteContent(spaceId, contentItem.getContentId());
        return createModel(contentItem);
	}

	

	@Override
	protected ModelAndView get(HttpServletRequest request,
			HttpServletResponse response, ContentItem ci,
			BindException errors) throws Exception {
		ContentItem contentItem = new ContentItem();
		try{
            SpaceUtil.populateContentItem(
            							  getBaseURL(request),
            							  contentItem,
                                          ci.getSpaceId(),
                                          ci.getContentId(),
                                          getContentStore(ci),
                                          getServicesManager());
            
            if(!StringUtils.isBlank(contentItem.getContentId())){
                return createModel(contentItem);
            }else{
            	return new ModelAndView("jsonView", "contentItem", null);
            }
		}catch(ContentStoreException ex){
			ex.printStackTrace();
		    response.setStatus(HttpStatus.SC_NOT_FOUND);
		    return new ModelAndView("jsonView", "contentItem", null);
		}
	}
	
	
	
	
	@Override
	protected ModelAndView put(HttpServletRequest request,
			HttpServletResponse response, ContentItem contentItem,
			BindException errors) throws Exception {
	    try{
	        String spaceId = contentItem.getSpaceId();
	        String contentId = contentItem.getContentId();
	        ContentStore contentStore = getContentStore(contentItem);
	        ContentItem result = new ContentItem();
	        String method = request.getParameter("method");
	        Map<String,String> properties =
                contentStore.getContentProperties(spaceId, contentId);
	        if("changeMimetype".equals(method)){
	            String mimetype = contentItem.getContentMimetype();
	            String oldMimetype = properties.get(ContentStore.CONTENT_MIMETYPE);
	            if(!StringUtils.isBlank(mimetype) && !mimetype.equals(oldMimetype)){
	                properties.put(ContentStore.CONTENT_MIMETYPE, mimetype);
	                contentStore.setContentProperties(spaceId, contentId, properties);
	            }
	        }else{ 
                PropertiesUtils.handle(method,
                                       "space [" + spaceId + "]",
                                       properties,
                                       request);
                contentStore.setContentProperties(spaceId, contentId, properties);
            }


	        SpaceUtil.populateContentItem(getBaseURL(request),
                                          result,
                                          contentItem.getSpaceId(),
	                                      contentItem.getContentId(),
                                          contentStore,
                                          servicesManager);
	        return createModel(result);
	        
	    }catch(Exception ex){
	        ex.printStackTrace();
	        throw ex;
	    }
	}


	public static String getBaseURL(HttpServletRequest request) throws MalformedURLException{
		URL url = new URL(request.getRequestURL().toString());
		int port =  url.getPort();
		String baseURL = url.getProtocol() + "://" + url.getHost() + ":" +
                        (port > 0 && port != 80 ? url.getPort() : "") +
                        request.getContextPath();
		return baseURL;
	}

	private ModelAndView createModel(ContentItem ci){
        return new ModelAndView("jsonView", "contentItem",ci);
	}
	
	protected ContentStore getContentStore(ContentItem contentItem) throws ContentStoreException{
		return contentStoreManager.getContentStore(contentItem.getStoreId());
	}



	private ContentItem create(HttpServletRequest request) {
		ContentItem ci = new ContentItem();
		ci.setStoreId(request.getParameter("storeId"));	
		ci.setSpaceId(request.getParameter("spaceId"));
		ci.setContentId(request.getParameter("contentId"));
		return ci;
	}


}
