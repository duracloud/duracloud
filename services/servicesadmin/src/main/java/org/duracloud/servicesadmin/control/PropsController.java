/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.control;

import org.duracloud.servicesadmin.util.HttpRequestHelper;
import org.duracloud.servicesutil.util.ServicePropsFinder;
import org.duracloud.common.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 18, 2009
 */
public class PropsController extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(PropsController.class);

    private ServicePropsFinder servicePropsFinder;
    private HttpRequestHelper requestHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
        throws Exception {
        ServletOutputStream out = response.getOutputStream();

        String serviceId = getRequestHelper().getServiceIdFromPropsURL(request);
        log.debug("Props for: " + serviceId);

        Map<String, String> props = getServicePropsFinder().getProps(serviceId);
        String serializedProps = SerializationUtil.serializeMap(props);

        out.print(serializedProps);
        out.close();
        return null;
    }

    public void setServicePropsFinder(ServicePropsFinder servicePropsFinder) {
        this.servicePropsFinder = servicePropsFinder;
    }

    public ServicePropsFinder getServicePropsFinder() {
        return servicePropsFinder;
    }

    public void setRequestHelper(HttpRequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }

    public HttpRequestHelper getRequestHelper() {
        return requestHelper;
    }
}
