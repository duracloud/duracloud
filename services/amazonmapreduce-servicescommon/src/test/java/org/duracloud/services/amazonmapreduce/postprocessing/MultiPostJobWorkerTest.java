/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker.JobStatus;

/**
 * @author Andrew Woods
 *         Date: Oct 1, 2010
 */
public class MultiPostJobWorkerTest {

    private AmazonMapReduceJobWorker multiPostJobWorker;
    private AmazonMapReduceJobWorker predecessor;
    private AmazonMapReduceJobWorker worker0;
    private AmazonMapReduceJobWorker worker1;
    private AmazonMapReduceJobWorker worker2;

    private List<String> proof = new ArrayList<String>();
    private String id0 = "id-0";
    private String id1 = "id-1";
    private String id2 = "id-2";
    private long sleepMillis = 100;


    private void createMocks() throws Exception {
        createMocks(false);
    }

    private void createMocks(boolean withErrors) throws Exception {
        predecessor = createMockJobWorker();

        worker0 = createMockPostJobWorker(predecessor, id0, withErrors);
        worker1 = createMockPostJobWorker(worker0, id1, withErrors);
        worker2 = createMockPostJobWorker(worker1, id2, withErrors);
        multiPostJobWorker = new MultiPostJobWorker(predecessor,
                                                    sleepMillis,
                                                    worker0,
                                                    worker1,
                                                    worker2);

        Assert.assertEquals(JobStatus.WAITING, worker0.getJobStatus());
        Assert.assertEquals(JobStatus.WAITING, worker1.getJobStatus());
        Assert.assertEquals(JobStatus.WAITING, worker2.getJobStatus());
    }

    private AmazonMapReduceJobWorker createMockJobWorker() {
        AmazonMapReduceJobWorker worker = EasyMock.createMock(
            "PredecessorWorker",
            AmazonMapReduceJobWorker.class);

        EasyMock.expect(worker.getJobStatus())
            .andReturn(JobStatus.RUNNING)
            .times(2);
        EasyMock.expect(worker.getJobStatus())
            .andReturn(JobStatus.COMPLETE)
            .times(2);

        EasyMock.replay(worker);
        return worker;
    }

    private AmazonMapReduceJobWorker createMockPostJobWorker(
        AmazonMapReduceJobWorker aPredecessor,
        final String id,
        final boolean withError) {
        return new BaseAmazonMapReducePostJobWorker(aPredecessor, sleepMillis) {
            @Override
            protected void doWork() {
                sleep(sleepMillis); // do some work
                proof.add(id);
            }

            @Override
            public String getError() {
                return withError ? "some-error" : null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(predecessor);

        Assert.assertEquals(JobStatus.COMPLETE, worker0.getJobStatus());
        Assert.assertEquals(JobStatus.COMPLETE, worker1.getJobStatus());
        Assert.assertEquals(JobStatus.COMPLETE, worker2.getJobStatus());
    }

    @Test
    public void testGetJobStatus() throws Exception {
        createMocks();

        JobStatus status = multiPostJobWorker.getJobStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(JobStatus.WAITING, status);

        multiPostJobWorker.run();

        status = multiPostJobWorker.getJobStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(JobStatus.COMPLETE, status);
    }
    
    @Test
    public void testRun() throws Exception {
        createMocks();

        multiPostJobWorker.run();

        Assert.assertEquals(3, proof.size());
        Assert.assertTrue(id0 + " not found", proof.contains(id0));
        Assert.assertTrue(id1 + " not found", proof.contains(id1));
        Assert.assertTrue(id2 + " not found", proof.contains(id2));
    }

    @Test
    public void testGetError() throws Exception {
        createMocks();

        multiPostJobWorker.run();

        String error = multiPostJobWorker.getError();
        Assert.assertNull(error, error);
    }

    @Test
    public void testGetErrorWith() throws Exception {
        boolean withErrors = true;
        createMocks(withErrors);

        multiPostJobWorker.run();

        String error = multiPostJobWorker.getError();
        Assert.assertNotNull(error);
        Assert.assertEquals("some-error", error);
    }

}
