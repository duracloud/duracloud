/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.script.osgi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.apache.commons.io.FileUtils;
import org.duracloud.services.script.ScriptService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Bill Branan
 *         Date: Dec 18, 2009
 */
public class ScriptServiceTester {

    private static final String SCRIPT_CREATED_FILE = "script-created-file.txt";

    private ScriptService scriptService;
    private String workDir;

    public ScriptServiceTester(ScriptService service)
        throws IOException {
        this.scriptService = service;

        File workDir = new File(service.getServiceWorkDir());
        workDir.mkdirs();
        this.workDir = workDir.getAbsolutePath();
    }

    public void testScriptService() throws Exception {
        String serviceWorkDir = scriptService.getServiceWorkDir();
        assertNotNull(serviceWorkDir);
        assertTrue(new File(serviceWorkDir).exists());
        testStartStopCycle();
    }

    public void testStartStopCycle() throws Exception {
        createScripts();
        scriptService.start();
        Thread.sleep(2000); // Wait to allow file to be created
        testScriptCreatedFiles(true);
        scriptService.stop();
        Thread.sleep(2000); // Wait to allow file to be deleted
        testScriptCreatedFiles(false);
    }

    private void createScripts() throws Exception {
        ScriptService.OS os = scriptService.determineOS();
        if(os.equals(ScriptService.OS.Linux)) {
            // Start script
            File startScript = new File(workDir, "start.sh");
            startScript.createNewFile();
            List<String> scriptLines = new ArrayList<String>();
            scriptLines.add("#!/bin/bash");
            scriptLines.add("touch " + SCRIPT_CREATED_FILE);
            FileUtils.writeLines(startScript, scriptLines);
            startScript.setExecutable(true);

            // Stop script
            File stopScript = new File(workDir, "stop.sh");
            stopScript.createNewFile();
            scriptLines = new ArrayList<String>();
            scriptLines.add("#!/bin/bash");
            scriptLines.add("rm " + SCRIPT_CREATED_FILE);
            FileUtils.writeLines(stopScript, scriptLines);
            stopScript.setExecutable(true);
        } else { // Windows
            // Start script
            File startScript = new File(workDir, "start.bat");
            startScript.createNewFile();
            List<String> scriptLines = new ArrayList<String>();
            scriptLines.add("echo test > " + SCRIPT_CREATED_FILE);
            FileUtils.writeLines(startScript, scriptLines);

            // Stop script
            File stopScript = new File(workDir, "stop.bat");
            stopScript.createNewFile();
            scriptLines = new ArrayList<String>();
            scriptLines.add("del " + SCRIPT_CREATED_FILE);
            FileUtils.writeLines(stopScript, scriptLines);
        }
    }

    private void testScriptCreatedFiles(boolean shouldExist) throws Exception {
        File file = new File(workDir, SCRIPT_CREATED_FILE);
        assertEquals(shouldExist, file.exists());
    }
}
