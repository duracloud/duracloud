/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

import org.duracloud.common.util.ApplicationConfig;

import java.util.Properties;

/**
 * This class abstracts the resource file that holds the mapping between
 *  exception keys and there MessageFormat patterns.
 *
 * @author Andrew Woods
 *         Date: Oct 24, 2009
 */
public class ExceptionMessages extends ApplicationConfig {

    private static String EXCEPTION_RESOURCE_NAME = "exception.properties";

    private static String configFileName;


    private static Properties getProps() throws Exception {
        return getPropsFromResource(getConfigFileName());
    }

    public static String getMessagePattern(String key) throws Exception {
        return getProps().getProperty(key);
    }

    public static void setConfigFileName(String name) {
        configFileName = name;
    }

    public static String getConfigFileName() {
        if (configFileName == null) {
            configFileName = EXCEPTION_RESOURCE_NAME;
        }
        return configFileName;
    }

}
