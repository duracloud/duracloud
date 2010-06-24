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

import org.duracloud.services.ComputeService;
import org.duracloud.services.util.ServiceSerializer;
import org.duracloud.servicesutil.util.ServiceLister;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ListController
        extends AbstractController {

    private ServiceLister serviceLister;

    private ServiceSerializer serializer;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {
        ServletOutputStream out = response.getOutputStream();
        java.util.List<ComputeService> services =
                getServiceLister().getDuraServices();
        out.println(getSerializer().serialize(services));

        out.close();
        return null;
    }

    public ServiceLister getServiceLister() {
        return serviceLister;
    }

    public void setServiceLister(ServiceLister serviceLister) {
        this.serviceLister = serviceLister;
    }

    public ServiceSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(ServiceSerializer serializer) {
        this.serializer = serializer;
    }

}
