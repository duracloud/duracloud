/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.duradmin;

import java.io.IOException;

import org.duracloud.common.test.TestConfig;
import org.duracloud.common.test.TestConfigUtil;

/**
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class DuradminTestBase {
    
    private static String baseUrl;
    private TestConfig config;
    
    public DuradminTestBase() {
        try {
            this.config = new TestConfigUtil().getTestConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHost() {
        return this.config.getTestEndPoint().getHost();
    }

    private String getPort() {
        return this.config.getTestEndPoint().getPort()+"";
    }

    protected String getBaseUrl() {
        if (null == baseUrl) {
            baseUrl =
                "http" + (getPort().equals("443") ? "s" : "")
                      + "://"
                      + getHost()
                      + ":"
                      + getPort()
                      + "/duradmin";
        }
        return baseUrl;
    }
}
