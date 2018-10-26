/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.apache.commons.lang3.StringUtils;

import org.springframework.core.env.Environment;

/**
 * @author Nicholas Woodward
 */
public class DuracloudConfigBean {

    protected Environment env;

    public DuracloudConfigBean(Environment env) {
        this.env = env;
    }

    public String getMcHost() {
        return env.getProperty("mc.host");
    }

    public String getMcPort() {
        return env.getProperty("mc.port");
    }

    public String getMcContext() {
        return env.getProperty("mc.context");
    }

    public String getAmaUrl() {
        String port = getMcPort();
        String context = getMcContext();
        String amaUrl = new String();

        switch (port) {
            case "80":
                amaUrl = "http://" + getMcHost();
                break;
            case "443":
                amaUrl = "https://" + getMcHost();
                break;
            default:
                amaUrl = "http://" + getMcHost() + ":" + port;
                break;
        }

        if (!StringUtils.isEmpty(context)) {
            amaUrl += "/" + context;
        }

        return amaUrl;
    }

}
