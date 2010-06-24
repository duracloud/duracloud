/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk;

import org.duracloud.common.util.ApplicationConfig;

import java.util.Properties;

/**
 * @author Andrew Woods
 * Date: Feb 8, 2010
 */
public class ChunkTestsConfig extends ApplicationConfig {
    private String propName = "test-chunk.properties";

    private Properties getProps() throws Exception {
        return getPropsFromResource(propName);
    }

    public String getPort() throws Exception {
        return getProps().getProperty("port");
    }
}