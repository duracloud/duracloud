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
import org.duracloud.servicesutil.util.ServiceStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class StartController
        extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(StartController.class);

    private ServiceStarter serviceStarter;

    private HttpRequestHelper requestHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {

        String serviceId = getRequestHelper().getServiceIdParameter(request);
        getServiceStarter().start(serviceId);

        ServletOutputStream out = response.getOutputStream();
        out.println("in start-controller");
        out.close();
        return null;
    }

    public ServiceStarter getServiceStarter() {
        return serviceStarter;
    }

    public void setServiceStarter(ServiceStarter serviceStarter) {
        this.serviceStarter = serviceStarter;
    }

    public HttpRequestHelper getRequestHelper() {
        return requestHelper;
    }

    public void setRequestHelper(HttpRequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }

}
