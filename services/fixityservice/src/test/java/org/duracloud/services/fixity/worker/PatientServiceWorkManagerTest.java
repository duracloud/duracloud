/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import org.duracloud.services.fixity.results.ServiceResultListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class PatientServiceWorkManagerTest extends ServiceWorkManagerMockSupport {

    private PatientServiceWorkManager manager;

    private ServiceWorkload workload;
    private ServiceWorkerFactory workerFactory;
    private ServiceResultListener resultListener;
    private int threads = 3;
    private int NUM_LATCHES = 3;
    private CountDownLatch countDownLatch = new CountDownLatch(NUM_LATCHES);

    @Before
    public void setUp() throws Exception {
        workload = createWorkload();
        workerFactory = createWorkerFactory();
        resultListener = createResultListener();

        manager = new PatientServiceWorkManager(workload,
                                                workerFactory,
                                                resultListener,
                                                threads,
                                                countDownLatch);
    }

    @Test
    public void testRun() throws Exception {
        manager.start();
        verifyLatchCountAndCallsThenRest(NUM_LATCHES);

        countDownLatch.countDown();
        verifyLatchCountAndCallsThenRest(NUM_LATCHES - 1);

        countDownLatch.countDown();
        verifyLatchCountAndCallsThenRest(NUM_LATCHES - 2);

        countDownLatch.countDown();
        Thread.sleep(500);
        verifyLatchCountAndCallsThenRest(NUM_LATCHES - 3);
    }

    private void verifyLatchCountAndCallsThenRest(int count)
        throws InterruptedException {
        Assert.assertEquals(count, countDownLatch.getCount());

        if (count == 0) {
            Assert.assertTrue(callsMade > 0);
        } else {
            Assert.assertEquals(0, callsMade);
        }
        Thread.sleep(10);
    }
}
