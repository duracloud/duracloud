/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.duracloud.syncui.AbstractTest;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.duracloud.syncui.service.SyncOptimizeManager;
import org.duracloud.syncui.service.SyncProcessException;
import org.duracloud.syncui.service.SyncProcessManager;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.View;

/**
 * @author Daniel Bernstein
 */
public class StatusControllerTest extends AbstractTest {

    private SyncProcessManager syncProcessManager;
    private SyncConfigurationManager syncConfigurationManager;
    private SyncOptimizeManager syncOptimizeManager;
    private StatusController statusController;

    @Before
    @Override
    public void setup() {
        super.setup();
        syncProcessManager = createMock(SyncProcessManager.class);
        syncConfigurationManager = createMock(SyncConfigurationManager.class);
        syncOptimizeManager = createMock(SyncOptimizeManager.class);
        statusController =
            new StatusController(syncProcessManager,
                                 syncConfigurationManager,
                                 syncOptimizeManager);
    }

    @Test
    public void testStatus() {
        EasyMock.expect(this.syncConfigurationManager.retrieveDirectoryConfigs())
                .andReturn(new DirectoryConfigs());
        replay();

        String s = statusController.get("queued", new ExtendedModelMap());
        Assert.assertNotNull(s);
    }

    @Test
    public void testStart() throws SyncProcessException {
        syncProcessManager.start();

        EasyMock.expectLastCall().once();
        replay();

        View v = statusController.start();
        Assert.assertNotNull(v);
    }

    @Test
    public void testResume() throws SyncProcessException {
        syncProcessManager.resume();
        EasyMock.expectLastCall().once();
        replay();

        View v = statusController.resume();
        Assert.assertNotNull(v);
    }

    @Test
    public void testPause() throws SyncProcessException {
        syncProcessManager.pause();
        EasyMock.expectLastCall().once();
        replay();

        View v = statusController.pause();
        Assert.assertNotNull(v);
    }

    @Test
    public void testStop() {
        syncProcessManager.stop();
        EasyMock.expectLastCall().once();
        replay();

        View v = statusController.stop();
        Assert.assertNotNull(v);
    }

}
