/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.common.util.ExceptionUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.security.xml.SecurityUsersDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Collection;

/**
 * This class provides an abstraction of an application that is reachable
 * at a host:port/context endpoint and can be initialized and can have its
 * security-users updated.
 *
 * @author Andrew Woods
 *         Date: Apr 22, 2010
 */
public class Application {
    private final Logger log = LoggerFactory.getLogger(Application.class);

    private String host;
    private String port;
    private String context;

    private RestHttpHelper restHelper;

    public Application(String host, String port, String context) {
        this.host = host;
        this.port = port;
        this.context = context;
    }

    /**
     * This method initializes this application with the provided configuration.
     *
     * @param config
     * @return
     */
    public RestHttpHelper.HttpResponse initialize(AppConfig config) {
        String xml = config.asXml();
        String url = getInitUrl(config);
        try {
            Map<String, String> headers = null;
            return getRestHelper().post(url, xml, headers);

        } catch (Exception e) {
            log.error("error initializing app at: " + url,
                      ExceptionUtil.getStackTraceAsString(e));
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * This method sets the security users of this application to the provided
     * users.
     *
     * @param users
     * @return
     */
    public RestHttpHelper.HttpResponse setSecurityUsers(Collection<SecurityUserBean> users) {
        String xml = SecurityUsersDocumentBinding.createDocumentFrom(users);
        try {
            Map<String, String> headers = null;
            return getRestHelper().post(getSecurityUrl(), xml, headers);

        } catch (Exception e) {
            log.error("error initializing durastore security",
                      ExceptionUtil.getStackTraceAsString(e));
            throw new DuraCloudRuntimeException(e);
        }
    }

    private String getInitUrl(AppConfig config) {
        return getBaseUrl() + config.getInitResource();
    }

    private String getBaseUrl() {
        return getProtocol() + getHost() + ":" + getPort() + "/" + getContext();
    }

    private String getProtocol() {
        String protocol = "http://";
        if (getPort().equals("443")) {
            protocol = "https://";
        }
        return protocol;
    }

    private String getSecurityUrl() {
        return getBaseUrl() + "/security";
    }

    private RestHttpHelper getRestHelper() {
        if (null == restHelper) {
            restHelper = new RestHttpHelper(new RootUserCredential());
        }
        return restHelper;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getContext() {
        return context;
    }
}
