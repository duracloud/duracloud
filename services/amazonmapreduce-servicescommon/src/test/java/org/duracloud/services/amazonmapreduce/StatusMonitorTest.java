/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Sep 30, 2010
 */
public class StatusMonitorTest {

    private StatusMonitor statusMonitor;
    private AmazonMapReduceJobWorker worker;

    @Before
    public void setUp() throws Exception {
        worker = createMockWorker();

        long sleepMillis = 1000;
        statusMonitor = new StatusMonitor(worker, sleepMillis);
    }

    private AmazonMapReduceJobWorker createMockWorker() {
        AmazonMapReduceJobWorker worker = EasyMock.createMock(
            AmazonMapReduceJobWorker.class);

        EasyMock.expect(worker.getJobStatus()).andReturn(
            AmazonMapReduceJobWorker.JobStatus.RUNNING).times(3);
        EasyMock.expect(worker.getJobStatus()).andReturn(
            AmazonMapReduceJobWorker.JobStatus.COMPLETE).times(1);

        Map<String, String> emptyMap = new HashMap<String, String>();
        EasyMock.expect(worker.getJobDetailsMap()).andReturn(emptyMap).times(2);

        Map<String, String> doneMap = new HashMap<String, String>();
        doneMap.put("Job State", "COMPLETED");
        EasyMock.expect(worker.getJobDetailsMap()).andReturn(doneMap).times(1);

        worker.shutdown();
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(worker);

        return worker;
    }

    @After
    public void tearDown() {
        EasyMock.verify(worker);
    }

    @Test
    public void testRun() throws Exception {
        statusMonitor.run();
        // This test verifies the interaction with the AmazonMapReduceJobWorker.
    }
}
