/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.cloudsync;

import org.duracloud.services.cloudsync.error.CloudSyncWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Andrew Woods
 *         Date: Sep 20, 2011
 */
public class CloudSyncInstallHelper {

    private final Logger log =
        LoggerFactory.getLogger(CloudSyncInstallHelper.class);

    private File cloudSyncHome;

    public CloudSyncInstallHelper(File installDir) {
        this.cloudSyncHome = installDir;
    }

    protected Map<String, String> getInstallEnv() {
        Map<String, String> env = new HashMap<String, String>();

        String home = cloudSyncHome.getAbsolutePath();
        String javaOpts = "-Djava.awt.headless=true -Xmx512M -Xms64M";

        env.put("CLOUDSYNC_HOME", home);
        env.put("JAVA_OPTS", javaOpts);
        return env;
    }

    protected File getWarFile(String warName) {
        File warFile = new File(cloudSyncHome, warName);
        if (!warFile.exists()) {
            String msg = "Warfile does not exist: " + warFile.getAbsolutePath();
            log.error(msg);
            throw new CloudSyncWrapperException(msg);
        }
        return warFile;
    }

}
