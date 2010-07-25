/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStore.AccessType;
import org.duracloud.controller.AbstractRestController;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.util.MetadataUtils;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.duradmin.util.TagUtil;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	public SpaceController(){
		super(null);
		setValidator(new Validator(){
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
		
		String prefix = request.getParameter("prefix");
		if(prefix != null){
			prefix = ("".equals(prefix.trim())?null:prefix);
		}
		
		String marker = request.getParameter("marker");
		org.duracloud.domain.Space cloudSpace = 
			contentStoreManager.getContentStore(space.getStoreId()).getSpace(space.getSpaceId(), prefix, 200, marker);
		SpaceUtil.populateSpace(space, cloudSpace);
		return createModel(space);
	}
	
	
	@Override
	protected ModelAndView put(HttpServletRequest request,
			HttpServletResponse response, Space space,
			BindException errors) throws Exception {
		String spaceId = space.getSpaceId();
        ContentStore contentStore = getContentStore(space);
        
        String method = request.getParameter("method");
        if("changeAccess".equals(method)){
            String access = space.getAccess();
            if(access !=null){
                contentStore.setSpaceAccess(spaceId, AccessType.valueOf(access));
            }
            return createModel(space);
        }else{ 
        	Map<String,String> metadata  = contentStore.getSpaceMetadata(spaceId);
        	MetadataUtils.handle(method, "space ["+spaceId+"]",  metadata, request);
        	contentStore.setSpaceMetadata(spaceId, metadata);
            Space newSpace = new Space();
            SpaceUtil.populateSpace(newSpace, contentStore.getSpace(spaceId,
                    null,
                    0,
                    null));

    		return createModel(newSpace);

        }
       
	}

	protected ModelAndView post(HttpServletRequest request,
			HttpServletResponse response, Space space,
			BindException errors) throws Exception {
		String spaceId = space.getSpaceId();
        ContentStore contentStore = getContentStore(space);
        contentStore.createSpace(spaceId, null);
        contentStore.setSpaceAccess(spaceId, AccessType.valueOf(space
                .getAccess()));
        SpaceUtil.populateSpace(space, contentStore.getSpace(spaceId,
                                                             null,
                                                             0,
                                                             null));
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


}
