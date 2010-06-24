/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.control;

import org.duracloud.services.ComputeService;
import org.duracloud.servicesadmin.util.HttpRequestHelper;
import org.duracloud.servicesutil.util.ServiceStatusReporter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Andrew Woods
 *         Date: Dec 14, 2009
 */
public class StatusController extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(StatusController.class);

    private ServiceStatusReporter serviceStatusReporter;
    private HttpRequestHelper requestHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
        throws Exception {
        ServletOutputStream out = response.getOutputStream();

        String serviceId = getRequestHelper().getServiceIdFromStatusURL(request);
        log.debug("Status for: " + serviceId);

        ComputeService.ServiceStatus status;
        status = getServiceStatusReporter().getStatus(serviceId);
        
        out.println(status.name());
        out.close();
        return null;
    }

    public ServiceStatusReporter getServiceStatusReporter() {
        return serviceStatusReporter;
    }

    public void setServiceStatusReporter(ServiceStatusReporter serviceStatusReporter) {
        this.serviceStatusReporter = serviceStatusReporter;
    }

    public HttpRequestHelper getRequestHelper() {
        return requestHelper;
    }

    public void setRequestHelper(HttpRequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }
}
