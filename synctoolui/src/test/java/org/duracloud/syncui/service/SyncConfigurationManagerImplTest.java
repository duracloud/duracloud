/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import static org.junit.Assert.*;

import java.io.File;

import org.duracloud.syncui.AbstractTest;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
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

        String username = "username", password = "password", host =
            "host.duracloud.org", spaceId = "test-space-id", port = "8080";

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
        assertEquals(RunMode.CONTINUOUS,this.syncConfigurationManager.getMode());
        this.syncConfigurationManager.setMode(RunMode.SINGLE_PASS);
        setupConfigurationManager();
        assertEquals(RunMode.SINGLE_PASS,this.syncConfigurationManager.getMode());
    }

    @Test
    public void testPurgeWorkDirectory() {
        File workDir = this.syncConfigurationManager.getWorkDirectory();
        if(workDir != null){

            if(!workDir.exists()){
                workDir.mkdirs();
            }
            
            if(workDir.list().length == 0){
                new File(workDir, "test"+System.currentTimeMillis()).mkdir();
            }
        }

        this.syncConfigurationManager.purgeWorkDirectory();

        if(workDir != null){
            assertTrue(workDir.list().length == 0);
        }
    }

}
