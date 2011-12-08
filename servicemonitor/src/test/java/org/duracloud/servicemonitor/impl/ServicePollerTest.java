/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceCompletionHandler;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.error.ServiceSummaryException;
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
    private ServiceSummaryDirectory summaryDirectory;
    private ServiceSummarizer summarizer;
    private long sleepMillis = 5;
    private ServiceCompletionHandler completionHandler;

    @Before
    public void setUp() throws Exception {
        summaryDirectory = EasyMock.createMock("ServiceSummaryDirectory",
                                               ServiceSummaryDirectory.class);
        summarizer = EasyMock.createMock("ServiceSummarizer",
                                         ServiceSummarizer.class);
        completionHandler = EasyMock.createMock("ServiceCompletionHandler",
                                                ServiceCompletionHandler.class);

        poller = new ServicePoller(serviceId,
                                   deploymentId,
                                   summaryDirectory,
                                   summarizer,
                                   sleepMillis,
                                   completionHandler);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(summaryDirectory, summarizer);
    }

    private void replayMocks() {
        EasyMock.replay(summaryDirectory, summarizer);
    }

    @Test
    public void testRun() throws Exception {
        int times = 5;
        createMockExpections(times);
        replayMocks();

        poller.run();
    }

    private void createMockExpections(int times)
        throws ServicesException, NotFoundException, ServiceSummaryException {
        ServiceSummary summary = new ServiceSummary();
        summary.setId(serviceId);
        summary.setDeploymentId(deploymentId);

        // serviceSummarizer
        Map<String, String> props = createProps(ComputeService.ServiceStatus.PROCESSING);
        Map<String, String> completeProps = createProps(ComputeService.ServiceStatus.SUCCESS);

        if (times > 0) {
            EasyMock.expect(summarizer.getServiceProps(serviceId, deploymentId))
                .andReturn(props)
                .times(times);

            EasyMock.expect(summarizer.getServiceProps(serviceId, deploymentId))
                .andReturn(completeProps);

            EasyMock.expect(summarizer.summarizeService(serviceId,
                                                        deploymentId))
                .andReturn(summary);
        }

        // summaryDirectory
        summaryDirectory.addServiceSummary(summary);
        EasyMock.expectLastCall();

        // completion handler
        completionHandler.handleServiceComplete(summary);
        EasyMock.expectLastCall();
    }

    private Map<String, String> createProps(ComputeService.ServiceStatus status) {
        Map<String, String> props = new HashMap<String, String>();
        props.put(ComputeService.STATUS_KEY, status.name());
        return props;
    }
}
