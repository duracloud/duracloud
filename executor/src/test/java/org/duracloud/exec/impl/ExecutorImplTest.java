/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.impl;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.exec.Executor;
import org.duracloud.exec.ServiceHandler;
import org.duracloud.exec.error.InvalidActionRequestException;
import org.duracloud.serviceapi.ServicesManager;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 3/2/12
 */
public class ExecutorImplTest {

    private Executor exec;
    private ContentStoreManager storeMgr;
    private ServicesManager servicesMgr;
    private ServiceHandler handler;

    private static final String ACTION = "ACTION";
    private Set<String> actions;

    @Before
    public void setup() {
        storeMgr = EasyMock.createMock("ContentStoreManager",
                                       ContentStoreManager.class);
        servicesMgr = EasyMock.createMock("ServicesManager",
                                          ServicesManager.class);
        handler = EasyMock.createMock("ServiceHandler", ServiceHandler.class);

        actions = new HashSet<String>();
        actions.add(ACTION);
    }

    @After
    public void teardown() {
        EasyMock.verify(storeMgr, servicesMgr, handler);
    }

    private void replayMocks() {
        EasyMock.replay(storeMgr, servicesMgr, handler);
    }

    @Test
    public void testInitialize() throws Exception {
        setUpInitMocks();

        replayMocks();
        exec = new ExecutorImpl(handler);

        // Verify that no functions are run before init
        int execptions = 0;

        try {
            exec.stop();
        } catch(DuraCloudRuntimeException e) {
            ++execptions;
        }

        try {
            exec.getSupportedActions();
        } catch(DuraCloudRuntimeException e) {
            ++execptions;
        }

        try {
            exec.performAction("action", null);
        } catch(DuraCloudRuntimeException e) {
            ++execptions;
        }

        try {
            exec.getStatus();
        } catch(DuraCloudRuntimeException e) {
            ++execptions;
        }

        assertEquals(4, execptions);

        // Run init
        exec.initialize(storeMgr, servicesMgr);
    }

    private void setUpInitMocks() {
        EasyMock.expect(handler.getSupportedActions())
                .andReturn(actions)
                .anyTimes();

        handler.initialize(storeMgr, servicesMgr);
        EasyMock.expectLastCall();
        
        handler.start();
        EasyMock.expectLastCall();
    }

    @Test
    public void testStop() {
        setUpInitMocks();

        handler.stop();
        EasyMock.expectLastCall();

        replayMocks();
        exec = new ExecutorImpl(handler);
        exec.initialize(storeMgr, servicesMgr);

        // Run stop
        exec.stop();
    }

    @Test
    public void testGetSupportedActions() {
        setUpInitMocks();

        replayMocks();
        exec = new ExecutorImpl(handler);
        exec.initialize(storeMgr, servicesMgr);

        // Get supported actions
        Set<String> retActions = exec.getSupportedActions();
        assertEquals(actions, retActions);
    }

    @Test
    public void testPerformAction() throws Exception {
        setUpInitMocks();

        String invalidAct = "invalid-action";
        String actParams = "action-parameters";

        handler.performAction(ACTION, actParams);
        EasyMock.expectLastCall();

        replayMocks();
        exec = new ExecutorImpl(handler);
        exec.initialize(storeMgr, servicesMgr);

        // Perform invalid action
        try {
            exec.performAction(invalidAct, actParams);
            fail("Exception expected performing invalid action");
        } catch (InvalidActionRequestException e) {
            assertNotNull(e);
        }

        // Perform valid action
        exec.performAction(ACTION, actParams);
    }

    @Test
    public void testGetStatus() {
        setUpInitMocks();

        String handlerName = "handler-name";
        String handlerStatus = "handler-status";

        EasyMock.expect(handler.getName()).andReturn(handlerName);
        EasyMock.expect(handler.getStatus()).andReturn(handlerStatus);

        replayMocks();
        exec = new ExecutorImpl(handler);
        exec.initialize(storeMgr, servicesMgr);

        // Get status
        Map<String, String> status = exec.getStatus();
        assertNotNull(status);
        assertEquals(1, status.size());
        assertEquals(handlerStatus, status.get(handlerName));
    }

}
