/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.util;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.services.beans.ComputeServiceBean;
import org.duracloud.services.util.ServiceSerializer;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class HttpRequestHelper {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestHelper.class);

    private ServiceSerializer serializer;

    public String getServiceIdFromStatusURL(HttpServletRequest request)
        throws Exception {
        String prefix = "status/";
        return getSuffixFromURL(request, prefix);
    }

    public String getServiceIdFromPropsURL(HttpServletRequest request)
        throws Exception {
        String prefix = "props/";
        return getSuffixFromURL(request, prefix);
    }

    public String getConfigIdFromRestURL(HttpServletRequest request)
        throws Exception {
        String prefix = "configure/";
        return getSuffixFromURL(request, prefix);
    }

    private String getSuffixFromURL(HttpServletRequest request, String prefix)
        throws Exception {
        String pathInfo = request.getPathInfo();
        log.debug("getSuffixFromURL, pathInfo: '" + pathInfo + "'");

        int index = pathInfo.indexOf(prefix);
        if (index == -1) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unable to find prefix [");
            msg.append(prefix + "]");
            msg.append("in URL [" + pathInfo + "]");
            log.error(msg.toString());
            throw new Exception(msg.toString());
        }
        String suffix = pathInfo.substring(index + prefix.length());
        log.debug("getSuffixFromURL, found suffix: '" + suffix + "'");
        return suffix;
    }

    public Map<String, String> getConfigProps(HttpServletRequest request)
        throws Exception {

        String content = getRequestContent(request);
        log.debug("getConfigProps(request) content: '" + content + "'");
        Map<String, String> props = SerializationUtil.deserializeMap(content);

        return props;
    }

    public String getServiceIdParameter(HttpServletRequest request)
        throws Exception {

        String content = getRequestContent(request);
        log.debug("getServiceIdParameter(request) content: '" + content + "'");
        ComputeServiceBean bean = getSerializer().deserializeBean(content);
        return bean.getServiceName();
    }

    private String getRequestContent(HttpServletRequest request)
        throws Exception {
        int len = request.getContentLength();
        if (len < 1) {
            String msg = "Error: No request content was provided.";
            log.error(msg);
            throw new Exception(msg);
        }

        byte[] buffer = new byte[len];
        int bytesRead = 0;
        while (bytesRead != -1) {
            bytesRead += request.getInputStream().readLine(buffer,
                                                           bytesRead,
                                                           len);
        }
        return new String(buffer);
    }

    public ServiceSerializer getSerializer() {
        if (serializer == null) {
            serializer = new XMLServiceSerializerImpl();
        }
        return serializer;
    }

    public void setSerializer(ServiceSerializer serializer) {
        this.serializer = serializer;
    }
}
