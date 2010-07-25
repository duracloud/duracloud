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
import org.duracloud.duradmin.util.MessageUtils;
import org.duracloud.error.ContentStoreException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

public class ControllerSupport {

    private ContentStoreProvider contentStoreProvider;
    private ServicesManager servicesManager;

    public ControllerSupport(ContentStoreProvider contentStoreProvider,
                             ServicesManager servicesManager) {
        this.contentStoreProvider = contentStoreProvider;
        this.servicesManager = servicesManager;
    }

    public ContentStore getContentStore() throws ContentStoreException {
        return getContentStoreProvider().getContentStore();
    }

    public ContentStoreProvider getContentStoreProvider() {
        return contentStoreProvider;
    }

    public void setContentStoreProvider(ContentStoreProvider contentStoreProvider) {
        this.contentStoreProvider = contentStoreProvider;
    }

    protected List<String> getSpaces() throws Exception {
        List<String> spaces = getContentStore().getSpaces();
        Collections.sort(spaces);
        return spaces;
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
}
