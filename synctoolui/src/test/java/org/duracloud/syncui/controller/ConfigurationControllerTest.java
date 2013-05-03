/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.duracloud.syncui.AbstractTest;
import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class ConfigurationControllerTest extends AbstractTest {

    private ConfigurationController configurationController; 
    private SyncConfigurationManager syncConfigurationManager;
    @Before
    @Override
    public void setup() {
        super.setup();

        this.syncConfigurationManager = createMock(SyncConfigurationManager.class);
        this.configurationController = new ConfigurationController(syncConfigurationManager, null);
    }
    
    @Test
    public void testGet() {
        EasyMock.expect(syncConfigurationManager.isSyncDeletes()).andReturn(false);
        
        replay();
        Assert.assertNotNull(configurationController.get(new ExtendedModelMap()));
    }
    
    @Test
    public void testRemove() {
        String testPath = "testPath";
        DirectoryConfigs configs = createMock(DirectoryConfigs.class);
        EasyMock.expect(this.syncConfigurationManager.retrieveDirectoryConfigs()).andReturn(configs);
        EasyMock.expect(configs.removePath(testPath)).andReturn(null);
        DirectoryConfigForm f = new DirectoryConfigForm();
        f.setDirectoryPath(testPath);
        this.syncConfigurationManager.persistDirectoryConfigs(configs);
        replay();
        Assert.assertNotNull(configurationController.removeDirectory(f, new RedirectAttributesModelMap()));
    }
    
    @Test
    public void testGetAdd() {
        replay();
        Assert.assertNotNull(configurationController.getAdd());
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
        Assert.assertNotNull(configurationController.add(f, new RedirectAttributesModelMap()));
    }

}
