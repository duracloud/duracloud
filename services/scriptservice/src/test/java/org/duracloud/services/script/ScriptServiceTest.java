/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.script;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.duracloud.services.script.error.ScriptServiceException;
import org.duracloud.services.script.osgi.ScriptServiceTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: Dec 11, 2009
 */
public class ScriptServiceTest extends TestCase {

    private static final String SCRIPT_CREATED_FILE = "script-created-file.txt";

    private ScriptService scriptService;
    private String workDir;

    @Before
    public void setUp() throws IOException {
        setWorkDir("target/script-test");
        File serviceWorkDir = new File(workDir);
        serviceWorkDir.mkdirs();

        ScriptService scriptService = new ScriptService();
        scriptService.setServiceWorkDir(workDir);
        setScriptService(scriptService);
    }

    @After
    public void tearDown() {
        File serviceWorkDir = new File(workDir);
        try {
            FileUtils.deleteDirectory(serviceWorkDir);
        } catch(IOException e) {
            // Ignore
        }
    }

    @Test
    public void testServiceBasics() throws Exception {
        assertNotNull(scriptService.describe());
        assertNotNull(scriptService.getServiceProps());
    }

    @Test
    public void testNoScripts() throws Exception {
        try {
            scriptService.start();
            fail("Start should throw when no script is available");
        } catch(ScriptServiceException expected) {
            assertNotNull(expected);
        }

        try {
            scriptService.stop();
            fail("Stop should throw when no script is available");
        } catch(ScriptServiceException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testStartStopCycle() throws Exception {
        ScriptServiceTester tester = new ScriptServiceTester(scriptService);
        tester.testStartStopCycle();
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }    

}
