/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class SyncToolConfigParserTest {

    SyncToolConfigParser syncConfigParser;
    File tempDir;

    @Before
    public void setUp() throws Exception {
        syncConfigParser = new SyncToolConfigParser();
        tempDir = new File(System.getProperty("java.io.tmpdir"));
    }

    @After
    public void tearDown() throws Exception {
        File backupFile =
            new File(tempDir, SyncToolConfigParser.BACKUP_FILE_NAME);
        if(backupFile.exists()) {
            backupFile.delete();
        }

        File prevBackupFile =
            new File(tempDir, SyncToolConfigParser.PREV_BACKUP_FILE_NAME);
        if(prevBackupFile.exists()) {
            prevBackupFile.delete();
        }
    }

    @Test
    public void testStandardOptions() throws Exception {
        HashMap<String, String> argsMap = getArgsMap();

        // Process configs, make sure values match
        SyncToolConfig syncConfig =
            syncConfigParser.processStandardOptions(mapToArray(argsMap));
        checkStandardOptions(argsMap, syncConfig);

        // Remove optional params
        argsMap.remove("-f");
        argsMap.remove("-p");
        argsMap.remove("-t");
        argsMap.remove("-m");
        argsMap.remove("-x");

        // Process configs, make sure optional params are set to defaults
        syncConfig =
            syncConfigParser.processStandardOptions(mapToArray(argsMap));
        assertEquals(SyncToolConfigParser.DEFAULT_POLL_FREQUENCY,
                     syncConfig.getPollFrequency());
        assertEquals(SyncToolConfigParser.DEFAULT_PORT, syncConfig.getPort());
        assertEquals(SyncToolConfigParser.DEFAULT_NUM_THREADS,
                     syncConfig.getNumThreads());
        assertEquals(SyncToolConfigParser.DEFAULT_MAX_FILE_SIZE *
                     SyncToolConfigParser.GIGABYTE,
                     syncConfig.getMaxFileSize());
        assertEquals(false, syncConfig.syncDeletes());

        // Make sure error is thrown on missing required params
        for(String arg : argsMap.keySet()) {
            String failMsg = "An exception should have been thrown due to " +
                             "missing arg: " + arg;
            removeArgFailTest(argsMap, arg, failMsg);
        }

        // Make sure error is thrown when numerical args are not numerical
        String failMsg = "Frequency arg should require a numerical value";
        addArgFailTest(argsMap, "-f", "nonNum", failMsg);
        failMsg = "Port arg should require a numerical value";
        addArgFailTest(argsMap, "-p", "nonNum", failMsg);
        failMsg = "Threads arg should require a numerical value";
        addArgFailTest(argsMap, "-t", "nonNum", failMsg);
        failMsg = "Max file size arg should require a numerical value";
        addArgFailTest(argsMap, "-m", "nonNum", failMsg);
        failMsg = "Max file size arg should be between 1 and 5";
        addArgFailTest(argsMap, "-m", "0", failMsg);
        addArgFailTest(argsMap, "-m", "6", failMsg);
    }

    private HashMap<String, String> getArgsMap() {
        HashMap<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("-b", tempDir.getAbsolutePath());
        argsMap.put("-f", "1000");
        argsMap.put("-h", "localhost");
        argsMap.put("-p", "8088");
        argsMap.put("-d", tempDir.getAbsolutePath());
        argsMap.put("-t", "5");
        argsMap.put("-u", "user");
        argsMap.put("-w", "pass");
        argsMap.put("-s", "mySpace");
        argsMap.put("-m", "2");
        argsMap.put("-x", "");
        return argsMap;
    }

    private void checkStandardOptions(HashMap<String, String> argsMap,
                                      SyncToolConfig syncConfig) {
        assertEquals(argsMap.get("-b"),
                     syncConfig.getBackupDir().getAbsolutePath());
        assertEquals(argsMap.get("-f"),
                     String.valueOf(syncConfig.getPollFrequency()));
        assertEquals(argsMap.get("-h"), syncConfig.getHost());
        assertEquals(argsMap.get("-p"),
                     String.valueOf(syncConfig.getPort()));
        assertEquals(argsMap.get("-d"),
                     syncConfig.getSyncDirs().get(0).getAbsolutePath());
        assertEquals(argsMap.get("-t"),
                     String.valueOf(syncConfig.getNumThreads()));
        assertEquals(argsMap.get("-u"), syncConfig.getUsername());
        assertEquals(argsMap.get("-w"), syncConfig.getPassword());
        assertEquals(argsMap.get("-s"), syncConfig.getSpaceId());
        assertEquals(argsMap.get("-m"),
                     String.valueOf(syncConfig.getMaxFileSize() /
                                    SyncToolConfigParser.GIGABYTE));
        assertEquals(true, syncConfig.syncDeletes());
    }

    private String[] mapToArray(HashMap<String, String> map) {
        ArrayList<String> list = new ArrayList<String>();
        for(String key : map.keySet()) {
            list.add(key);
            list.add(map.get(key));
        }
        return list.toArray(new String[0]);
    }

    private void addArgFailTest(HashMap<String, String> argsMap,
                                String arg,
                                String value,
                                String failMsg) {
        HashMap<String, String> cloneMap =
            (HashMap<String, String>)argsMap.clone();
        cloneMap.put(arg, value);
        try {
            syncConfigParser.processStandardOptions(mapToArray(cloneMap));
            fail(failMsg);
        } catch(ParseException e) {
            assertNotNull(e);
        }
    }

    private void removeArgFailTest(HashMap<String, String> argsMap,
                                   String arg,
                                   String failMsg) {
        HashMap<String, String> cloneMap =
            (HashMap<String, String>)argsMap.clone();
        cloneMap.remove(arg);
        try {
            syncConfigParser.processStandardOptions(mapToArray(cloneMap));
            fail(failMsg);
        } catch(ParseException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testBackupRestore() throws Exception {
        String[] testArgs = {"-a", "b", "-c", "d", "e", "f", "-g", "-h", "i"};
        syncConfigParser.backupConfig(tempDir, testArgs);

        File backupFile = getBackupFile();
        String[] retrieveArgs = syncConfigParser.retrieveConfig(backupFile);
        compareArrays(testArgs, retrieveArgs);
    }

    @Test
    public void testPrevBackupFile() throws Exception {
        HashMap<String, String> argsMap = getArgsMap();
        String[] args = mapToArray(argsMap);

        // First backup
        syncConfigParser.backupConfig(tempDir, args);
        File backupFile = getBackupFile();
        String[] retrieveArgs = syncConfigParser.retrieveConfig(backupFile);
        compareArrays(args, retrieveArgs);

        HashMap<String, String> newArgsMap =
            (HashMap<String, String>)argsMap.clone();
        newArgsMap.put("-z", "new");
        String[] newArgs = mapToArray(newArgsMap);

        // Second backup
        syncConfigParser.backupConfig(tempDir, newArgs);

        // Check config file (should be new args)
        backupFile = getBackupFile();
        retrieveArgs = syncConfigParser.retrieveConfig(backupFile);
        compareArrays(newArgs, retrieveArgs);

        // Check previous config backup (should be old args)
        backupFile = getPrevBackupFile();
        retrieveArgs = syncConfigParser.retrieveConfig(backupFile);
        compareArrays(args, retrieveArgs);
    }

    private File getBackupFile() {
        File backupFile =
            new File(tempDir, SyncToolConfigParser.BACKUP_FILE_NAME);
        assertTrue(backupFile.exists());
        return backupFile;
    }

    private File getPrevBackupFile() {
        File prevBackupFile =
            new File(tempDir, SyncToolConfigParser.PREV_BACKUP_FILE_NAME);
        assertTrue(prevBackupFile.exists());
        return prevBackupFile;
    }

    private void compareArrays(String[] arr1, String[] arr2) {
        assertEquals(arr1.length, arr2.length);
        for(int i=0; i<arr1.length; i++) {
            assertTrue(arr1[i].equals(arr2[i]));
        }
    }

    @Test
    public void testConfigFileOptions() throws Exception {
        HashMap<String, String> argsMap = getArgsMap();

        // Process standard options
        String[] args = mapToArray(argsMap);
        SyncToolConfig syncConfig =
            syncConfigParser.processStandardOptions(args);
        // Create config backup file
        syncConfigParser.backupConfig(syncConfig.getBackupDir(), args);
        File backupFile = getBackupFile();

        // Create arg map including only -c option, pointing to config file
        argsMap = new HashMap<String, String>();
        argsMap.put("-c", backupFile.getAbsolutePath());

        // Process using config file
        syncConfigParser = new SyncToolConfigParser();
        syncConfig =
            syncConfigParser.processConfigFileOptions(mapToArray(argsMap));
        checkStandardOptions(getArgsMap(), syncConfig);
    }

}
