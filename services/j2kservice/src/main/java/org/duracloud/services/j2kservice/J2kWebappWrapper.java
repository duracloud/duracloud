/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice;

import org.apache.commons.io.FilenameUtils;
import static org.duracloud.common.web.NetworkUtil.waitForShutdown;
import static org.duracloud.common.web.NetworkUtil.waitForStartup;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.error.ServiceRuntimeException;
import org.duracloud.services.j2kservice.error.J2kWrapperException;
import org.duracloud.services.webapputil.WebAppUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * This class acts at the OSGi service representative of Djatoka webapp deployed
 * as a DuraCloud service.
 *
 * @author Andrew Woods
 *         Date: Dec 20, 2009
 */
public class J2kWebappWrapper extends BaseService implements ComputeService, ManagedService {

    private final Logger log = LoggerFactory.getLogger(J2kWebappWrapper.class);

    private URL url; // of running webapp    

    private String warName;
    private String j2kZipName;
    private String platform;

    private WebAppUtil webappUtil;

    private static final String DEFAULT_URL = "http://example.org";
    private static final String APACHE_FLAG = "WITH_APACHE";

    @Override
    public void start() throws Exception {
        log.debug("J2kWebappWrapper is Starting");
        this.setServiceStatus(ServiceStatus.STARTING);

        J2kInstallHelper helper = new J2kInstallHelper(getWorkDir(),
                                                       getJ2kZipName());

        Map<String, String> env;
        File war;
        env = helper.getInstallEnv(getPlatform());
        war = helper.getWarFile(getWarName());

        String context = FilenameUtils.getBaseName(war.getName());
        List<String> filterNames = getFilterNames();
        url = getWebappUtil().filteredDeploy(context,
                                             new FileInputStream(war),
                                             env,
                                             filterNames);

        waitForStartup(url.toString());
        this.setServiceStatus(ServiceStatus.STARTED);
    }

    private List<String> getFilterNames() {
        List<String> filterNames = new ArrayList<String>();
        filterNames.add("test.html");
        filterNames.add("viewer.html");
        return filterNames;
    }

    private File getWorkDir() {
        File work = new File(getServiceWorkDir());
        if (null == work || !work.exists()) {
            String msg = "Error finding work dir:" + work.getAbsolutePath();
            log.error(msg);
            throw new J2kWrapperException(msg);
        }

        return work;
    }

    @Override
    public void stop() throws Exception {
        log.debug("J2kWebappWrapper is Stopping");
        this.setServiceStatus(ServiceStatus.STOPPING);

        getWebappUtil().unDeploy(url);

        waitForShutdown(url.toString());
        setUrl(DEFAULT_URL);
        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();
        props.put("url", getPublicURL().toString());
        return props;
    }

    private URL getPublicURL() {
        String apacheFlag = System.getProperty(APACHE_FLAG);
        if (null == apacheFlag || apacheFlag.isEmpty()) {
            return url;
        }

        int port = url.getPort();
        String portSuffix = "";
        if (port != -1) {
            portSuffix = "-p" + port;
        }

        String protocol = url.getProtocol();
        String host = url.getHost();
        String file = url.getFile();

        try {
            return new URL(protocol, host, file + portSuffix);
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public String describe() throws Exception {
        log.debug("J2kWebappWrapper: Calling describe().");

        String baseDescribe = super.describe();
        return baseDescribe + "; Service url: '" + url + "'";
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        log.debug("J2kWebappWrapper updating config: ");
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
        sb.append("warName: " + warName);
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
        if (null == url) {
            setUrl(DEFAULT_URL);
        }
        return url.toString();
    }

    public void setUrl(String url) {
        log.debug("J2kWebappWrapper: setUrl (" + url + ")");
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public String getWarName() {
        return warName;
    }

    public void setWarName(String warName) {
        this.warName = warName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getJ2kZipName() {
        return j2kZipName;
    }

    public void setJ2kZipName(String j2kZipName) {
        this.j2kZipName = j2kZipName;
    }

}
