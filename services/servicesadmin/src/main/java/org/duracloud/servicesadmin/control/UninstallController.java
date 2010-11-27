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

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.servicesadmin.util.HttpRequestHelper;
import org.duracloud.servicesutil.util.ServiceUninstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import java.io.IOException;

/**
 * @author Andrew Woods
 *         Date: Jan 01, 2010
 */
public class UninstallController
        extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(UninstallController.class);

    public static final String ALL_SERVICES = "all";

    private ServiceUninstaller serviceUninstaller;

    private HttpRequestHelper requestHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {
        log.debug(request.getRequestURI() + ", method: " + request.getMethod());

        String msg = null;

        String serviceId = getServiceId(request);
        if (null == serviceId || serviceId.isEmpty()) {
            msg = "serviceId param is blank: " + request.getRequestURI();
            error(msg, response);
            return null;
        }

        try {
            if (ALL_SERVICES.equals(serviceId)) {
                this.getServiceUninstaller().uninstallAll();
                msg = ALL_SERVICES + " uninstalled";

            } else {
                this.getServiceUninstaller().uninstall(serviceId);
                msg = serviceId + " uninstalled";
            }
            success(msg, response);

        } catch (Exception e) {
            msg = "unable to uninstall " + serviceId + ". " + e.getMessage();
            error(msg, response);
        }

        return null;
    }

    private String getServiceId(HttpServletRequest request) throws Exception {
        String serviceId = null;
        try {
            serviceId = getRequestHelper().getServiceIdFromUninstallURL(request);
            
        } catch (Exception e) {
            log.error("Error from request-helper: " + e.getMessage());
        }
        return serviceId;
    }

    private void success(String msg, HttpServletResponse response)
        throws IOException {
        response.setStatus(HttpStatus.SC_OK);
        log.debug(msg);

        printMessage(msg, response);
    }

    private void error(String msg, HttpServletResponse response)
        throws IOException {
        response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        String err = "Error: " + msg;
        log.error(err);

        printMessage(err, response);
    }

    private void printMessage(String msg, HttpServletResponse response)
        throws IOException {
        ServletOutputStream out = response.getOutputStream();
        out.println(msg);
        out.close();
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
