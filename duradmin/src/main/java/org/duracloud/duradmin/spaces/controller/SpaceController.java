/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.StoreCaller;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.ExtendedIteratorCounterThread;
import org.duracloud.common.util.IOUtil;
import org.duracloud.controller.AbstractRestController;
import org.duracloud.domain.Content;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.domain.SpaceProperties;
import org.duracloud.duradmin.util.PropertiesUtils;
import org.duracloud.duradmin.util.ServiceUtil;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.execdata.bitintegrity.BitIntegrityResults;
import org.duracloud.execdata.bitintegrity.SpaceBitIntegrityResult;
import org.duracloud.execdata.bitintegrity.StoreBitIntegrityResults;
import org.duracloud.execdata.bitintegrity.serialize.BitIntegrityResultsSerializer;
import org.duracloud.execdata.mediastreaming.MediaStreamingConstants;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SpaceController extends  AbstractRestController<Space> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

	private ContentStoreManager contentStoreManager;
	private ServicesManager servicesManager;
	
	public SpaceController(){
		super(null);
		setValidator(new Validator(){
			@SuppressWarnings("unchecked")
			@Override
			public boolean supports(Class clazz) {
				return clazz == Space.class;
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				Space command = (Space)target;

		        if (!StringUtils.hasText(command.getStoreId())) {
		            errors.rejectValue("storeId","required");
		        }

				if (!StringUtils.hasText(command.getSpaceId())) {
		            errors.rejectValue("spaceId","required");
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


	
	

	@Override
	protected ModelAndView get(HttpServletRequest request,
			HttpServletResponse response, Space space,
			BindException errors) throws Exception {
		try{
			String prefix = request.getParameter("prefix");
			if(prefix != null){
				prefix = ("".equals(prefix.trim())?null:prefix);
			}
			String marker = request.getParameter("marker");
			ContentStore contentStore = contentStoreManager.getContentStore(space.getStoreId());
	         org.duracloud.domain.Space cloudSpace = contentStore.getSpace(space.getSpaceId(), prefix, 200, marker);
			populateSpace(space, cloudSpace, contentStore);
			populateSpaceCount(space, request);
			populateStreamEnabled(space);
            populateBitIntegrityResults(space);

			return createModel(space);
		}catch(ContentStoreException ex){
			ex.printStackTrace();
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return createModel(null);
		}
	}

    private void populateBitIntegrityResults(Space space) {
        try{
            String storeId = space.getStoreId();
            String spaceId = space.getSpaceId();
            ContentStore contentStore = this.contentStoreManager.getPrimaryContentStore();
            Content content = contentStore.getContent("x-duracloud-admin", "bit-integrity-results.json");
            InputStream is = content.getStream();
            BitIntegrityResultsSerializer serializer = new BitIntegrityResultsSerializer();
            String json = IOUtil.readStringFromStream(is);
            BitIntegrityResults results = serializer.deserialize(json);
            StoreBitIntegrityResults storeResults = results.getStores().get(storeId);
            if(storeResults != null){
                List<SpaceBitIntegrityResult> spaceResults = storeResults.getSpaceResults(spaceId);
                if(spaceResults != null && spaceResults.size() > 0){
                    for(SpaceBitIntegrityResult spaceResult : spaceResults){
                        if(spaceResult.isDisplay()){
                            
                            space.setBitIntegrityResult(spaceResult);
                            break;
                        }
                    }
                }
            }        
            
        }catch(Exception ex){
            log.error("failed to populate bit integrity results due to error:" + ex.getMessage(), ex);
        }
    }

    private void populateStreamEnabled(Space space) {
        boolean enabled = false;
        try {
            ServiceInfo info =
                ServiceUtil.findMediaStreamingService(this.servicesManager);
            enabled =
                ServiceUtil.isMediaStreamingServiceEnabled(info,
                                                           space.getSpaceId());
        }catch(NotFoundException e){
            enabled = false;
        }catch (ServicesException e) {
            throw new DuraCloudRuntimeException(e);
        }

        space.setStreamingEnabled(enabled);

    }
    


    private void populateSpace(Space space,
                               org.duracloud.domain.Space cloudSpace,
                               ContentStore contentStore)
        throws ContentStoreException {
        SpaceUtil.populateSpace(space,
                                cloudSpace,
                                contentStore,
                                getAuthentication());
    }
	
	
	private void populateSpaceCount(Space space, HttpServletRequest request) throws Exception{
		String countStr = space.getProperties().getCount();
		if(countStr.endsWith("+")){
			setItemCount(space, request);
		}else{
			space.setItemCount(Long.valueOf(space.getProperties().getCount()));
		}
	}

	private void setItemCount(final Space space, HttpServletRequest request) throws ContentStoreException{
		String key = space.getStoreId() + "/" + space.getSpaceId() + "/itemCountListener";
		ItemCounter listener = (ItemCounter)request.getSession().getAttribute(key);
		space.setItemCount(new Long(-1));
        if(listener != null){
            if(listener.isCountComplete()) {
                space.setItemCount(listener.getCount());
                request.getSession().removeAttribute(key);
            } else {
                SpaceProperties properties = space.getProperties();
                Long interCount = listener.getIntermediaryCount();
                if(interCount == null){
                    interCount = 0l;
                }
                if(interCount % 1000 != 0) {
                    interCount += 1;
                }
                properties.setCount(String.valueOf(interCount) + "+");
                space.setProperties(properties);
            }
		}else{
		    final ItemCounter itemCounterListener = new ItemCounter();
			request.getSession().setAttribute(key, itemCounterListener);
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

    @Override
    protected ModelAndView put(HttpServletRequest request,
                               HttpServletResponse response,
                               Space space,
                               BindException errors) throws Exception {

        String spaceId = space.getSpaceId();
        ContentStore contentStore = getContentStore(space);

        String method = request.getParameter("method");
        Map<String, String> properties =
            contentStore.getSpaceProperties(spaceId);
        PropertiesUtils.handle(method,
                               "space [" + spaceId + "]",
                               properties,
                               request);
        contentStore.setSpaceProperties(spaceId, properties);
        Space newSpace = new Space();
        populateSpace(newSpace,
                      contentStore.getSpace(spaceId, null, 0, null),
                      contentStore);
        return createModel(newSpace);
    }

	private Authentication getAuthentication() {
	    return (Authentication)SecurityContextHolder.getContext().getAuthentication();
    }

    protected ModelAndView post(HttpServletRequest request,
                                HttpServletResponse response,
                                Space space,
                                BindException errors) throws Exception {
        String spaceId = space.getSpaceId();
        ContentStore contentStore = getContentStore(space);
        contentStore.createSpace(spaceId, null);

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

	
	protected ModelAndView delete(HttpServletRequest request,
			HttpServletResponse response, Space space,
			BindException errors) throws Exception {
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

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }


}
