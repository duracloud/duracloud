/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.setup;

import org.duracloud.syncui.service.SyncProcessManager;
import org.duracloud.syncui.AbstractTest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class StartSyncActionTest extends AbstractTest {

    @Test
    public void testExecute() throws Exception {
        SyncProcessManager spm = createMock(SyncProcessManager.class);
        spm.start();
        EasyMock.expectLastCall();
        RequestContext context = createMock(RequestContext.class);
        
        replay();
        
        StartSyncAction a = new StartSyncAction(spm);
        
        Event result = a.execute(context);
        
        Assert.assertEquals("success", result.getId());
        
    }

}
