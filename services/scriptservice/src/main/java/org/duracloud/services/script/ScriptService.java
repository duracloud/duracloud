/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.script;

import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.script.error.ScriptServiceException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;

/**
 * Service which handles the calling of OS-dependent scripts in order
 * to deploy and undeploy utilities within the DuraCloud service environment
 *
 * @author Bill Branan
 *         Date: Dec 11, 2009
 */
public class ScriptService extends BaseService implements ComputeService, ManagedService {

    public static enum OS {
        Windows, Linux
    }
    
    private static final String START_SCRIPT = "start";
    private static final String STOP_SCRIPT = "stop";
    private static final String WIN_EXT = ".bat";
    private static final String LIN_EXT = ".sh";

    private final Logger log = LoggerFactory.getLogger(ScriptService.class);

    @Override
    public void start() throws Exception {
        File workDir = new File(getServiceWorkDir());
        log.info("Starting Script Service: " + workDir.getName());

        this.setServiceStatus(ServiceStatus.STARTING);

        String startScriptName = getScriptName(START_SCRIPT);
        checkScriptExists(startScriptName);
        String script = new File(workDir, startScriptName).getAbsolutePath();
        log.info("Running Script: " + script);

        ProcessBuilder pb = new ProcessBuilder(script);
        pb.directory(workDir);
        Process p = pb.start();

        this.setServiceStatus(ServiceStatus.STARTED);
    }

    @Override
    public void stop() throws Exception {
        File workDir = new File(getServiceWorkDir());
        log.info("Stopping Script Service: " + workDir.getName());

        this.setServiceStatus(ServiceStatus.STOPPING);

        String stopScriptName = getScriptName(STOP_SCRIPT);
        checkScriptExists(stopScriptName);
        String script = new File(workDir, stopScriptName).getAbsolutePath();
        log.info("Running Script: " + script);

        ProcessBuilder pb = new ProcessBuilder(script);
        pb.directory(workDir);
        Process p = pb.start();

        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    private String getScriptName(String script) {
        OS os = determineOS();
        String scriptFileName = script;

        if (os.equals(OS.Linux)) { // Linux
            scriptFileName = scriptFileName + LIN_EXT;
        } else { // Windows
            scriptFileName = scriptFileName + WIN_EXT;
        }

        return scriptFileName;
    }

    private void checkScriptExists(String scriptName) {
        File scriptFile = new File(getServiceWorkDir(), scriptName);
        if (!scriptFile.exists()) {
            String error =
                "No script available at: " + scriptFile.getAbsolutePath();
            throw new ScriptServiceException(error);
        } else {
            scriptFile.setExecutable(true);
        }
    }

    public OS determineOS() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") >= 0) {
            return OS.Windows;
        } else if (osName.toLowerCase().indexOf("linux") >= 0) {
            return OS.Linux;
        } else {
            String error = "No script version available for OS: " + osName;
            throw new ScriptServiceException(error);
        }
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();
        return props;
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        // Implementation not needed. Update performed through setters.
    }

}
