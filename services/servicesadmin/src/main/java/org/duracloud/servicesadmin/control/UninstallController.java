/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.control;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.servicesadmin.util.HttpRequestHelper;
import org.duracloud.servicesutil.util.ServiceUninstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class UninstallController
        extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(UninstallController.class);

    private ServiceUninstaller serviceUninstaller;

    private HttpRequestHelper requestHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {

        ServletOutputStream out = response.getOutputStream();
        out.println("in uninstall-controller");
        out.close();

        String serviceId = getRequestHelper().getServiceIdParameter(request);
        this.getServiceUninstaller().uninstall(serviceId);

        return null;
    }

    public ServiceUninstaller getServiceUninstaller() {
        return serviceUninstaller;
    }

    public void setServiceUninstaller(ServiceUninstaller serviceUninstaller) {
        this.serviceUninstaller = serviceUninstaller;
    }

    public HttpRequestHelper getRequestHelper() {
        return requestHelper;
    }

    public void setRequestHelper(HttpRequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }

}
