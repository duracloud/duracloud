/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.duracloud.syncui.AbstractTest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class DirectoryControllerTest extends AbstractTest {

    private DirectoryController directoryController; 
    private SyncConfigurationManager syncConfigurationManager;

    @Before
    @Override
    public void setup() {
        super.setup();

        this.syncConfigurationManager = createMock(SyncConfigurationManager.class);
        this.directoryController = new DirectoryController(syncConfigurationManager);
    }
    
    @Test
    public void testGet() {
        replay();
        Assert.assertNotNull(directoryController.get());
    }
    
    @Test
    public void testAdd() {
        String testPath = "testPath";
        DirectoryConfigs configs = createMock(DirectoryConfigs.class);
        EasyMock.expect(this.syncConfigurationManager.retrieveDirectoryConfigs()).andReturn(configs);
        EasyMock.expect(configs.add(EasyMock.isA(DirectoryConfig.class))).andReturn(true);
        DirectoryConfigForm f = new DirectoryConfigForm();
        f.setDirectoryPath(testPath);
        this.syncConfigurationManager.persistDirectoryConfigs(configs);
        replay();
        Assert.assertNotNull(directoryController.add(f));
    }
}
