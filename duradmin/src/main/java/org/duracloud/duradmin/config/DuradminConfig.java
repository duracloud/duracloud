/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.config;

import java.util.Properties;

import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.duradmin.domain.AdminInit;

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

    private static String durabossContextKey = "durabossContext";
    
    private static String millDbEnabledKey = "millDbEnabled";

    private static boolean initialized = false;

    private static Properties getProps() {
        return ApplicationConfig.getPropsFromResource(getConfigFileName());
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
    
    public static Boolean isPropsMillDbEnabled() {
        return Boolean.valueOf(getProps().getProperty(millDbEnabledKey));
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
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
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

    public static boolean isMillDbEnabled() {
        checkInitialized();
        return config.isMillDbEnabled();
    }

    public String getAmaUrl() {
        checkInitialized();
        return config.getAmaUrl();
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
        config.setDuraBossContext(getPropsDuraBossContext());
        config.setAmaUrl(null); // default is null.
        config.setMillDbEnabled(isPropsMillDbEnabled());
    }


    private static String getPropsDuraBossContext() {
        return getProps().getProperty(durabossContextKey, "duraboss");
    }

    public static String getDuraBossHost() {
        return getPropsHost();
    }

    public static String getDuraBossPort() {
        return getPropsPort();
    }

    public static String getDuraBossContext() {
        return getPropsDuraBossContext();
    }

}
