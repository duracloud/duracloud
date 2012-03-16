/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.exec.error.InvalidActionRequestException;
import org.duracloud.exec.runner.BitIntegrityRunner;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 3/15/12
 */
public class BitIntegrityHandlerTest extends HandlerTestBase {

    private BitIntegrityHandler handler;
    private BitIntegrityRunner runner;

    private int serviceId = 6;
    private String configVersion = "config-version";

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        handler = new BitIntegrityHandler();
        runner = EasyMock.createMock("BitIntegrityRunner",
                                     BitIntegrityRunner.class);
        EasyMock.makeThreadSafe(runner, true);

        handler.setRunner(runner);
    }

    @Override
    protected void replayMocks() {
        super.replayMocks();
        EasyMock.replay(runner);

        handler.initialize(storeMgr, servicesMgr);
    }

    @After
    @Override
    public void teardown() {
        super.teardown();
        EasyMock.verify(runner);
    }

    @Test
    public void testStart() throws Exception {
        EasyMock.expect(servicesMgr.getAvailableServices())
                .andReturn(createServiceList());

        replayMocks();

        handler.start();
    }

    private List<ServiceInfo> createServiceList() {
        ServiceInfo service = new ServiceInfo();
        service.setId(serviceId);
        service.setUserConfigVersion(configVersion);
        service.setDisplayName(BitIntegrityHandler.BIT_INTEGRITY_NAME);
        service.setUserConfigModeSets(new ArrayList<UserConfigModeSet>());

        List<ServiceInfo> services = new ArrayList<ServiceInfo>();
        services.add(service);
        return services;
    }

    @Test
    public void testStop() {
        runner.stop();
        EasyMock.expectLastCall();

        replayMocks();

        handler.stop();
    }

    @Test
    public void testPerformInvalidStart() throws Exception {
        int failedCalls = 0;
        try {
            handler.performAction(BitIntegrityHandler.START_BIT_INTEGRITY, "");
            fail("Exception expected starting with empty parameters");
        } catch (InvalidActionRequestException e) {
            ++failedCalls;
        }

        try {
            String params = "0,0";
            handler.performAction(BitIntegrityHandler.START_BIT_INTEGRITY,
                                  params);
            fail("Exception expected starting with 0 start date");
        } catch (InvalidActionRequestException e) {
            assertTrue(e.getMessage().contains("schedule"));
            ++failedCalls;
        }

        try {
            String params = "1577854800000,0";
            handler.performAction(BitIntegrityHandler.START_BIT_INTEGRITY,
                                  params);
            fail("Exception expected starting with 0 frequency");
        } catch (InvalidActionRequestException e) {
            assertTrue(e.getMessage().contains("frequency"));
            ++failedCalls;
        }

        assertEquals(3, failedCalls);
        replayMocks();
    }

    @Test
    public void testPerformStart() throws Exception {
        EasyMock.expect(runner.isRunning())
                .andReturn(false);

        runner.run();
        EasyMock.expectLastCall();

        replayMocks();

        String params = System.currentTimeMillis() + 100 +",604800000";
        handler.performAction(BitIntegrityHandler.START_BIT_INTEGRITY,
                              params);
        Thread.sleep(500);
    }

    @Test
    public void testPerformCancel() throws Exception {
        runner.stop();
        EasyMock.expectLastCall();

        replayMocks();

        handler.performAction(BitIntegrityHandler.CANCEL_BIT_INTEGRITY, "");
    }

}
