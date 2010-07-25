/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.StringUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ServicesManager;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.util.SpaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 * @author Daniel Bernstein
 */

public class ContentItemUploadController implements Controller{

    protected final Logger log = 
        LoggerFactory.getLogger(ContentItemUploadController.class);

	
	private ServicesManager servicesManager;
    
	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	public ContentStoreManager getContentStoreManager() {
		return contentStoreManager;
	}

	public void setContentStoreManager(ContentStoreManager contentStoreManager) {
		this.contentStoreManager = contentStoreManager;
	}

	private ContentStoreManager contentStoreManager;
	
	private class ProgressAdapter implements ProgressListener{
			long bytesRead = 0;
			long totalBytes = -1;
			private ProgressListener listener = null;
			@Override
			public void update(long pBytesRead, long pContentLength,
					int pItems) {
				if(listener != null){
					listener.update(pBytesRead, pContentLength, pItems);
				}else{
					bytesRead = pBytesRead;
					totalBytes = pContentLength;
				}
			}
			
			public void setListener(ProgressListener listener){
				this.listener = listener;
				this.listener.update(bytesRead, totalBytes, 1);
			}		
		
	}
	
	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		try{
			log.debug("handling request...");
			
			ContentItem ci = new ContentItem();
			ServletFileUpload upload = new ServletFileUpload();
			
			ProgressAdapter progress = new ProgressAdapter();
			upload.setProgressListener(progress);
			FileItemIterator iter = upload.getItemIterator(request);
			FileItemStream fileStream = null;
			while(iter.hasNext()){
			    FileItemStream item = iter.next();
			    if(item.isFormField()){
			    	String value = Streams.asString(item.openStream());
			    	if(item.getFieldName().equals("contentId")){
						log.debug("setting contentId: {}", value);
			    		ci.setContentId(value);
			    	}else if(item.getFieldName().equals("contentMimetype")){
						log.debug("setting mimetype: {}", value);
			    		ci.setContentMimetype(value);
			    	}else if(item.getFieldName().equals("spaceId")){
						log.debug("setting spaceId: {}", value);
			    		ci.setSpaceId(value);
			    	}else if(item.getFieldName().equals("storeId")){
						log.debug("setting storeId: {}", value);
			    		ci.setStoreId(value);
			    	}
			    }else{
					log.debug("setting fileStream: {}", item);
			    	fileStream = item;
			    	break;
			    }
			}

			if(StringUtils.isBlank(ci.getContentId())){
				log.debug("contentId is blank. using upload filename: {}", fileStream.getName());
				ci.setContentId(fileStream.getName());
			}
			
			if(StringUtils.isBlank(ci.getContentMimetype())){
				log.debug("contentMimetype is blank. using content from header: {}", fileStream.getContentType());
				ci.setContentMimetype(fileStream.getContentType());
			}

			ContentStore contentStore = contentStoreManager.getContentStore(ci.getStoreId());
			ContentItemUploadTask task = new ContentItemUploadTask(ci, contentStore, fileStream.openStream(), request.getUserPrincipal().getName());
			progress.setListener(task);
			ContentUploadHelper.getManager(request).addUploadTask(task);
			task.execute();
	        ContentItem result = new ContentItem();
	        SpaceUtil.populateContentItem(ContentItemController.getBaseURL(request),result, ci.getSpaceId(), ci.getContentId(),contentStore, servicesManager);
	        return new ModelAndView("javascriptJsonView", "contentItem", ci);
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}

	}


}
