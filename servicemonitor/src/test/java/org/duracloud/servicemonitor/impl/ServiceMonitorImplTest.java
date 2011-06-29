/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.serviceapi.aop.DeployMessage;
import org.duracloud.serviceapi.aop.ServiceMessage;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.error.ServiceSummaryException;
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

    private long pollingInterval = 5; //millis
    private ServiceSummaryDirectory summaryDirectory;
    private ServiceSummarizer summarizer;

    @Before
    public void setUp() throws Exception {
        summaryDirectory = EasyMock.createMock("ServiceSummaryDirectory",
                                               ServiceSummaryDirectory.class);
        summarizer = EasyMock.createMock("ServiceSummarizer",
                                         ServiceSummarizer.class);

        monitor = new ServiceMonitorImpl(pollingInterval,
                                         summaryDirectory,
                                         summarizer);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(summaryDirectory, summarizer);
    }

    private void replayMocks() {
        EasyMock.replay(summaryDirectory, summarizer);
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
        throws ServicesException, NotFoundException, ServiceSummaryException {
        Map<String, String> props = new HashMap<String, String>();
        props.put(ComputeService.STATUS_KEY,
                  ComputeService.ServiceStatus.FAILED.name());
        EasyMock.expect(summarizer.getServiceProps(serviceId, deploymentId))
            .andReturn(props);

        ServiceSummary summary = new ServiceSummary();
        summary.setId(serviceId);
        summary.setDeploymentId(deploymentId);

        EasyMock.expect(summarizer.summarizeService(serviceId, deploymentId))
            .andReturn(summary);

        EasyMock.makeThreadSafe(summarizer, true);

        summaryDirectory.addServiceSummary(summary);
        EasyMock.expectLastCall();

        EasyMock.makeThreadSafe(summaryDirectory, true);
    }

    @Test
    public void testOnUndeploy() throws Exception {
        int serviceId = 6;
        int deploymentId = 10;
        replayMocks();

        ServiceMessage msg = new ServiceMessage();
        msg.setServiceId(serviceId);
        msg.setDeploymentId(deploymentId);

        monitor.onUndeploy(msg);
    }

    @Test
    public void testDispose() throws Exception {
        replayMocks();
        monitor.dispose();
    }
}
