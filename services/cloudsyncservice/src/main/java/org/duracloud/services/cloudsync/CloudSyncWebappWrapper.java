/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.cloudsync;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.common.web.NetworkUtil;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.cloudsync.error.CloudSyncWrapperException;
import org.duracloud.services.common.error.ServiceRuntimeException;
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
 * This class acts as the OSGi service representative of the CloudSync webapp
 * deployed as a DuraCloud service.
 *
 * @author Andrew Woods
 *         Date: Sep 20, 2011
 */
public class CloudSyncWebappWrapper extends BaseService implements ComputeService, ManagedService {

    private final Logger log =
        LoggerFactory.getLogger(CloudSyncWebappWrapper.class);

    private URL url; // of running webapp    

    private String warName;
    private String portIndex;

    private WebAppUtil webappUtil;
    private NetworkUtil networkUtil;
    private CloudSyncInstallHelper installHelper;

    private static final String DEFAULT_URL = "http://example.org";

    @Override
    public void start() throws Exception {
        log.debug("CloudSyncWebappWrapper is Starting");
        this.setServiceStatus(ServiceStatus.STARTING);

        CloudSyncInstallHelper helper = getInstallHelper(getWorkDir());

        Map<String, String> env;
        File war;
        env = helper.getInstallEnv();
        war = helper.getWarFile(getWarName());

        String context = FilenameUtils.getBaseName(war.getName());
        url = getWebappUtil().deploy(context,
                                     getPortIndex(),
                                     new FileInputStream(war),
                                     env);

        getNetworkUtil().waitForStartup(url.toString());
        super.start();
        setServiceStatus(ServiceStatus.STARTED);
    }

    private File getWorkDir() {
        File work = new File(getServiceWorkDir());
        if (!work.exists()) {
            String msg = "Error finding work dir:" + work.getAbsolutePath();
            log.error(msg);
            throw new CloudSyncWrapperException(msg);
        }

        return work;
    }

    @Override
    public void stop() throws Exception {
        log.debug("CloudSyncWebappWrapper is Stopping");
        this.setServiceStatus(ServiceStatus.STOPPING);

        getWebappUtil().unDeploy(url);

        getNetworkUtil().waitForShutdown(url.toString());
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
        try {
            return new URL(url.toString() + "/login");
        } catch (MalformedURLException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public String describe() throws Exception {
        log.debug("CloudSyncWebappWrapper: Calling describe().");

        String baseDescribe = super.describe();
        return baseDescribe + "; Service url: '" + url + "'";
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        log.debug("CloudSyncWebappWrapper updating config: ");
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

    public NetworkUtil getNetworkUtil() {
        if (null == networkUtil) {
            networkUtil = new NetworkUtil();
        }
        return networkUtil;
    }

    public void setNetworkUtil(NetworkUtil networkUtil) {
        this.networkUtil = networkUtil;
    }

    private CloudSyncInstallHelper getInstallHelper(File workDir) {
        if (null == installHelper) {
            installHelper = new CloudSyncInstallHelper(workDir);
        }
        return installHelper;
    }

    public void setInstallHelper(CloudSyncInstallHelper installHelper) {
        this.installHelper = installHelper;
    }

    public void setUrl(String url) {
        log.debug("CloudSyncWebappWrapper: setUrl (" + url + ")");
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

    public String getPortIndex() {
        return portIndex;
    }

    public void setPortIndex(String portIndex) {
        this.portIndex = portIndex;
    }

}
