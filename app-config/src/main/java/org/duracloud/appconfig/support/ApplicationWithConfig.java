package org.duracloud.appconfig.support;

import org.duracloud.appconfig.domain.AppConfig;
import org.duracloud.appconfig.domain.Application;

/**
 * @author: Andrew Woods
 * Date: Jun 25, 2010
 */
public class ApplicationWithConfig {

    private String name;
    private Application application;
    private AppConfig config;

    public ApplicationWithConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public AppConfig getConfig() {
        return config;
    }

    public void setConfig(AppConfig config) {
        this.config = config;
    }
}
