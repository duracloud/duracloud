/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.tomcat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.util.IOUtil;
import static org.duracloud.common.web.NetworkUtil.waitForShutdown;
import static org.duracloud.common.web.NetworkUtil.waitForStartup;
import org.duracloud.services.webapputil.error.WebAppDeployerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a stateful representation of a tomcat instance.
 * It may be started or stopped,
 * and
 * webapps may be deployed to it or undeployed from it.
 *
 * @author Andrew Woods
 *         Date: Dec 2, 2009
 */
public class TomcatInstance {

    private final Logger log = LoggerFactory.getLogger(TomcatInstance.class);

    private File catalinaHome;
    private int port;

    public TomcatInstance(File catalinaHome, int port) {
        this.catalinaHome = catalinaHome;
        this.port = port;
    }

    /**
     * This method starts the tomcat instance.
     * i.e. ./bin/startup.[sh|bat]
     */
    public void start() {
        this.start(new HashMap<String, String>());
    }

    public void start(Map<String, String> env) {
        runScript(getStartUpScript(), env);
        try {
            waitForStartup(getLocalUrl());
        } catch (DuraCloudCheckedException e) {
            throw new WebAppDeployerException("Unable to start tomcat", e);
        }
    }

    /**
     * This  method shutsdown the tomcat instance.
     * i.e. ./bin/shutdown.[sh|bat]
     */
    public void stop() {
        runScript(getShutdownScript());
        try {
            waitForShutdown(getLocalUrl());
        } catch (DuraCloudCheckedException e) {
            throw new WebAppDeployerException("Unable to stop tomcat", e);
        }
    }

    private String getLocalUrl() {
        return "http://localhost:" + port;
    }

    private void runScript(File script) {
        this.runScript(script, new HashMap<String, String>());
    }

    private void runScript(File script, Map<String, String> env) {
        String catalina = catalinaHome.getAbsolutePath();
        String java = System.getProperty("java.home");
        if (java.endsWith("jre")) {
            java = java.substring(0, java.lastIndexOf(File.separatorChar));
        }
        String scriptPath = script.getAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        Map<String, String> tomcatEnv = pb.environment();
        tomcatEnv.put("CATALINA_HOME", catalina);
        tomcatEnv.put("JAVA_HOME", java);

        for (String key : env.keySet()) {
            tomcatEnv.put(key, env.get(key));
        }

        try {
            // Caution: Attempting to use the process object returned
            // by this call for anything, even a null check, 
            // may cause tests to fail
            pb.start();
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error running script: \n -- ");
            sb.append(script.getAbsolutePath());
            sb.append("\n -- catalinaHome: ");
            sb.append(catalinaHome.getAbsolutePath());
            sb.append("\n -- " + e.getMessage());
            log.error(sb.toString());
            throw new WebAppDeployerException(sb.toString(), e);
        }

    }

    private File getStartUpScript() {
        String os = System.getProperty("os.name");
        String filename = "startup.sh";
        if (os != null && os.toLowerCase().contains("windows")) {
            filename = "startup.bat";
        }
        return getScript(filename);
    }

    private File getShutdownScript() {
        String os = System.getProperty("os.name");
        String filename = "shutdown.sh";
        if (os != null && os.toLowerCase().contains("windows")) {
            filename = "shutdown.bat";
        }
        return getScript(filename);
    }

    private File getScript(String filename) {
        File script = new File(getBinDir(), filename);
        if (!script.exists()) {
            String msg = "script not found:" + script.getAbsolutePath();
            log.error(msg);
            throw new WebAppDeployerException(msg);
        }
        return script;
    }

    private File getBinDir() {
        return new File(catalinaHome, "bin");
    }

    /**
     * This method deploys the arg war into the appserver under the arg context.
     * The arg war inputstream is closed by this method.
     *
     * @param context of deployed webapp
     * @param war     to be deployed
     */
    public void deploy(String context, InputStream war) {
        File webappsDir = new File(catalinaHome, "webapps");
        File warFile = new File(webappsDir, context + ".war");
        OutputStream output = IOUtil.getOutputStream(warFile);
        IOUtil.copy(war, output);

        IOUtils.closeQuietly(war);
        IOUtils.closeQuietly(output);
    }

    /**
     * This method undeploys the webapp found under the arg context
     *
     * @param context to be undeployed
     */
    public void unDeploy(String context) {
        File webappsDir = new File(catalinaHome, "webapps");
        File warFile = new File(webappsDir, context + ".war");
        File warDir = new File(webappsDir, context);

        FileUtils.deleteQuietly(warFile);
        try {
            FileUtils.deleteDirectory(warDir);
        } catch (IOException e) {
            log.warn("Error deleting warDir: " + warDir.getAbsolutePath(), e);
        }
    }

    public File getCatalinaHome() {
        return catalinaHome;
    }

    public int getPort() {
        return port;
    }

}
