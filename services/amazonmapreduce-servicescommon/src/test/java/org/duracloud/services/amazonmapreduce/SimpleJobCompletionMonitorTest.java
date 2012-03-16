/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.duracloud.services.amazonmapreduce.impl.SimpleJobCompletionMonitor;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jun 07, 2011
 */
public class SimpleJobCompletionMonitorTest {

    private SimpleJobCompletionMonitor jobCompletionMonitor;
    private AmazonMapReduceJobWorker worker;
    private BaseAmazonMapReduceService service;

    @Before
    public void setUp() throws Exception {
        worker = createMockWorker();
        service = createMockService();

        long sleepMillis = 1000;
        jobCompletionMonitor = new SimpleJobCompletionMonitor(worker,
                                                              service,
                                                              sleepMillis);
    }

    private AmazonMapReduceJobWorker createMockWorker() {
        AmazonMapReduceJobWorker worker = EasyMock.createMock(
            AmazonMapReduceJobWorker.class);

        EasyMock.expect(worker.getJobStatus()).andReturn(
            AmazonMapReduceJobWorker.JobStatus.RUNNING).times(3);
        EasyMock.expect(worker.getJobStatus()).andReturn(
            AmazonMapReduceJobWorker.JobStatus.COMPLETE).times(1);

        EasyMock.replay(worker);

        return worker;
    }

    private BaseAmazonMapReduceService createMockService() {
        BaseAmazonMapReduceService service = EasyMock.createMock(
            "BaseAmazonMapReduceService",
            BaseAmazonMapReduceService.class);

        service.doneWorking();
        EasyMock.expectLastCall();

        EasyMock.replay(service);
        return service;
    }

    @After
    public void tearDown() {
        EasyMock.verify(worker, service);
    }

    @Test
    public void testRun() throws Exception {
        jobCompletionMonitor.run();
        // This test verifies the interaction with the AmazonMapReduceJobWorker.
    }
}
