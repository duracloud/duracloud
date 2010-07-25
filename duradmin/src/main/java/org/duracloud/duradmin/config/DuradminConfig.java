/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.config;

import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.duradmin.domain.AdminInit;

import java.util.Properties;

/**
 * This class provides configuration properties associated with the duracloud
 * duradmin.
 * 
 * @author awoods
 */
public class DuradminConfig
        extends ApplicationConfig {

    private static AdminInit config = null;

    private static String DURADMIN_PROPERTIES_NAME = "duradmin.properties";

    private static String configFileName;

    private static String hostKey = "host";

    private static String portKey = "port";

    private static String durastoreContextKey = "durastoreContext";

    private static String duraserviceContextKey = "duraserviceContext";

    private static Properties getProps() {
        return getPropsFromResource(getConfigFileName());
    }

    public static String getPropsHost() {
        return getProps().getProperty(hostKey);
    }

    public static String getPropsPort() {
        return getProps().getProperty(portKey);
    }

    public static String getPropsDuraStoreContext() {
        return getProps().getProperty(durastoreContextKey);
    }

    public static String getPropsDuraServiceContext() {
        return getProps().getProperty(duraserviceContextKey);
    }

    public static void setConfigFileName(String name) {
        configFileName = name;
    }

    public static String getConfigFileName() {
        if (configFileName == null) {
            configFileName = DURADMIN_PROPERTIES_NAME;
        }
        return configFileName;
    }

    public static void setConfig(AdminInit init) {
        config = init;
    }

    public static String getDuraStoreHost() {
        checkInitialized();
        return config.getDuraStoreHost();
    }

    public static String getDuraStorePort() {
        checkInitialized();
        return config.getDuraStorePort();
    }

    public static String getDuraStoreContext() {
        checkInitialized();
        return config.getDuraStoreContext();
    }

    public static String getDuraServiceHost() {
        checkInitialized();
        return config.getDuraServiceHost();
    }

    public static String getDuraServicePort() {
        checkInitialized();
        return config.getDuraServicePort();
    }

    public static String getDuraServiceContext() {
        checkInitialized();
        return config.getDuraServiceContext();
    }

    private static void checkInitialized() {
        if(config == null) {
            initFromProperties();
        }
    }

    private static void initFromProperties() {
        config = new AdminInit();
        config.setDuraStoreHost(getPropsHost());
        config.setDuraStorePort(getPropsPort());
        config.setDuraStoreContext(getPropsDuraStoreContext());
        config.setDuraServiceHost(getPropsHost());
        config.setDuraServicePort(getPropsPort());
        config.setDuraServiceContext(getPropsDuraServiceContext());
    }

}
