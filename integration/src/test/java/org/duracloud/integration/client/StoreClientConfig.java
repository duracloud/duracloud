/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.client;

import java.util.Properties;

import org.duracloud.common.util.ApplicationConfig;

/**
 * @author: Bill Branan
 * Date: Nov 23, 2009
 */
public class StoreClientConfig extends ApplicationConfig {
    private String propName = "test-client.properties";

    private Properties getProps() throws Exception {
        return getPropsFromResource(propName);
    }

    public String getPort() throws Exception {
        return getProps().getProperty("port");
    }
}
