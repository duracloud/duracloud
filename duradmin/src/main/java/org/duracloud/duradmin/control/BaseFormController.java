/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ServicesManager;
import org.duracloud.duradmin.contentstore.ContentStoreProvider;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public abstract class BaseFormController
        extends SimpleFormController {

    protected final Logger log = LoggerFactory.getLogger(BaseFormController.class);

    private ControllerSupport controllerSupport;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response)
            throws Exception {
        ModelAndView mav = super.handleRequest(request, response);
        return controllerSupport.handle(mav, request, response);
    }

    public ContentStore getContentStore() throws ContentStoreException {
        return controllerSupport.getContentStore();
    }

    public ContentStoreProvider getContentStoreProvider() {
        return controllerSupport.getContentStoreProvider();
    }

    protected List<String> getSpaces() throws Exception {
        return controllerSupport.getSpaces();
    }

    protected ServicesManager getServicesManager() throws Exception {
        return controllerSupport.getServicesManager();
    }

    public void setControllerSupport(ControllerSupport controllerSupport) {
        this.controllerSupport = controllerSupport;
    }
}