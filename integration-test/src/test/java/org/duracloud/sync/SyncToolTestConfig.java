/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import org.duracloud.common.util.ApplicationConfig;

import java.util.Properties;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class SyncToolTestConfig  extends ApplicationConfig {
    private String propName = "test-synctool.properties";

    private Properties getProps() throws Exception {
        return getPropsFromResource(propName);
    }

    public String getPort() throws Exception {
        return getProps().getProperty("port");
    }
}
