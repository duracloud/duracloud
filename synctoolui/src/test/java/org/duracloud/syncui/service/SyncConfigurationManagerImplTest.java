/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.duracloud.syncui.AbstractTest;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Bernstein
 */
public class SyncConfigurationManagerImplTest extends AbstractTest {
    private SyncConfigurationManagerImpl syncConfigurationManager;
    private String configPath;

    @Before
    public void setUp() throws Exception {
        super.setup();
        configPath = System.getProperty("java.io.tmpdir")
                     + File.separator + ".sync-config" + System.currentTimeMillis();

        setupConfigurationManager();
    }

    protected void setupConfigurationManager() {
        syncConfigurationManager = new SyncConfigurationManagerImpl();
        syncConfigurationManager.setConfigXmlPath(configPath);
    }

    @Override
    public void tearDown() {
        new File(configPath).delete();
        super.tearDown();
    }

    @Test
    public void testIsConfigurationCompleteFalse() {
        assertFalse(this.syncConfigurationManager.isConfigurationComplete());
    }

    @Test
    public void testPersistDuracloudConfiguration() {

        String username = "username";
        String password = "password";
        String host = "host.duracloud.org";
        String spaceId = "test-space-id";
        String port = "8080";

        this.syncConfigurationManager.persistDuracloudConfiguration(username,
                                                                    password,
                                                                    host,
                                                                    port,
                                                                    spaceId);
    }

    @Test
    public void testRetrieveDirectoryConfigs() {
        DirectoryConfigs directoryConfigs =
            this.syncConfigurationManager.retrieveDirectoryConfigs();
        assertNotNull(directoryConfigs);
    }

    @Test
    public void testRetrieveDuracloudConfiguration() {
        DuracloudConfiguration dc =
            this.syncConfigurationManager.retrieveDuracloudConfiguration();
        assertNotNull(dc);
    }

    @Test
    public void testGetSetRunMode() {
        this.syncConfigurationManager.setMode(RunMode.CONTINUOUS);
        setupConfigurationManager();
        assertEquals(RunMode.CONTINUOUS, this.syncConfigurationManager.getMode());
        this.syncConfigurationManager.setMode(RunMode.SINGLE_PASS);
        setupConfigurationManager();
        assertEquals(RunMode.SINGLE_PASS, this.syncConfigurationManager.getMode());
    }

    @Test
    public void testGetSetMaxFileSize() {
        long mfs = SyncConfigurationManager.GIGABYTES;
        long mfs2 = 2 * SyncConfigurationManager.GIGABYTES;

        this.syncConfigurationManager.setMaxFileSizeInBytes(mfs);
        setupConfigurationManager();
        assertEquals(mfs, this.syncConfigurationManager.getMaxFileSizeInBytes());
        this.syncConfigurationManager.setMaxFileSizeInBytes(mfs2);
        setupConfigurationManager();
        assertEquals(mfs2, this.syncConfigurationManager.getMaxFileSizeInBytes());
    }

    @Test
    public void testPurgeWorkDirectory() {
        File workDir = this.syncConfigurationManager.getWorkDirectory();
        if (workDir != null) {

            if (!workDir.exists()) {
                workDir.mkdirs();
            }

            if (workDir.list().length == 0) {
                new File(workDir, "test" + System.currentTimeMillis()).mkdir();
            }
        }

        this.syncConfigurationManager.purgeWorkDirectory();

        if (workDir != null) {
            assertTrue(workDir.list().length == 0);
        }
    }

    @Test
    public void testGetSetExcludeList() {
        File excludeList = new File(System.getProperty("java.io.tmpdir")
                + File.separator + "excludeList.txt");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("test1.txt");
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append("path/to/test2.txt");

        String excludeListContent = stringBuilder.toString();

        writeExcludeList(excludeList, excludeListContent);

        this.syncConfigurationManager.setExcludeList(excludeList);
        setupConfigurationManager();

        File secondExcludeList = this.syncConfigurationManager.getExcludeList();

        assertEquals(excludeListContent, readExcludeList(secondExcludeList));
        if (excludeList.exists()) {
            excludeList.delete();
        }
    }

    private void writeExcludeList(File excludeList, String content) {
        try {
            BufferedWriter backupWriter =
                    new BufferedWriter(new FileWriter(excludeList));
            backupWriter.write(content);
            backupWriter.flush();
            backupWriter.close();
        } catch (IOException e ) {
            throw new RuntimeException("Unable to write sample exclude list file " +
                    "due to: " + e.getMessage(), e);
        }
    }

    private String readExcludeList(File excludeList) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(excludeList));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();

            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read sample exclude list file " +
                    "due to: " + e.getMessage(), e);
        }
    }
}
