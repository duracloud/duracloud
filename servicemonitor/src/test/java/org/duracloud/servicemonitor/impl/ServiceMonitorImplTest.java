/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.aop.DeployMessage;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.servicemonitor.ServiceSummaryWriter;
import org.duracloud.services.ComputeService;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/20/11
 */
public class ServiceMonitorImplTest {

    private ServiceMonitorImpl monitor;

    private String spaceId = "space-id";
    private String contentId = "content-id";
    private long pollingInterval = 5; //millis
    private ServiceSummaryWriter summaryWriter;
    private ServicesManager servicesManager;
    private ContentStore contentStore;

    @Before
    public void setUp() throws Exception {
        summaryWriter = EasyMock.createMock("ServiceSummaryWriter",
                                            ServiceSummaryWriter.class);
        servicesManager = EasyMock.createMock("ServicesManager",
                                              ServicesManager.class);
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);

        monitor = new ServiceMonitorImpl(spaceId,
                                         contentId,
                                         pollingInterval,
                                         summaryWriter,
                                         servicesManager,
                                         contentStore);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(summaryWriter, servicesManager, contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(summaryWriter, servicesManager, contentStore);
    }

    @Test
    public void testOnDeployNull() throws Exception {
        replayMocks();

        boolean thrown = false;
        try {
            monitor.onDeploy(null);
            Assert.fail("exception expected");

        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        Assert.assertTrue("exception was expected", thrown);
    }

    @Test
    public void testOnDeploy() throws Exception {
        int serviceId = 5;
        int deploymentId = 9;

        createOnDeployMockExpectations(serviceId, deploymentId);
        replayMocks();

        DeployMessage msg = new DeployMessage();
        msg.setServiceId(serviceId);
        msg.setDeploymentId(deploymentId);

        monitor.onDeploy(msg);

        Thread.sleep(500); // let the threads work a moment.
    }

    private void createOnDeployMockExpectations(int serviceId, int deploymentId)
        throws ServicesException, NotFoundException {
        Map<String, String> props = new HashMap<String, String>();
        props.put(ComputeService.STATUS_KEY,
                  ComputeService.ServiceStatus.FAILED.name());
        EasyMock.expect(servicesManager.getDeployedServiceProps(serviceId,
                                                                deploymentId))
            .andReturn(props);

        EasyMock.makeThreadSafe(servicesManager, true);

        summaryWriter.collectAndWriteSummary(serviceId, deploymentId);
        EasyMock.expectLastCall();
    }

    @Test
    public void testDispose() throws Exception {
        replayMocks();
        monitor.dispose();
    }
}
