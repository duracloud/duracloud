/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig;

import org.duracloud.appconfig.domain.AppConfig;
import org.duracloud.appconfig.domain.Application;
import org.duracloud.appconfig.domain.BaseConfig;
import org.duracloud.appconfig.domain.DuradminConfig;
import org.duracloud.appconfig.domain.DuraserviceConfig;
import org.duracloud.appconfig.domain.DurastoreConfig;
import org.duracloud.appconfig.domain.SecurityConfig;
import org.duracloud.appconfig.support.ApplicationWithConfig;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.util.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class loads the configuration properties of durastore, duradmin,
 * duraservice, and security and initializes each.
 *
 * @author Andrew Woods
 *         Date: Apr 22, 2010
 */
public class ApplicationInitializer extends BaseConfig {
    private final Logger log = LoggerFactory.getLogger(ApplicationInitializer.class);

    public static final String QUALIFIER = "app";

    private static final String duradminKey = "duradmin";
    private static final String duraserviceKey = "duraservice";
    private static final String durastoreKey = "durastore";

    protected static final String hostKey = "host";
    protected static final String portKey = "port";
    protected static final String contextKey = "context";
    protected static final String wildcardKey = "*";

    private String duradminHost;
    private String duradminPort;
    private String duradminContext;
    private String duraserviceHost;
    private String duraservicePort;
    private String duraserviceContext;
    private String durastoreHost;
    private String durastorePort;
    private String durastoreContext;

    private SecurityConfig securityConfig = new SecurityConfig();
    private Map<String, ApplicationWithConfig> appsWithConfigs = new HashMap<String, ApplicationWithConfig>();

    public ApplicationInitializer(File propsFile) throws IOException {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.SHA_256);
        Properties p = new Properties();
        p.load(new FileInputStream(propsFile));

