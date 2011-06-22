/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.servicemonitor.ServiceSummaryWriter;
import org.duracloud.services.ComputeService;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/20/11
 */
public class ServicePollerTest {

    private ServicePoller poller;

    private int serviceId = 7;
    private int deploymentId = 9;
    private ServiceSummaryWriter summaryWriter;
    private ServicesManager servicesManager;
    private long sleepMillis = 5;
    private boolean continuePolling = true;

    @Before
    public void setUp() throws Exception {
        summaryWriter = EasyMock.createMock("ServiceSummaryWriter",
                                            ServiceSummaryWriter.class);
        servicesManager = EasyMock.createMock("ServicesManager",
                                              ServicesManager.class);

        poller = new ServicePoller(serviceId,
                                       deploymentId,
                                       summaryWriter,
                                       servicesManager,
                                       sleepMillis,
                                       continuePolling);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(summaryWriter, servicesManager);
    }

    private void replayMocks() {
        EasyMock.replay(summaryWriter, servicesManager);
    }

    @Test
    public void testRun() throws Exception {
        int times = 5;
        createMockExpections(times);
        replayMocks();

        poller.run();
    }

    private void createMockExpections(int times)
        throws ServicesException, NotFoundException {
        // serviceManager
        Map<String, String> props = createProps(ComputeService.ServiceStatus.PROCESSING);
        Map<String, String> completeProps = createProps(ComputeService.ServiceStatus.SUCCESS);

        if (times > 0) {
            EasyMock.expect(servicesManager.getDeployedServiceProps(serviceId,
                                                                    deploymentId))
                .andReturn(props)
                .times(times);

            EasyMock.expect(servicesManager.getDeployedServiceProps(serviceId,
                                                                    deploymentId))
                .andReturn(completeProps);
        }

        // summaryWriter
        summaryWriter.collectAndWriteSummary(serviceId, deploymentId);
        EasyMock.expectLastCall();

    }

    private Map<String, String> createProps(ComputeService.ServiceStatus status) {
        Map<String, String> props = new HashMap<String, String>();
        props.put(ComputeService.STATUS_KEY, status.name());
        return props;
    }
}
