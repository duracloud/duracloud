/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.control;

import java.io.IOException;

import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.servicesadmin.util.HttpRequestHelper;
import org.duracloud.servicesutil.util.DuraConfigAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ConfigureController
        extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(ConfigureController.class);

    private DuraConfigAdmin configAdmin;

    private HttpRequestHelper requestHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {
        if (isPost(request)) {
            log.debug("ConfigureController: is POST.");
            updateConfiguration(request);
        } else if (isGet(request)) {
            log.debug("ConfigureController: is GET.");
            getConfiguration(request, response);
        } else {
            log.warn("ConfigController: Unsupported :" + request.getMethod());
        }

        return null;
    }

    private void getConfiguration(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, Exception {
        ServletOutputStream out = response.getOutputStream();

        String configId = getRequestHelper().getConfigIdFromRestURL(request);
        Map<String, String> props = getConfigAdmin().getConfiguration(configId);
        String serializedConfig = SerializationUtil.serializeMap(props);

        out.print(serializedConfig);
        out.close();
    }

    private void updateConfiguration(HttpServletRequest request)
            throws Exception {
        String configId = getRequestHelper().getConfigIdFromRestURL(request);
        Map<String, String> props = getRequestHelper().getConfigProps(request);
        getConfigAdmin().updateConfiguration(configId, props);
    }

    private boolean isPost(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }

    private boolean isGet(HttpServletRequest request) {
        return "GET".equals(request.getMethod());
    }

    public DuraConfigAdmin getConfigAdmin() {
        return configAdmin;
    }

    public void setConfigAdmin(DuraConfigAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public HttpRequestHelper getRequestHelper() {
        return requestHelper;
    }

    public void setRequestHelper(HttpRequestHelper requestHelper) {
        this.requestHelper = requestHelper;
    }

}
