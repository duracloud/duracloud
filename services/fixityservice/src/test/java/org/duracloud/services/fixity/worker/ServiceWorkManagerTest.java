/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import java.util.concurrent.CountDownLatch;

import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultListener.StatusMsg;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class ServiceWorkManagerTest extends ServiceWorkManagerMockSupport {

    private ServiceWorkManager manager;

    private ServiceWorkload workload;
    private ServiceWorkerFactory workerFactory;
    private ServiceResultListener resultListener;
    private int threads = 3;
    private CountDownLatch doneWorking = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {
        workload = createWorkload();
        workerFactory = createWorkerFactory();
        resultListener = createResultListener();

        manager = new ServiceWorkManager(workload,
                                         workerFactory,
                                         resultListener,
                                         threads,
                                         doneWorking);

    }

    @Test
    public void testRun() throws Exception {
        Assert.assertEquals(1, doneWorking.getCount());

        manager.start();
        StatusMsg status = null;
        while (callsMade < 50) {
            status = manager.getProcessingStatus();
        }
        Assert.assertNotNull(status);
        Assert.assertEquals(STATUS_MSG, status);

        manager.stopProcessing();
        Assert.assertEquals(0, doneWorking.getCount());
    }

}
