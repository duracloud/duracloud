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
import org.springframework.web.servlet.ModelAndView;

public class ControllerSupport {

    private ContentStoreManager contentStoreManager;

    public ControllerSupport(ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    public ModelAndView handle(ModelAndView modelAndView,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        MessageUtils.addRedirectMessageToModelAndView(modelAndView, request);
        return modelAndView;
    }

    public ContentStoreManager getContentStoreManager() {
        return this.contentStoreManager;
    }
}
