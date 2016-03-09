/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.config;

import java.util.Properties;

import org.duracloud.common.rest.DuraCloudRequestContextUtil;
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



    private static boolean initialized = false;

    public static DuraCloudRequestContextUtil contextUtil = new DuraCloudRequestContextUtil();
    
    private static Properties getProps() {
        return ApplicationConfig.getPropsFromResource(getConfigFileName());
    }

    public static String getPropsHost() {
        return getProps().getProperty("host");
    }

    public static String getPropsPort() {
        return getProps().getProperty("port");
    }

    public static String getPropsDuraStoreContext() {
        return getProps().getProperty("durastoreContext", "durastore");
    }

    public static Boolean isPropsMillDbEnabled() {
        return Boolean.valueOf(getProps().getProperty("millDbEnabled", "true"));
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
        return contextUtil.getHost();
    }

    public static String getDuraStorePort() {
        return contextUtil.getPort()+"";
    }

    public static String getDuraStoreContext() {
        checkInitialized();
        return getPropsDuraStoreContext();
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
        config.setAmaUrl(null); // default is null.
        config.setMillDbEnabled(isPropsMillDbEnabled());
    }
}
