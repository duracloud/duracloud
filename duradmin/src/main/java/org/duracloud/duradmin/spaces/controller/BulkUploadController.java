/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.controller.AbstractRestController;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class BulkUploadController extends AbstractRestController<Space> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ContentStoreManager contentStoreManager;

    private DuracloudUserDetailsService userDetailsService;

    public BulkUploadController() {
        super(null);
    }

    public ContentStoreManager getContentStoreManager() {
        return contentStoreManager;
    }

    public void setContentStoreManager(ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    @Override
    protected ModelAndView get(
        HttpServletRequest request, HttpServletResponse response, Space space,
        BindException errors) throws Exception {
        ModelAndView mav = new ModelAndView("upload-tool");
        try {
            ContentStore cs =
                contentStoreManager.getContentStore(space.getStoreId());
            mav.addObject("contentStore", cs);

            org.duracloud.domain.Space cloudSpace =
                cs.getSpace(space.getSpaceId(), null, 0, null);
            mav.addObject("space", space);

            String username =  SecurityContextHolder.getContext()
                    .getAuthentication().getName();
                   
            mav.addObject("user",
                this.userDetailsService.loadUserByUsername(username));

        } catch (ContentStoreException ex) {
            ex.printStackTrace();
        }

        return mav;
    }

    public DuracloudUserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(
        DuracloudUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

}
