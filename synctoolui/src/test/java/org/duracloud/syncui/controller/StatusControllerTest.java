/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.duracloud.syncui.service.SyncProcessException;
import org.duracloud.syncui.service.SyncProcessManager;
import org.duracloud.syncui.AbstractTest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.View;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class StatusControllerTest extends AbstractTest {

    private SyncProcessManager syncProcessManager;
    private StatusController statusController;
    @Before
    @Override
    public void setup() {
        super.setup();
        syncProcessManager = createMock(SyncProcessManager.class);
        statusController = new StatusController(syncProcessManager);
    }
    
    @Test
    public void testStatus() {
        replay();
        
        String s = statusController.get("queued", new ExtendedModelMap());
        Assert.assertNotNull(s);
    }

    
    @Test
    public void testStart() throws SyncProcessException{
        syncProcessManager.start();
        EasyMock.expectLastCall();
        replay();
        
        View v = statusController.start();
        Assert.assertNotNull(v);
    }

    @Test
    public void testResume() throws SyncProcessException{
        syncProcessManager.resume();
        EasyMock.expectLastCall();
        replay();
        
        View v = statusController.resume();
        Assert.assertNotNull(v);
    }

    @Test
    public void testPause() throws SyncProcessException {
        syncProcessManager.pause();
        EasyMock.expectLastCall();
        replay();
        
        View v = statusController.pause();
        Assert.assertNotNull(v);
    }

    @Test
    public void testStop() {
        syncProcessManager.stop();
        EasyMock.expectLastCall();
        replay();
        
        View v = statusController.stop();
        Assert.assertNotNull(v);
    }

 
 
}