        Map<String, String> props = new HashMap<String, String>();
        for (String key : p.stringPropertyNames()) {
            String value = p.get(key).toString();
            if(key.startsWith("security.user.") && key.endsWith(".password")) {
                value = util.generateChecksum(value);
            }
            props.put(key, value);
        }
        this.load(props);
    }

    /**
     * This method sets the configuration of duradmin, durastore, duraservice,
     * and application security from the provided props.
     * Note: this method is called by the constructor, so generally is should
     * not be needed publicly.
     *
     * @param props
     */
    public void load(Map<String, String> props) {
        super.load(props);
        createApplications();

        securityConfig.load(props);
        for (ApplicationWithConfig appWithConfig : appsWithConfigs.values()) {
            appWithConfig.getConfig().load(props);
        }
    }

    private void createApplications() {
        Application app;
        ApplicationWithConfig appWithConfig;

        if (duradminEndpointLoad()) {
            app = new Application(duradminHost, duradminPort, duradminContext);

            appWithConfig = new ApplicationWithConfig(duradminKey);
            appWithConfig.setApplication(app);
            appWithConfig.setConfig(new DuradminConfig());
            appsWithConfigs.put(appWithConfig.getName(), appWithConfig);
        } else {
            log.warn("duradmin endpoint not !loaded");
        }

        if (duraserviceEndpointLoad()) {
            app = new Application(duraserviceHost,
                                  duraservicePort,
                                  duraserviceContext);

            appWithConfig = new ApplicationWithConfig(duraserviceKey);
            appWithConfig.setApplication(app);
            appWithConfig.setConfig(new DuraserviceConfig());
            appsWithConfigs.put(appWithConfig.getName(), appWithConfig);
        } else {
            log.warn("duraservice endpoint !loaded");
        }

        if (durastoreEndpointLoad()) {
            app = new Application(durastoreHost,
                                  durastorePort,
                                  durastoreContext);

            appWithConfig = new ApplicationWithConfig(durastoreKey);
            appWithConfig.setApplication(app);
            appWithConfig.setConfig(new DurastoreConfig());
            appsWithConfigs.put(appWithConfig.getName(), appWithConfig);
        } else {
            log.warn("durastore endpoint !loaded");
        }
    }

    private boolean duradminEndpointLoad() {
        return null != duradminHost && null != duradminPort &&
            null != duradminContext;
    }

    private boolean duraserviceEndpointLoad() {
        return null != duraserviceHost && null != duraservicePort &&
            null != duraserviceContext;
    }

    private boolean durastoreEndpointLoad() {
        return null != durastoreHost && null != durastorePort &&
            null != durastoreContext;
    }

    protected String getQualifier() {
        return QUALIFIER;
    }

    protected void loadProperty(String key, String value) {
        String prefix = getPrefix(key);
        String suffix = getSuffix(key);

        if (prefix.equalsIgnoreCase(DuradminConfig.QUALIFIER)) {
            loadDuradmin(suffix, value);

        } else if (prefix.equalsIgnoreCase(DurastoreConfig.QUALIFIER)) {
            loadDurastore(suffix, value);

        } else if (prefix.equalsIgnoreCase(DuraserviceConfig.QUALIFIER)) {
            loadDuraservice(suffix, value);

        } else if (prefix.equalsIgnoreCase(wildcardKey)) {
            loadDuradmin(suffix, value);
            loadDurastore(suffix, value);
            loadDuraservice(suffix, value);

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadDuradmin(String key, String value) {
        String prefix = getPrefix(key);
        if (prefix.equalsIgnoreCase(hostKey)) {
            this.duradminHost = value;

        } else if (prefix.equalsIgnoreCase(portKey)) {
            this.duradminPort = value;

        } else if (prefix.equalsIgnoreCase(contextKey)) {
            this.duradminContext = value;

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadDurastore(String key, String value) {
        String prefix = getPrefix(key);
        if (prefix.equalsIgnoreCase(hostKey)) {
            this.durastoreHost = value;

        } else if (prefix.equalsIgnoreCase(portKey)) {
            this.durastorePort = value;

        } else if (prefix.equalsIgnoreCase(contextKey)) {
            this.durastoreContext = value;

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadDuraservice(String key, String value) {
        String prefix = getPrefix(key);
        if (prefix.equalsIgnoreCase(hostKey)) {
            this.duraserviceHost = value;

        } else if (prefix.equalsIgnoreCase(portKey)) {
            this.duraservicePort = value;

        } else if (prefix.equalsIgnoreCase(contextKey)) {
            this.duraserviceContext = value;

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    /**
     * This method initializes durastore, duraservice, and duradmin based
     * on the loaded configuration.
     *
     * @return
     */
    public RestHttpHelper.HttpResponse initialize() {
        RestHttpHelper.HttpResponse response = null;

        for (ApplicationWithConfig appWithConfig : appsWithConfigs.values()) {
            Application app = appWithConfig.getApplication();
            AppConfig config = appWithConfig.getConfig();

            response = app.initialize(config);
            validate(response, appWithConfig.getName());
        }

        return response;
    }

    private void validate(RestHttpHelper.HttpResponse response, String name) {
        if (null == response || response.getStatusCode() != 200) {
            String body = null;
            try {
                body = response.getResponseBody();
            } catch (IOException e) {
            } finally {
                StringBuilder msg = new StringBuilder("error initializing ");
                msg.append(name);
                msg.append(" (" + response.getStatusCode() + ")");
                if (null != body) {
                    msg.append("\n");
                    msg.append(body);
                }
                log.error(msg.toString());
                throw new DuraCloudRuntimeException(msg.toString());
            }
        }
    }

    /**
     * This method sets the security users from the loaded configuration.
     */
    public void setSecurityUsers() {
        RestHttpHelper.HttpResponse response = null;

        for (ApplicationWithConfig appWithConfig : appsWithConfigs.values()) {
            Application app = appWithConfig.getApplication();
            response = app.setSecurityUsers(securityConfig.getUsers());
            validate(response, appWithConfig.getName());
        }
    }

    /**
     * This method writes the configuration files for durastore, duraservice,
     * duradmin and application security to the provided directory.
     *
     * @param dir
     */
    public void outputXml(File dir) {
        for (ApplicationWithConfig appWithConfig : appsWithConfigs.values()) {
            String name = appWithConfig.getName();
            AppConfig config = appWithConfig.getConfig();
            write(new File(dir, name + "-init.xml"), config.asXml());
        }
        
        write(new File(dir, "security-init.xml"), securityConfig.asXml());
    }

    private void write(File file, String xml) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(xml);
            bw.close();

        } catch (IOException e) {
            String msg = "error writing init xml: " + file.getPath();
            log.error(msg, e);
            throw new DuraCloudRuntimeException(msg, e);
        }
    }

    public Application getDuradmin() {
        return appsWithConfigs.get(duradminKey).getApplication();
    }

    public Application getDurastore() {
        return appsWithConfigs.get(durastoreKey).getApplication();
    }

    public Application getDuraservice() {
        return appsWithConfigs.get(duraserviceKey).getApplication();
    }
}
