/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

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

    public String getAmaUrl() {
        String mcSchema = (getMcPort() == "443") ? "https" : "http";
        return mcSchema + "://" + getMcHost();
    }

}
