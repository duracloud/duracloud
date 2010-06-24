/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.services.beans.ComputeServiceBean;
import org.duracloud.services.util.ServiceSerializer;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.duracloud.services.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 */
public class ServicesAdminClient {

    private final Logger log = LoggerFactory.getLogger(ServicesAdminClient.class);

    private RestHttpHelper rester;

    private ServiceSerializer serializer;

    private String baseURL;

    public HttpResponse postServiceBundle(String fileName,
                                          InputStream stream,
                                          long length) throws Exception {
        log.debug("FILENAME: " + fileName + "\nSTREAM: " + stream);
        return getRester().multipartFileStreamPost(getInstallURL(),
                                                   fileName,
                                                   stream,
                                                   length);
    }

    public HttpResponse postServiceBundle(File file) throws Exception {
        log.debug("FILE: " + file);
        return getRester().multipartFilePost(getInstallURL(), file);
    }

    public HttpResponse deleteServiceBundle(String bundleId) throws Exception {
        log.debug("BUNDLE-ID: " + bundleId);

        String requestContent = getSerializedBean(bundleId);
        Map<String, String> headers = null;

        return getRester().post(getUninstallURL(), requestContent, headers);
    }

    public HttpResponse startServiceBundle(String bundleId) throws Exception {
        log.debug("BUNDLE-ID: " + bundleId);

        String requestContent = getSerializedBean(bundleId);
        Map<String, String> headers = null;

        return getRester().post(getStartURL(), requestContent, headers);
    }

    public HttpResponse stopServiceBundle(String bundleId) throws Exception {
        log.debug("BUNDLE-ID: " + bundleId);

        String requestContent = getSerializedBean(bundleId);
        Map<String, String> headers = null;

        return getRester().post(getStopURL(), requestContent, headers);
    }

    public HttpResponse getServiceListing() throws Exception {
        log.debug("Listing");
        return getRester().get(getListURL());
    }

    public Map<String, String> getServiceConfig(String configId)
        throws Exception {
        HttpResponse response = getRester().get(getConfigureURL(configId));
        // TODO: process erroneous responses

        String body = response.getResponseBody();
        log.debug("config for '" + configId + "': " + body);
        return SerializationUtil.deserializeMap(body);
    }

    public HttpResponse postServiceConfig(String configId,
                                          Map<String, String> config)
        throws Exception {
        if (log.isDebugEnabled()) {
            log.debug(postServiceConfigText(configId, config));
        }
        String body = SerializationUtil.serializeMap(config);
        Map<String, String> headers = null;

        log.debug("POST url: " + getConfigureURL(configId));

        return getRester().post(getConfigureURL(configId), body, headers);
    }

    public ComputeService.ServiceStatus getServiceStatus(String serviceId)
        throws Exception {
        HttpResponse response = getRester().get(getServiceStatusURL(serviceId));
        // TODO: process erroneous responses

        String body = response.getResponseBody();
        log.debug("status for '" + serviceId + "': " + body);
        return ComputeService.ServiceStatus.valueOf(body.trim());
    }

    public Map<String, String> getServiceProps(String serviceId)
        throws Exception {
        HttpResponse response = getRester().get(getServicePropsURL(serviceId));
        // TODO: process erroneous responses

        String body = response.getResponseBody();
        log.debug("props for '" + serviceId + "': " + body);
        return SerializationUtil.deserializeMap(body);
    }

    public boolean isServiceDeployed(String bundleId) throws Exception {
        boolean deployed = false;
        HttpResponse response = getServiceListing();
        if (response != null &&
            HttpURLConnection.HTTP_OK == response.getStatusCode()) {
            String body = response.getResponseBody();
            List<ComputeServiceBean> beans = getSerializer().deserializeList(
                body);

            String id = bundleId.substring(0, bundleId.lastIndexOf("."));
            for (ComputeServiceBean bean : beans) {
                if (bean.getServiceName().contains(id)) {
                    deployed = true;
                }
            }
        }
        return deployed;
    }

    private String getSerializedBean(String bundleId) throws Exception {
        ComputeServiceBean bean = new ComputeServiceBean(bundleId);
        return getSerializer().serialize(bean);
    }

    private String postServiceConfigText(String configId,
                                         Map<String, String> config) {
        StringBuffer sb = new StringBuffer();
        sb.append("Posting config for id: '" + configId + "'");
        for (String key : config.keySet()) {
            sb.append("\t[" + key + "|" + config.get(key) + "]\n");
        }
        return sb.toString();
    }

    private String getInstallURL() {
        return this.baseURL + "/services/install";
    }

    private String getUninstallURL() {
        return this.baseURL + "/services/uninstall";
    }

    private String getListURL() {
        return this.baseURL + "/services/list";
    }

    private String getStartURL() {
        return this.baseURL + "/services/start";
    }

    private String getStopURL() {
        return this.baseURL + "/services/stop";
    }

    private String getServiceStatusURL(String serviceId) {
        return this.baseURL + "/services/status/" + serviceId;
    }

    private String getServicePropsURL(String serviceId) {
        return this.baseURL + "/services/props/" + serviceId;
    }

    private String getConfigureURL(String configId) {
        return this.baseURL + "/services/configure/" + configId;
    }

    public RestHttpHelper getRester() {
        return rester;
    }

    public void setRester(RestHttpHelper rester) {
        this.rester = rester;
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

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

}
