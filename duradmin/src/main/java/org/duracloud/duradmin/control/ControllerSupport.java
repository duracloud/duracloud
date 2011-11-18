/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.duradmin.util.MessageUtils;
import org.duracloud.serviceapi.ServicesManager;
import org.springframework.web.servlet.ModelAndView;

public class ControllerSupport {

    private ServicesManager servicesManager;
    private ContentStoreManager contentStoreManager; 
    
    public ControllerSupport(ServicesManager servicesManager, ContentStoreManager contentStoreManager ) {
        this.servicesManager = servicesManager;
        this.contentStoreManager = contentStoreManager;
    }



    public ModelAndView handle(ModelAndView modelAndView,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        MessageUtils.addRedirectMessageToModelAndView(modelAndView, request);
        return modelAndView;
    }

    /**
     * This method returns the current service-manager.
     * It is highly suggested that callers of this method not cache the object
     * returned here, since subsequent service-host/port/context updates will
     * render the cached object obsolete.
     *
     * @return
     * @throws Exception
     */
    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }



    public ContentStoreManager getContentStoreManager() {
        return this.contentStoreManager;
    }
}
