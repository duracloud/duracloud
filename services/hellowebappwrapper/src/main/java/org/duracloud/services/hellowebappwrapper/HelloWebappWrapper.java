/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hellowebappwrapper;

import org.apache.commons.io.FilenameUtils;
import static org.duracloud.common.web.NetworkUtil.waitForShutdown;
import static org.duracloud.common.web.NetworkUtil.waitForStartup;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.error.ServiceRuntimeException;
import org.duracloud.services.hellowebappwrapper.error.WebappWrapperException;
import org.duracloud.services.webapputil.WebAppUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

/**
 * This class acts at the OSGi service representative of webapp deployed as
 * a DuraCloud service.
 *
 * @author Andrew Woods
 *         Date: Dec 10, 2009
 */
public class HelloWebappWrapper extends BaseService implements ComputeService, ManagedService {

    private final Logger log = LoggerFactory.getLogger(HelloWebappWrapper.class);

    private String webappWarName;
    private URL url; // of running webapp
    private WebAppUtil webappUtil;
    private static final String DEFAULT_URL = "http://example.org";

    @Override
    public void start() throws Exception {
        log.debug("HelloWebappWrapper is Starting");
        this.setServiceStatus(ServiceStatus.STARTING);

        File war = getWarFile();
        String context = FilenameUtils.getBaseName(war.getName());
        url = getWebappUtil().deploy(context, new FileInputStream(war));

        waitForStartup(url.toString());
        this.setServiceStatus(ServiceStatus.STARTED);
    }

    private File getWarFile() {
        File warFile = new File(getServiceWorkDir(), getWebappWarName());
        if (!warFile.exists()) {
            String msg = "Warfile does not exist: " + warFile.getAbsolutePath();
            log.error(msg);
            throw new WebappWrapperException(msg);
        }
        return warFile;
    }

    @Override
    public void stop() throws Exception {
        log.debug("HelloWebappWrapper is Stopping");
        this.setServiceStatus(ServiceStatus.STOPPING);

        getWebappUtil().unDeploy(url);

        waitForShutdown(url.toString());
        setUrl(DEFAULT_URL);
        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();
        props.put("url", url.toString());
        return props;
    }

    @Override
    public String describe() throws Exception {
        log.debug("HelloWebappWrapper: Calling describe().");

        String baseDescribe = super.describe();
        return baseDescribe + "; Service url: '" + url + "'";
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        log.debug("HelloWebappWrapper updating config: ");
        if (config != null) {
            Enumeration keys = config.keys();
            {
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String val = (String) config.get(key);
                    log.info(" [" + key + "|" + val + "] ");
                }
            }
        } else {
            log.info("config is null.");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().toString());
        sb.append(":\n\t");
        sb.append("webappWarName: " + webappWarName);
        sb.append("\n\t");
        sb.append("workDir: " + getServiceWorkDir());
        sb.append("\n\t");
        String wau = webappUtil == null ? "null" : webappUtil.toString();
        sb.append("webappUtil: " + wau);
        sb.append("\n\t");
        sb.append("url: " + url.toString());
        sb.append("\n");
        return sb.toString();
    }

    public WebAppUtil getWebappUtil() {
        return webappUtil;
    }

    public void setWebappUtil(WebAppUtil webappUtil) {
        this.webappUtil = webappUtil;
    }

    public String getUrl() {
        return url.toString();
    }

    public void setUrl(String url) {
        log.debug("HelloWebappWrapper: setUrl (" + url + ")");
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public String getWebappWarName() {
        return webappWarName;
    }

    public void setWebappWarName(String webappWarName) {
        this.webappWarName = webappWarName;
    }

}
